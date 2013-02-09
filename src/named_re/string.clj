(ns named-re.string
	(:refer-clojure :exclude [replace])
	(:require [named-re.re :only [re-matcher]])
	(:import [java.util.regex Pattern Matcher]))

(defn- replace-by
	[^CharSequence s re f]
	(let [m (clojure.core/re-matcher re s)]
		(let [buffer (StringBuffer. (.length s))]
			(loop []
				(if (.find m)
					(do (.appendReplacement m buffer (f (clojure.core/re-groups m)))
						(recur))
					(do (.appendTail m buffer)
						(.toString buffer)))))))

(defn- replace-first-by
	[^CharSequence s ^Pattern re f]
	(let [m (clojure.core/re-matcher re s)]
		(let [buffer (StringBuffer. (.length s))]
			(if (.find m)
				(let [rep (f (clojure.core/re-groups m))]
					(.appendReplacement m buffer rep)
					(.appendTail m buffer)
					(str buffer))))))

(defn- replace-first-char
	[^CharSequence s ^Character match replace]
	(let
		[s (.toString s)
		 i (.indexOf s (int match))]
		(if (= -1 i)
			s
			(str (subs s 0 i) replace (subs s (inc i))))))

(defn- replace-first-str
	[^CharSequence s ^String match ^String replace]
	(let
		[^String s (.toString s)
		 i (.indexOf s match)]
		(if (= -1 i)
			s
			(str (subs s 0 i) replace (subs s (+ i (.length match)))))))

(defn- equivalent-replacement
	[s mapping]
	(clojure.string/replace
		s
		#"(?<!\\)(\\\\)*\$\{(\w+)\}"
		#(Matcher/quoteReplacement
			(str (nth % 1) "$" (mapping (nth % 2))))))

(defn ^String replace
	[^CharSequence s match replacement]
	(if (map? match)
		(let
			[s' (.toString s)
			 m (named-re.re/re-matcher match s')]
			(if (instance? CharSequence replacement)
				(.replaceAll
					^Matcher (:matcher m)
					(equivalent-replacement (.toString ^CharSequence replacement) (:mapping m)))
				(replace-by
					s'
					(:pattern match)
					(equivalent-replacement replacement (:mapping match)))))
		(let [s (.toString s)]
			(cond 
				(instance? Character match)
				(.replace s ^Character match ^Character replacement)
				(instance? CharSequence match)
				(.replace s ^CharSequence match ^CharSequence replacement)
				(instance? Pattern match)
				(if (instance? CharSequence replacement)
					(.replaceAll
						^Matcher (clojure.core/re-matcher ^Pattern match s)
						(.toString ^CharSequence replacement))
					(replace-by s match replacement))
				:else
				(throw (IllegalArgumentException. (str "Invalid match arg: " match)))))))

(defn ^String replace-first
	[^CharSequence s match replacement]
	(if (map? match)
		(let
			[s' (.toString s)
			 m (named-re.re/re-matcher match s')]
			(if (instance? CharSequence replacement)
				(.replaceFirst
					^Matcher (:matcher m)
					(equivalent-replacement (.toString ^CharSequence replacement) (:mapping m)))
				(replace-first-by
					s'
					(:pattern match)
					(equivalent-replacement replacement (:mapping match)))))
		(let [s (.toString s)]
			(cond
				(instance? Character match)
				(replace-first-char s match replacement)
				(instance? CharSequence match)
				(replace-first-str
					s
					(.toString ^CharSequence match)
					(.toString ^CharSequence replacement))
				(instance? Pattern match)
				(if (instance? CharSequence replacement)
					(.replaceFirst
						^Matcher (clojure.core/re-matcher ^Pattern match s)
						(.toString ^CharSequence replacement))
					(replace-first-by s match replacement))
				:else
				(throw (IllegalArgumentException. (str "Invalid match arg: " match)))))))