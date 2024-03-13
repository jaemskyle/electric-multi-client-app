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

#?(:clj (e/def db))

(e/defn People []
  (e/server
   (map (fn [{:keys [person/first-name person/last-name] :as data}]
          (assoc data :full-name (str first-name " " last-name)))
        (map first (xt/q db '{:find [(pull ?e [*])]
                              :where [[?e :person/last-name]]})))))

(e/defn People-filtered [filter-str]
  (filter
   (fn [{:keys [full-name]}]
     (str/includes? (str/upper-case full-name) filter-str))
   (People.)))




