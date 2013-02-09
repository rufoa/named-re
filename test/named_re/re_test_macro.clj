(ns named-re.re-test-macro
	(:require named-re.core)
	(:use midje.sweet))

(tabular "patterns with named groups work as expected"
	(fact
		(re-seq ?re ?s) => ?r)
	?re                             ?s           ?r
	#"a(?<g>.)"                     "ab"         (just [{:g "b" :0 "ab"}])
	#"(?<g>a)\k<g>"                 "aa"         (just [{:g "a" :0 "aa"}])
	#"(?<g>a)\k<g>"                 "ab"         nil
	#"(?<g>.)(?<h>.)\k<h>\k<g>"     "abba"       (just [{:g "a" :h "b" :0 "abba"}])
	#"(?<g>.)(?<h>.)\k<h>\k<g>"     "abab"       nil
	#"(?<g>a)\\k<g>"                "a\\k<g>"    (just [{:g "a" :0 "a\\k<g>"}])
	#"(?<g>a)\\\k<g>"               "a\\a"       (just [{:g "a" :0 "a\\a"}])
	#"(?<g>a)\\\k<g>"               "a\\b"       nil
	#"(?<g>[a-z])(?<h>[0-9])"       "a1 b2 c3"   (just [{:g "a" :h "1" :0 "a1"} {:g "b" :h "2" :0 "b2"} {:g "c" :h "3" :0 "c3"}]))

(tabular "named group detection"
	(fact
		(re-seq ?re ?s) => ?r)
	?re                            ?s           ?r
	#"(?:<g>abc)"                  "<g>abc"     (just ["<g>abc"])             ;non-capturing group; no named groups in this pattern
	#"\(?<g>a\)"                   "a"          nil                           ;literal left paren, not start of group
	#"\\(?<g>a)"                   "\\a"        (just [{:g "a" :0 "\\a"}]))   ;literal backslash followed by named group

(tabular "named replacements work as expected"
	(fact
		(clojure.string/replace ?string ?pattern ?replacement) => ?result)
	?string   ?pattern             ?replacement   ?result
	"abc"     #"a(?<g>.)(?<h>.)"   "a${h}${g}"    "acb"
	"ab"      #"a(?<g>.)"          "\\${g}a"      "${g}a"   ;literal $ followed by {g}
	"ab"      #"a(?<g>.)"          "\\\\${g}a"    "\\ba")   ;literal backslash followed by group g