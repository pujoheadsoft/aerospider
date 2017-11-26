(ns spider-server.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :as ring]
            [ring.logger :as logger]
            [spider-server.driver :as as]
            [spider-server.response :refer [json-response ok]]))

(defn information
  [host port]
  (->> (as/client host port)
       as/server-info))

(defn set
  [host port ns-name set-name]
  (-> (as/client host port)
      as/first-node
      (as/scan ns-name set-name)))

(defroutes app-routes
  (GET "/" [] "Spider Server: Version 1")

  (GET "/v1/informations"
       {{:keys [host port] :or {host "localhost" port 3000}} :params}
       (->> {:status ok :body (information host port)}
            json-response))

  (GET "/v1/namespaces/:ns-name/sets/:set-name"
       {{:keys [host port ns-name set-name] :or {host "localhost" port 3000}} :params}
       (->> {:status ok :body {:records (set host port ns-name set-name)}}
            json-response))

  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
      (wrap-reload)
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (wrap-json-params)
      (logger/wrap-with-logger)))
