(ns spider-server.util.tests
  (:require [clojure.test :refer :all]
            [clojure.set :refer [subset?]]))

(defn- exec-do-report
  [fn-name {:keys [def-value symbol expected]}]
  `(do-report {:type :fail,
               :message (str "about in(" ~(str fn-name) ") arg "  ~(str def-value) " isn't expected value."),
               :expected ~expected,
               :actual ~symbol}))

(defmulti gen-test
  (fn [_ {:keys [expected]}]
    (cond
      (coll? expected) :coll
      :else :default)))

(defmethod gen-test :coll
  [fn-name {:keys [def-value symbol expected] :as args}]
  `(if (not (subset? (set ~expected) (set ~symbol)))
     ~(exec-do-report fn-name args)))

(defmethod gen-test :default
  [fn-name {:keys [def-value symbol expected] :as args}]
  `(if (not= ~symbol ~expected)
     ~(exec-do-report fn-name args)))

(defn- gen-fn
  [{:keys [fn-name args return]}]
  (let [symbols (->> args (map :symbol))]
    `(fn [~@symbols]
       ~@(map #(gen-test fn-name %) args)
       ~return)))


(defn- arglists [f] (-> f resolve meta :arglists))

(defn- fn-exection-info
  [fn-exection-map]
  (let [fn-exection (-> fn-exection-map :execution)
        fn-name (-> fn-exection first)
        exection-args (-> fn-exection rest)
        defined-arg-names (->> fn-name
                               arglists
                               (filter #(= (count exection-args) (count %)))
                               first)
        return (-> fn-exection-map :return)
        args-count (-> exection-args count)]
    {:fn-name fn-name
     :args (map
            #(array-map :def-value (nth defined-arg-names %)
                        :expected (nth exection-args %)
                        :symbol (gensym (str "p" %)))
            (range args-count))
     :return return}))

(defmacro with-redefs-fn-test
  [fn-exections & body]
  (let [infos (->> fn-exections
                   (partition 3)
                   (map
                    (fn [[execution operator return]]
                      {:execution execution :operator operator :return return}))
                   (map fn-exection-info))]
    `(with-redefs
       [~@(->> infos
               (map (fn [i] [`~(-> i :fn-name)
                             `~(gen-fn i)]))
               (apply concat))]
       ~@body)))
