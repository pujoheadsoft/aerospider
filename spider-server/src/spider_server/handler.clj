(ns spider-server.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :as ring]
            [ring.logger :as logger]))

(defroutes app-routes
  (GET "/" [] "Spider Server")
  (GET "/test1/:id" [id] (str "id = " id))
  (GET "/test2" [id] (str "id = " id))
  (GET "/test3/:id/:value" [id value] (str "id=" id ",value=" value))
  (GET "/test4" request (str request))
  (GET "/test5" [] (fn [r] (ring/response (str r))))
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
      (wrap-reload)
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (wrap-json-params)
      (logger/wrap-with-logger)))
