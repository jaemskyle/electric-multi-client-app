(ns lib.utils
  (:import
   [java.time Instant LocalTime LocalDate ZoneId ZonedDateTime]))

(defn utc-for-time-of [h m]
  (let [today (LocalDate/now)
        local-time (LocalTime/of h m)
        zone-id (ZoneId/systemDefault)
           ;; Combine the date and time into a LocalDateTime
        local-date-time (ZonedDateTime/of today local-time zone-id)
           ;; Convert to an Instant for the UTC timestamp
        utc-instant (.toInstant local-date-time)]
    utc-instant))