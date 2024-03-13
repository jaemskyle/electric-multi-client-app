(ns admin.events;client server
  (:require
   [promesa.core :as p]
   [hyperfiddle.electric :as e]
   #?(:clj [xtdb.api :as xt])
   [clojure.pprint :refer [pprint]]
   [clojure.set :as set]
   #?(:clj [lib.xtdb :as xtlib])
   [admin.subs :as subs]
   [clojure.data :as data])
  (:import
   #?(:clj [java.time Instant])))

;-- UI event handlers

#?(:clj (e/def db)); server only

(e/defn On-dump-db [_]
  (e/server
   (let [;history (xt/entity-history @xtlib/!xtdb #uuid "f6c616a0-7a69-4a0a-9ea0-bbe46ed0e37e" :desc)
         ]
     ;(xt/submit-tx @xtlib/!xtdb [[:xtdb.api/delete #uuid "702835bf-b2de-4cd4-8adb-9f94b9c60533"]])     
     ;(xt/submit-tx @xtlib/!xtdb [[:xtdb.api/evict #uuid "702835bf-b2de-4cd4-8adb-9f94b9c60533"]])
     (xt/sync @xtlib/!xtdb)
     (e/client
      (pprint {:people (e/server (subs/People.))});
      ))))



