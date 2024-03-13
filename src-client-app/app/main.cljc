(ns app.main
  (:require [hyperfiddle.electric :as e]
            [app.ui.views :as views]
            #?(:clj [lib.xtdb :as xtlib])
            [app.events :as events]
            [app.subs :as subs]))

(e/defn Main [ring-request]
  (e/server
   (xtlib/start-xtdb!)
   (let [db (new (xtlib/latest-db> @xtlib/!xtdb))]
     (binding [events/db db
               subs/db db]
       (views/Root.)))))

