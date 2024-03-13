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

(def person-keys #{:xt/id :person/first-name :person/last-name :person/gender})

(e/defn People []
  (e/server
   (xt/q db '{:find [(pull ?e [*])]
              :where [[?e :person/last-name]]})))

