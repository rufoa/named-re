(defproject named-re "1.0.0"
	:description "Named group support for regular expressions"
	:dependencies [[org.clojure/clojure "1.4.0"] [reader-macros "1.0.1"]]
	:profiles {:dev {:dependencies [[lein-midje "2.0.4"] [midje "1.4.0"]]}})