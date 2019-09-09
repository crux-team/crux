(ns crux-3df.core
  (:require
   [clojure.tools.logging :as log]
   [clj-3df.core :as df]
   [clj-3df.attribute :as attribute]
   [crux.api :as api]
   [crux.bootstrap :as b]
   [crux.dataflow :as dataflow]
   [crux.io :as cio])
  (:import java.io.Closeable))

(def schema
  {:user/name (merge
               (attribute/of-type :String)
               (attribute/input-semantics :db.semantics.cardinality/one)
               (attribute/tx-time))

   :user/email (merge
                (attribute/of-type :String)
                (attribute/input-semantics :db.semantics.cardinality/one)
                (attribute/tx-time))

   :user/knows (merge
                (attribute/of-type :Eid)
                (attribute/input-semantics :db.semantics.cardinality/many)
                (attribute/tx-time))

   :user/likes (merge
                (attribute/of-type :String)
                (attribute/input-semantics :db.semantics.cardinality/many)
                (attribute/tx-time))})

(defn -main [& args]
  (with-open [crux (api/start-standalone-node
                    {:kv-backend "crux.kv.rocksdb.RocksKv"
                     :event-log-dir "data/eventlog"
                     :db-dir "data/db-dir"})
              crux-3df (dataflow/start-dataflow-tx-listener
                        crux
                        {:crux.dataflow/url "ws://127.0.0.1:6262"
                         :crux.dataflow/schema schema})]

    (api/submit-tx
     crux
     [[:crux.tx/put
       {:crux.db/id 1
        :user/name "Patrik"
        :user/likes ["apples" "bananas"]
        :user/email "p@p.com"}]])

    (api/submit-tx
     crux
     [[:crux.tx/put
       {:crux.db/id 1
        :user/likes ["something new" "change this"]
        :user/name "Patrik"
        :user/knows [3]}]])

    (api/submit-tx
     crux
     [[:crux.tx/put
       {:crux.db/id 2
        :user/name "lars"
        :user/knows [3]}]
      [:crux.tx/put
       {:crux.db/id 3
        :user/name "henrik"
        :user/knows [4]}]])

    (df/exec! (:conn crux-3df)
              (df/query
               (:db crux-3df) "patrik-email"
               '[:find ?email
                 :where
                 [?patrik :user/name "Patrik"]
                 [?patrik :user/email ?email]]))

    (df/exec! (:conn crux-3df)
              (df/query
               (:db crux-3df) "patrik-likes"
               '[:find ?likes
                 :where
                 [?patrik :user/name "Patrik"]
                 [?patrik :user/likes ?likes]]))

    (df/exec! (:conn crux-3df)
              (df/query
               (:db crux-3df) "patrik-knows-1"
               '[:find ?knows
                 :where
                 [?patrik :user/name "Patrik"]
                 [?patrik :user/knows ?knows]]))

    (df/exec! (:conn crux-3df)
              (df/query
               (:db crux-3df) "patrik-knows"
               '[:find ?user-name
                 :where
                 [?patrik :user/name "Patrik"]
                 (trans-knows ?patrik ?knows)
                 [?knows :user/name ?user-name]]
               '[[(trans-knows ?user ?knows)
                  [?user :user/knows ?knows]]
                 [(trans-knows ?user ?knows)
                  [?user :user/knows ?knows-between]
                  (trans-knows ?knows-between ?knows)]]))

    (df/listen!
     (:conn crux-3df)
     :key
     (fn [& data] (log/info "DATA: " data)))

    (df/listen-query!
     (:conn crux-3df)
     "patrik-knows"
     (fn [& message]
       (log/info "QUERY BACK: " message)))))