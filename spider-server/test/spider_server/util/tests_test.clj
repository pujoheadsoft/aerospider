(ns spider-server.util.tests-test
  (:require [clojure.test :refer :all]
            [spider-server.util.tests :refer :all]))

(defn join [{:keys [name value]} {:keys [id]}]
  (str id ":" name ":" value))

(defn include-sharp
  [v]
  (str "#" v "#"))

(defn exec-join
  [map1 map2]
  (->> (join map1 map2)
       (include-sharp)
       (str "Joined: ")))

#_(clojure.pprint/pprint
 (macroexpand-1 '(with-redefs-fn-test [(join {:name "" :value ""} {:id ""}) => "X"
                                    (include-sharp "HiHiHi") => "Y"]
                  (exec-join {} {}))))

(deftest test
  (testing "テストその1"
    (with-redefs-fn-test [(join {:name "Name" :value "Value"} {:id "ID"}) => "X"
                       (include-sharp "X") => "%Z%"]
      (is (= (exec-join {:name "Name" :value "Value"} {:id "ID"})
             "Joined: %Z%")))))
