(ns app.events;client server
  (:require
   [promesa.core :as p]
   [hyperfiddle.electric :as e] 
   [app.subs :as subs]
   [clojure.pprint :refer [pprint]]
   #?(:clj [xtdb.api :as xt])
   #?(:clj [lib.xtdb :as xtlib])
   #?(:cljs [lib.localstore :as ls]))
  (:import
   #?(:clj [java.time Instant])))

;-- UI event handlers

(e/defn Watch$
  "Watch a promise. Emits an initial nil."
  [$p]
  (let [!a (atom nil)]
    (-> $p (p/then #(reset! !a %)))
    (e/watch !a)))

#?(:clj (e/def db)); server only
(e/def user-key); server and client

#?(:cljs
   (defn $user-key []
     (p/let [uk (ls/$get-item :user-key)]
       (or uk (ls/$set-item :user-key (str (random-uuid)))))))

(e/defn On-sign-out []
  (e/server
   (when user-key
     (let [user-eid (subs/User-eid.)]
       (xt/submit-tx @xtlib/!xtdb [[:xtdb.api/put
                                    {:xt/id  (random-uuid)
                                     :event/type :sign-out
                                     :event/person user-eid
                                     :event/timestamp (Instant/now);
                                     }]])))))

(e/defn On-sign-in [{:as new-data}]
  (e/server
   ;filter invalid attrs
   (let [new-data (select-keys (assoc new-data
                                      :person/key user-key
                                      ;:person/roles #{:admin}
                                      )subs/person-keys)]
     (assert (= (e/server user-key) (e/client user-key)))
     (xt/sync @xtlib/!xtdb)
     ;:xt/id is the entity-id
     (let [{user-eid :xt/id :as data} (or (subs/Person.) {:xt/id (random-uuid)})
           user-changes (not-empty
                         (keep (fn [k]
                                 (let [n (k new-data)]
                                   (when (not= n (k data))
                                     [k n]))) (keys new-data)))]
       (when user-changes
         ;Always put full entity; missing attrs are removed when overwriting
         (xt/submit-tx @xtlib/!xtdb
                       [[:xtdb.api/put
                         (merge data new-data)]]))
        ;create sign-in event
       (xt/submit-tx @xtlib/!xtdb
                     [[:xtdb.api/put
                       {:xt/id  (random-uuid)
                        :event/type :sign-in
                        :event/person user-eid
                        :event/timestamp (Instant/now);
                        }]])))))

(e/defn On-dump-db [_]
  (e/server
   (xt/sync @xtlib/!xtdb)
   (e/client
    (pprint {:people (e/server (subs/People.))});
    )))



