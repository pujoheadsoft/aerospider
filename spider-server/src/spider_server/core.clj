(ns spider-server.core
  (:require [clojure.string :as str]
            [clojure.data.json :as json]
            [cheshire.core :as chesire])
  (:import [java.util HashMap]
           [com.aerospike.client AerospikeClient Host Info Key Record ScanCallback]
           [com.aerospike.client.policy ClientPolicy InfoPolicy ScanPolicy]
           [com.aerospike.client.cluster Node Connection]))

(defn strkeymap->map
  [hashmap]
  (->> hashmap
       (map (fn [m] {(keyword (key m)) (val m)}))
       (apply merge)))

(defn host
  [name port]
  (Host. name port))

(defn client-policy
  []
  (ClientPolicy.))

(defn info-policy
  []
  (InfoPolicy.))

(defn scan-policy
  []
  (ScanPolicy.))

(defn client
  [^Host host]
  (AerospikeClient. (client-policy) (into-array Host [host])))

(defn nodes
  [^AerospikeClient client]
  (.getNodes client))

(defn statistics-map
  [statistics]
  (let [l (str/split statistics #";")]
    (->> l
         (map #(str/split % #"="))
         (into {})
         strkeymap->map)))

(defn features-list
  [features]
  (str/split features #";"))

(defn set-string->set-chunks
  [set-string]
  (str/split set-string #";"))

(defn set-chunks->set-entries-list
  [set-chunks]
  (map #(str/split % #":") set-chunks))

(defn set-entries->set-map
  [set-entries]
  (->> set-entries
       (map #(str/split % #"="))
       (into {})
       strkeymap->map))

(defn sets-string->set-maps
  [sets-string]
  (->> sets-string
       set-string->set-chunks
       set-chunks->set-entries-list
       (map set-entries->set-map)))

(defn ns-string->ns-list
  [ns-string]
  (str/split ns-string #";"))

(defmulti server-info type)

(defmethod server-info Node
  [^Node node]
  (let [m (->> node
               (Info/request (info-policy))
               (into {})
               strkeymap->map)
        set-maps (->> (Info/request (info-policy) node "sets")
                      sets-string->set-maps)
        as-ns (->> (Info/request (info-policy) node "namespaces")
                   ns-string->ns-list)]
    (-> m
        (update-in [:features] features-list)
        (update-in [:statistics] statistics-map)
        (assoc :ns as-ns)
        (assoc :sets set-maps))))

(defmethod server-info AerospikeClient
  [^AerospikeClient ac]
  (->> ac
       nodes
       first
       server-info))

(defn local-client
  []
  (->> 3000
       (host "localhost")
       client))

(def info (server-info (local-client)))

(def n (first (nodes (local-client))))

(defn scan
  [^Key key ^Record record]
  ;(prn (.digest key))
  ;(prn "record: " (strkeymap->map (.bins record)))
  (strkeymap->map (.bins record)))

(defn do-scan
  [node namespace set]
  (let [l (atom [])]
    (.scanNode (local-client) (scan-policy) node namespace set (reify ScanCallback
       (scanCallback [this key record]
         (swap! l conj (scan key record))))
     (into-array String []))
    @l))

(do-scan n "music" "album")

(defn ns-sets
  [namespace sets]
  {:ns namespace
   :sets (->> sets
              (filter #(= namespace (:ns %)))
              (map :set))})

(defn ns-struct
  [namespaces sets]
  (map #(ns-sets % sets) namespaces))

(def struct (ns-struct (:ns info) (:sets info)))

(defn ns-structs->ns-set-list
  [ns-structs]
  (letfn [(entry [namespace set] {:ns namespace :set set})
          (f [namespace sets] (map #(entry namespace %) sets))]
    (->> ns-structs
         (map #(f (:ns %) (:sets %)))
         flatten)))

(map (fn [l] (do-scan n (:ns l) (:set l))) (ns-structs->ns-set-list struct))
