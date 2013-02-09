(ns named-re.re
	(:refer-clojure :exclude [re-pattern re-matcher re-groups re-seq re-matches re-find])
	(:import [java.util.regex Pattern Matcher]))

(defn- is-java7?
	"Does the JRE support named groups directly?"
	[]
	(>= (java.lang.Float/parseFloat (System/getProperty "java.specification.version")) 1.7))

(defn- group-mapping
	"Returns map from group names to group numbers, given a regex string"
	[s]
	(into {}
		(filter
			(comp not nil? first) ; only named groups
			(zipmap
				(map second (clojure.core/re-seq #"(?<!\\)(?:\\\\)*\((?:\?<(\w+)>|[^?])" s)) ; all capturing groups
				(drop 1 (range)))))) ; number from 1

(defn re-pattern
	[s]
	(let
		[s (if (instance? Pattern s) (. ^Pattern s (pattern)) s) ; for backwards compatibility with clojure.core/re-pattern
		 mapping (group-mapping s)]
		(if (empty? mapping) ; no named groups
			(. Pattern (compile s))
			{:mapping mapping
			 :pattern
			 	(if (is-java7?)
			 		(. Pattern (compile s))
			 		(. Pattern (compile
						(-> s
							(clojure.string/replace ,,, #"(?<!\\)(\\\\)*\(\?<\w+>" "$1(") ; turn named groups into normal groups
							(clojure.string/replace ,,, #"(?<!\\)(\\\\)*\\k<(\w+)>" #(Matcher/quoteReplacement (str (nth % 1) "\\" (mapping (nth % 2)))))))))}))) ; replace named backreferences with corresponding number

(defn re-matcher
	[re s]
	(if (map? re)
		(let [{:keys [mapping pattern]} re]
			{:mapping mapping
			 :matcher (. ^Pattern pattern (matcher s))})
		(. ^Pattern re (matcher s))))

(defn re-groups
	[m]
	(if (map? m)
		(reduce
			(fn
				[map [name number]]
				(if (is-java7?)
					(assoc map (keyword name) (. ^Matcher (:matcher m) (group ^String name)))
					(assoc map (keyword name) (. ^Matcher (:matcher m) (group ^Integer number)))))
			{:0 (. ^Matcher (:matcher m) (group 0))}
			(:mapping m))
		(let
			[m ^Matcher m
			 gc (. m (groupCount))]
			(if (zero? gc)
				(. m (group))
				(loop [ret [] c 0]
					(if (<= c gc)
						(recur (conj ret (. m (group c))) (inc c))
						ret))))))

(defn re-seq
	[re s]
	(let [m (re-matcher re s)]
		((fn step []
			(when (. (if (map? m) ^Matcher (:matcher m) ^Matcher m) (find))
				(cons (re-groups m) (lazy-seq (step))))))))

(defn re-matches
	[re s]
	(let
		[^Pattern re (if (map? re) (:pattern re) re)
		 ^Matcher m (re-matcher re s)]
		(when (. m (matches))
			(re-groups m))))

(defn re-find
	([m]
		(when (. (if (map? m) ^Matcher (:matcher m) ^Matcher m) (find))
			(re-groups m)))
	([re s]
		(re-find (re-matcher re s))))