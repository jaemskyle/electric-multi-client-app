(ns app.main
  (:require [hyperfiddle.electric :as e]
            [app.ui.views :as views]
            #?(:clj [lib.xtdb :as xtlib])
            [app.events :as events]
            [app.subs :as subs]))

(e/defn Main [ring-request]
  (e/server
   (xtlib/start-xtdb!)
   (let [db (new (xtlib/latest-db> @xtlib/!xtdb))
         user-key (e/client (events/Watch$. (events/$user-key)))]
     (binding [events/db db
               subs/db db
               events/user-key user-key
               subs/user-key user-key]
       (when user-key
         (e/client
          (binding [events/user-key user-key
                    subs/user-key user-key]
            (views/Root.))))))))

#?(:cljs
   (def entrypoint (e/boot-client {} Main nil)))

#?(:clj
   (def config {:manifest-path "/app/js/manifest.edn" ; contains Electric compiled program's version so client and server stays in sync
                :index-page "/app/index.html"
                :js-path "/app/js";main-fn prefix in index.html
                :on-boot-server (fn [ring-request]
                                  (e/boot-server {} Main ring-request))}))

