(defproject vaultred "0.1.0-SNAPSHOT"
  :description "vault.red server"
  :url "https://vault.red"
  :dependencies [[org.clojure/clojure "1.9.0-alpha12"]
                 [http-kit "2.1.18"]
                 [compojure "1.5.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [org.clojure/data.json "0.2.6"]
                 [environ "1.0.3"]
                 [com.taoensso/timbre "4.7.4"]
                 [org.clojure/core.async "0.2.374"]
                 [buddy "1.1.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [com.taoensso/carmine "2.14.0"]]
  :main ^:skip-aot vaultred.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
