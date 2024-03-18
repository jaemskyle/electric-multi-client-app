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

#?(:clj
   (def config {:manifest-path "/admin/js/manifest.edn" ; contains Electric compiled program's version so client and server stays in sync
                :index-page "/admin/index.html"
                :js-path "/admin/js"
                :on-boot-server (fn [ring-request]
                                  (e/boot-server {} Main ring-request))}))

