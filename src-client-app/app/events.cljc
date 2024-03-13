(ns app.events;client server
  (:require
   [hyperfiddle.electric :as e]
   #?(:clj [xtdb.api :as xt])
   [clojure.pprint :refer [pprint]]
   #?(:clj [lib.xtdb :as xtlib])
   [app.subs :as subs])
  (:import
   #?(:clj [java.time Instant])))

;-- UI event handlers

#?(:clj (e/def db)); server only

(e/defn On-add-person [{:as data}]
  (e/server
   ;filter invalid attrs
   (let [data (select-keys data subs/person-keys)]
     (xt/sync @xtlib/!xtdb)
     ;:xt/id is the entity-id
     (xt/submit-tx @xtlib/!xtdb
                   [[:xtdb.api/put
                     (assoc data 
                            :xt/id (random-uuid)
                            :person/timestamp (Instant/now)
                            )]]))))

(e/defn On-dump-db [_]
  (e/server
   (xt/sync @xtlib/!xtdb)
   (e/client
    (pprint {:people (e/server (subs/People.))});
    )))



