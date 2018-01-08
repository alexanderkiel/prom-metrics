(defproject prom-metrics "0.2-SNAPSHOT"
  :description "Clojure Wrappers for the Prometheus Java Client."
  :url "https://github.com/alexanderkiel/prom-metrics"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.6.0"
  :pedantic? :abort

  :dependencies [[io.prometheus/simpleclient "0.1.0"]
                 [io.prometheus/simpleclient_hotspot "0.1.0"]
                 [io.prometheus/simpleclient_common "0.1.0"]
                 [org.clojure/clojure "1.8.0"]]

  :profiles {:dev {:dependencies [[criterium "0.4.4"]]}
             :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}}

  :aliases
  {"lint" ["eastwood" "{:linters [:all]}"]})
