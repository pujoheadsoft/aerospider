(ns spider-server.response
  (:require [clojure.spec.alpha :as s]
            [ring.util.response :as ring]
            [cheshire.core :as json]))

(def ok 200)
(def not-found 404)
(def error 500)

;(s/fdef json-response
;        :args (s/cat :status #{ok not-found error}
;                     :body (s/or string? map?))
;        :ret map?)

(defn json-response
  [{:keys [status body]}]
  (prn body)
  (-> body
      json/generate-string
      ring/response
      (ring/status status)
      (ring/content-type "application/json; charset=utf-8")))
