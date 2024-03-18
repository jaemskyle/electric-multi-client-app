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

#?(:clj
   (def config {:manifest-path "/app/js/manifest.edn" ; contains Electric compiled program's version so client and server stays in sync
                :index-page "/app/index.html"
                :js-path "/app/js";main-fn prefix in index.html
                :on-boot-server (fn [ring-request]
                                  (e/boot-server {} Main ring-request))}))

