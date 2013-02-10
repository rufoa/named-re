(defproject named-re "1.0.0"
	:description "Named group support for regular expressions"
	:url "https://github.com/rufoa/named-re"
	:license {:name "Eclipse Public License"
	          :url "http://www.eclipse.org/legal/epl-v10.html"}
	:dependencies [[org.clojure/clojure "1.4.0"] [reader-macros "1.0.1"]]
	:profiles {:dev {:dependencies [[midje "1.5-alpha10"]] :plugins [[lein-midje "2.0.4"]]}})