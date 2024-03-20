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
   (let []
     (xt/sync @xtlib/!xtdb)
     (e/client
      (pprint {:people (e/server (subs/People.))});
      ))))



