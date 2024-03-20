(ns app.subs
  (:require
   [hyperfiddle.electric :as e]
   #?(:clj [xtdb.api :as xt])
   #?(:clj [lib.xtdb :as xtlib])
   #?(:clj [lib.utils :as utils]))
  (:import
   #?(:clj [java.time Instant LocalTime LocalDate ZoneId ZonedDateTime])))

;-- UI subscriptions

#?(:clj (e/def db)); server only
(e/def user-key); server and client

(def person-keys #{:xt/id :person/first-name :person/last-name :person/key :person/gender})

(e/defn People []
  (e/server
   (xt/q db '{:find [(pull ?e [*])]
              :where [[?e :person/last-name]]})))

(e/defn User-eid []
  (e/server
   (-> (xt/q db '{:find [?e]
                  :in [user-key]
                  :where [[?e :person/key user-key]]} user-key) first first)))

(e/defn Person []
  (e/server
   (-> (xt/q db '{:in [$ user-key]
                  :find [(pull ?e [*])]
                  :where [[?e :person/key user-key]]} user-key) first first)))

(e/defn Events []
  (e/server
   (xt/q db '{:find [(pull ?e [*])]
              :where [[?e :event/type]]})))

(e/defn Signed-in? []
  (e/server
   (first
    (for [[event-type] (xt/q db '{:find [?type ?timestamp]
                                  :in [user-key from-timestamp]
                                  :where [[?ep :person/key user-key]
                                          [?e :event/person ?ep]
                                          [?e :event/timestamp ?timestamp]
                                          [?e :event/type ?type]
                                          [(> ?timestamp from-timestamp)]]
                                  :order-by [[?timestamp :desc]]} user-key (utils/today-start-time))
          :when (#{:sign-in :sign-out} event-type)]
      (= :sign-in event-type)))))


