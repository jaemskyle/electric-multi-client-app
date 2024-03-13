(ns lib.xtdb
  (:require
   [xtdb.api :as xt]
   [clojure.java.io :as io]
   [missionary.core :as m]))

(defonce !xtdb (atom nil))

(defn latest-db>
  "return flow of latest XTDB tx, but only works for XTDB in-process mode. see
  https://clojurians.slack.com/archives/CG3AM2F7V/p1677432108277939?thread_ts=1677430221.688989&cid=CG3AM2F7V"
  [xtdb]
  (->> (m/observe (fn [!]
                    (let [listener (xt/listen xtdb {::xt/event-type ::xt/indexed-tx :with-tx-ops? true} !)]
                      #(.close listener))))
       (m/reductions {} (xt/latest-completed-tx xtdb)) ; initial value is the latest known tx, possibly nil
       (m/relieve {})
       (m/latest (fn [{:keys [:xtdb.api/tx-time] :as ?tx}]
                   (prn ::latest-db> ?tx)
                   (if tx-time
                     (xt/db xtdb {::xt/tx-time tx-time})
                     (xt/db xtdb))))))

(defn start-xtdb! [] ; from XTDB’s getting started: xtdb-in-a-box
  (prn 'start-xtdb!)
  (assert (= "true" (System/getenv "XTDB_ENABLE_BYTEUTILS_SHA1"))) ; App must start with this env var set to "true"
  (letfn [(kv-store [dir] {:kv-store {:xtdb/module 'xtdb.rocksdb/->kv-store
                                      :db-dir (io/file dir)
                                      :sync? true}})]
    ;may have been started by another client
    (or @!xtdb
        (let [node (xt/start-node
                    {:xtdb/tx-log (kv-store "data/dev/tx-log")
                     :xtdb/document-store (kv-store "data/dev/doc-store")
                     :xtdb/index-store (kv-store "data/dev/index-store")})]
          (prn :node node)
          (reset! !xtdb node)))))