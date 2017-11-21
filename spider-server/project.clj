(defproject spider-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-RC1"]
                 [compojure "1.6.0"]
                 [ring "1.6.3"]
                 [ring-logger "0.7.7"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-json "0.4.0"]
                 [cheshire "5.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.aerospike/aerospike-client "LATEST"]]
  :plugins [[lein-ring "0.12.1"]]
  :ring {:handler spider-server.handler/app})
