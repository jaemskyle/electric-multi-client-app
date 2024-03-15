(ns server-dev
  (:require
   app.main
   admin.main
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

  (def app-config {:resources-path "public"
                   :manifest-path ; contains Electric compiled program's version so client and server stays in sync
                   "public//app/js/manifest.edn"
                   :index-page "/app/index.html"
                   :asset-path "/app/js"
                   :on-boot-server (fn [ring-request]
                                     (e/boot-server {} app.main/Main ring-request))})

  (def admin-config {:resources-path "public"
                     :manifest-path ; contains Electric compiled program's version so client and server stays in sync
                     "public//admin/js/manifest.edn"
                     :index-page "/admin/index.html"
                     :asset-path "/admin/js"
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
      (.stop server))))


