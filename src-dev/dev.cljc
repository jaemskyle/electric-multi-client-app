(ns dev
  (:require
   app.main
   admin.main
   [hyperfiddle.electric :as e]
   [clojure.pprint :refer [pprint]]
   #?(:clj [server.server-jetty :as jetty])
   #?(:clj [shadow.cljs.devtools.api :as shadow])
   #?(:clj [shadow.cljs.devtools.server :as shadow-server])
   #?(:clj [clojure.tools.logging :as log])))

(comment
  ;start repl with jack-in and deps.edn. Not deps.edn + shadow-cljs
  ;connect to shadow-cljs repl <ctr><shift>p > <ctr><alt>c ...
  (-main)) ; repl entrypoint

#?(:clj ;; Server Entrypoint

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
         (.stop server)))))

(comment 
  (let [modules (jetty/get-modules app-config)]
    (pprint modules)))


#?(:cljs ;; Client Entrypoint
   (do

     (defonce app-reactor nil)
     (defonce admin-reactor nil)

     (defn ^:dev/after-load ^:export start!
       ;specified in shadow-cljs.edn
       []
       (when (exists? app.main/Main)
         (set! app-reactor ((e/boot-client {} app.main/Main nil)
                                #(js/console.log "app reactor success:" %)
                                #(js/console.error "app reactor failure:" %))))

       (when (exists? admin.main/Main)
         (set! admin-reactor ((e/boot-client {} admin.main/Main nil)
                              #(js/console.log "admin reactor success:" %)
                              #(js/console.error "admin reactor failure:" %)))))

     (defn ^:dev/before-load stop! []
       (when app-reactor (app-reactor)) ; stop the reactor
       (set! app-reactor nil)
       (when admin-reactor (admin-reactor)) ; stop the reactor
       (set! admin-reactor nil))))


