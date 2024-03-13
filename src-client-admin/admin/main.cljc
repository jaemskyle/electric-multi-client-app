(ns admin.main
  (:require [hyperfiddle.electric :as e]
            [admin.ui.views :as views]
            #?(:clj [lib.xtdb :as xtlib])
            [admin.events :as events]
            [admin.subs :as subs]))

(e/defn Main [ring-request]
  (e/server
   (xtlib/start-xtdb!)
   (let [db (new (xtlib/latest-db> @xtlib/!xtdb))]
     (binding [events/db db
               subs/db db]
       (views/Root.)))))

