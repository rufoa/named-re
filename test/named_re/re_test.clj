(ns named-re.re-test
	(:require named-re.re)
	(:require named-re.string)
	(:use midje.sweet))

(tabular "patterns without named groups work as usual"
	(fact
		(named-re.re/re-find    (named-re.re/re-pattern ?re) ?s) => (clojure.core/re-find    (clojure.core/re-pattern ?re) ?s)
		(named-re.re/re-seq     (named-re.re/re-pattern ?re) ?s) => (clojure.core/re-seq     (clojure.core/re-pattern ?re) ?s)
		(named-re.re/re-matches (named-re.re/re-pattern ?re) ?s) => (clojure.core/re-matches (clojure.core/re-pattern ?re) ?s))
	?re                ?s
	"."                "abc"
	"a"                "def"
	"a(.)"             "ab"
	"(a)\\1"           "aa"         ;backreference
	"(a)\\1"           "ab"         ;backreference
	"(a)\\\\1"         "a\\1"       ;literal backslash followed by 1
	"(a)\\\\\\1"       "a\\a"       ;literal backslash followed by backreference
	"(a)\\\\\\1"       "a\\b"       ;literal backslash followed by backreference
	"([a-z])([0-9])"   "a1 b2 c3")

(tabular "patterns with named groups work as expected"
	(fact
		(named-re.re/re-seq (named-re.re/re-pattern ?re) ?s) => ?r)
	?re                            ?s           ?r
	"a(?<g>.)"                     "ab"         (just [{:g "b" :0 "ab"}])
	"(?<g>a)\\k<g>"                "aa"         (just [{:g "a" :0 "aa"}])
	"(?<g>a)\\k<g>"                "ab"         nil
	"(?<g>.)(?<h>.)\\k<h>\\k<g>"   "abba"       (just [{:g "a" :h "b" :0 "abba"}])
	"(?<g>.)(?<h>.)\\k<h>\\k<g>"   "abab"       nil
	"(?<g>a)\\\\k<g>"              "a\\k<g>"    (just [{:g "a" :0 "a\\k<g>"}])
	"(?<g>a)\\\\\\k<g>"            "a\\a"       (just [{:g "a" :0 "a\\a"}])
	"(?<g>a)\\\\\\k<g>"            "a\\b"       nil
	"(?<g>[a-z])(?<h>[0-9])"       "a1 b2 c3"   (just [{:g "a" :h "1" :0 "a1"} {:g "b" :h "2" :0 "b2"} {:g "c" :h "3" :0 "c3"}]))

(tabular "named group detection"
	(fact
		(named-re.re/re-seq (named-re.re/re-pattern ?re) ?s) => ?r)
	?re                            ?s           ?r
	"(?:<g>abc)"                   "<g>abc"     (just ["<g>abc"])             ;non-capturing group; no named groups in this pattern
	"\\(?<g>a\\)"                  "a"          nil                           ;literal left paren, not start of group
	"\\\\(?<g>a)"                  "\\a"        (just [{:g "a" :0 "\\a"}]))   ;literal backslash followed by named group

(tabular "normal replacements work as usual"
	(fact
		(named-re.string/replace ?string (named-re.re/re-pattern ?pattern) ?replacement) => (clojure.string/replace ?string (clojure.core/re-pattern ?pattern) ?replacement))
	?string   ?pattern    ?replacement
	"abc"     "a"         "z"
	"abc"     "a(.)(.)"   "a$2$1"
	"ab"      "a(.)"      "\\$1a"      ;literal $ followed by a 1
	"ab"      "a(.)"      "\\\\$1a")   ;literal backslash followed by group 1

(tabular "named replacements work as expected"
	(fact
		(named-re.string/replace ?string (named-re.re/re-pattern ?pattern) ?replacement) => ?result)
	?string   ?pattern            ?replacement   ?result
	"abc"     "a(?<g>.)(?<h>.)"   "a${h}${g}"    "acb"
	"ab"      "a(?<g>.)"          "\\${g}a"      "${g}a"   ;literal $ followed by {g}
	"ab"      "a(?<g>.)"          "\\\\${g}a"    "\\ba")   ;literal backslash followed by group g