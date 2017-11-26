(ns spider-server.driver
  (:require [clojure.string :as str])
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
  ([name port]
   (client (host name port)))
  ([^Host host]
   (AerospikeClient. (client-policy) (into-array Host [host]))))

(defn nodes
  [^AerospikeClient client]
  (.getNodes client))

(defn first-node
  [^AerospikeClient client]
  (-> client
      nodes
      first))

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

(defn user-key
  [^Key key]
  (when-let [k (.. key userKey)]
    (.. k getObject)))

(defn do-scan
  [^Key key ^Record record]
  (let [k (user-key key)
        bins (->> (.. record bins)
                  strkeymap->map)]
    {:key k :bins bins}))

(defn scan
  [node namespace set]
  (let [l (atom [])]
    (.scanNode (local-client) (scan-policy) node namespace set (reify ScanCallback
       (scanCallback [this key record]
         (swap! l conj (do-scan key record))))
     (into-array String []))
    @l))

(defn ns-sets
  [namespace sets]
  {:ns namespace
   :sets (->> sets
              (filter #(= namespace (:ns %)))
              (map :set))})

(defn ns-struct
  [namespaces sets]
  (map #(ns-sets % sets) namespaces))

(defn ns-structs->ns-set-list
  [ns-structs]
  (letfn [(entry [namespace set] {:ns namespace :set set})
          (f [namespace sets] (map #(entry namespace %) sets))]
    (->> ns-structs
         (map #(f (:ns %) (:sets %)))
         flatten)))
