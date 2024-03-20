(ns admin.subs
  (:require
   [hyperfiddle.electric :as e]
   [clojure.string :as str]
   #?(:clj [xtdb.api :as xt])
   #?(:clj [lib.xtdb :as xtlib])
   #?(:clj [lib.utils :as utils]))
  (:import
   #?(:clj [java.time Instant LocalTime LocalDate ZoneId ZonedDateTime])))

;-- UI subscriptions

#?(:clj
   (do
     (e/def db)))

(defn with-full-name [{:keys [person/first-name person/last-name] :as person}]
  (assoc person :full-name (str first-name " " last-name)))

(e/defn People []
  (e/server
   (map with-full-name (map first (xt/q db '{:find [(pull ?e [*])]
                                             :where [[?e :person/last-name]]})))))

(e/defn Events-today []
  (e/server
   (for [[person event time person-id] (xt/q db '{:find [(pull ?ep [*]) ?event-type ?timestamp ?ep]
                                                  :in [from-timestamp]
                                                  :where [[?ep :person/key]
                                                          [?e :event/person ?ep]
                                                          [?e :event/timestamp ?timestamp]
                                                          [?e :event/type ?event-type]
                                                          [(> ?timestamp from-timestamp)]]
                                                  :order-by [[?timestamp :desc]]}
                                             (utils/today-start-time))]
     (assoc (with-full-name person)
            :event-type event
            :pid person-id
            :time time))))

(defn latest-for-person [coll]
  (first (reduce (fn [[c s :as r] {id :pid :as event}]
                   (if (s id) r  [(conj c event) (conj s id)])) [[] #{}] coll)))

(e/defn Events-latest []
  (latest-for-person (Events-today.)))

(e/defn Events-signed-in []
  (filter #(= (:event-type %) :sign-in) (latest-for-person (Events-today.))))

(defn name-filter [filter-str coll]
  (filter
   (fn [{:keys [full-name]}]
     (str/includes? (str/upper-case full-name) filter-str))
   coll))

(e/defn Events-signed-in-filtered [filter-str]
  (name-filter filter-str (Events-signed-in.)))

(e/defn Events-latest-filtered [filter-str]
  (name-filter filter-str (Events-latest.)))

(e/defn Events-today-filtered [filter-str]
  (name-filter filter-str (Events-today.)))

(e/defn People-filtered [filter-str]
  (name-filter filter-str (People.)))




