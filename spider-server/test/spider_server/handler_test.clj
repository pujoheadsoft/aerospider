(ns spider-server.handler-test
  (:require [clojure.test :refer :all]
            [spider-server.handler :as h]
            [spider-server.driver :as as]
            [spider-server.util.tests :refer :all]))

(deftest information-test
  (testing "hostとportを基にサーバの情報を取得する"
    (with-redefs-fn-test [(as/client "localhost" 8080) => "client"
                          (as/server-info "client") => "server-info"]
      (is (= (h/information "localhost" 8080)
             "server-info")))))

(deftest set-info-test
  (testing "host, port, namespace名, set名を基にsetの情報を取得する"
    (with-redefs-fn-test [(as/client "localhost" 8080) => "client"
                          (as/first-node "client") => "node"
                          (as/scan "node" "n" "s") => "set-info"]
      (is (= (h/set "localhost" 8080 "n" "s")
             "set-info")))))
