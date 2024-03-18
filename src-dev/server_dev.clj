(ns server-dev
  (:require
   app.main
   admin.main
   [clojure.java.io :as io]
   [hyperfiddle.electric :as e]
   [server.server-jetty :as jetty]
   [shadow.cljs.devtools.api :as shadow]
   [shadow.cljs.devtools.server :as shadow-server]
   [clojure.tools.logging :as log]))

(comment
  ;start repl with jack-in and deps.edn. Not deps.edn + shadow-cljs
  ;connect to shadow-cljs repl <ctr><shift>p > <ctr><alt>c ...
  (-main)) ; repl entrypoint

(do
  (def server-config  {:host "localhost"
                       :port 8080})
  (def app-config {:manifest-path (io/resource "app/js/manifest.edn") ; contains Electric compiled program's version so client and server stays in sync
                   :index-page "/app/index.html"
                   :js-path "/app/js";main-fn prefix in index.html
                   :on-boot-server (fn [ring-request]
                                     (e/boot-server {} app.main/Main ring-request))})

  (def admin-config {:manifest-path (io/resource "app/js/manifest.edn") ; contains Electric compiled program's version so client and server stays in sync
                     :index-page "/admin/index.html"
                     :js-path "/admin/js"
                     :on-boot-server (fn [ring-request]
                                       (e/boot-server {} admin.main/Main ring-request)); 
                     })

  (defn -main [& args]
    (log/info "Starting Electric compiler and server...")

    (shadow-server/start!)
    (shadow/watch :app-dev)
    (shadow/watch :admin-dev)
    (comment (shadow-server/stop!))

    (def server (jetty/start-server!
                 server-config
                 app-config
                 admin-config))

    (comment
      (do
        (.stop server)
        (-main)))))



