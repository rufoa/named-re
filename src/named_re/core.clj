(ns named-re.core
	(:require [named-re re string])
	(:use reader-macros.core))

(set! *warn-on-reflection* true)

(defn- raw-read
	"Read like clojure.lang.RegexReader does - take every character as a literal and don't convert escape sequences"
	;see https://groups.google.com/d/msg/clojure/eJtXMlyIV1I/WXez0DtTbrkJ
	[^java.io.Reader reader]
	(loop [output "" esc false]
		(let [a (. reader read)]
			(if (= a -1)
				(throw (clojure.lang.Util/runtimeException "EOF while reading regex"))
				(let [c (char a)]
					(if (and (= c \") (not esc)) ;unescaped double quote signals end
						output
						(recur
							(str output c)
							(and (= c \\) (not esc))))))))) ;backslash triggers escape mode unless we're already in it

(set-dispatch-macro-character \"
	(fn [reader quote]
		(re-pattern (raw-read reader))))

(alter-var-root #'clojure.core/re-pattern (fn [f] #(apply named-re.re/re-pattern  %&)))
(alter-var-root #'clojure.core/re-matcher (fn [f] #(apply named-re.re/re-matcher  %&)))
(alter-var-root #'clojure.core/re-groups  (fn [f] #(apply named-re.re/re-groups   %&)))
(alter-var-root #'clojure.core/re-seq     (fn [f] #(apply named-re.re/re-seq      %&)))
(alter-var-root #'clojure.core/re-matches (fn [f] #(apply named-re.re/re-matches  %&)))
(alter-var-root #'clojure.core/re-find    (fn [f] #(apply named-re.re/re-find     %&)))
(alter-var-root #'clojure.string/replace  (fn [f] #(apply named-re.string/replace %&)))