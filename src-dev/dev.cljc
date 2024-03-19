(ns dev
  (:require
   app.main
   admin.main
   #?(:clj [server.server-jetty :as jetty])
   #?(:clj [shadow.cljs.devtools.api :as shadow])
   #?(:clj [shadow.cljs.devtools.server :as shadow-server])
   #?(:clj [clojure.tools.logging :as log])))

(comment
  (-main)) ; repl entrypoint

#?(:clj
   (do
     (def server-config  {:host "localhost"
                          :port 8080})

     (defn -main [& args]
       (log/info "Starting Electric compiler and server...")

       (shadow-server/start!)
       (shadow/watch :app-dev)
       (shadow/watch :admin-dev)
       (comment (shadow-server/stop!))

       (def server (jetty/start-server!
                    server-config
                    app.main/config
                    admin.main/config)))))

(comment
  (do
    (.stop server)
    (-main)))

#?(:cljs
   (do
     (defonce app-reactor nil)

     (defn ^:dev/after-load ^:export start-app!
       ;specified in shadow-cljs.edn
       []
       (set! app-reactor (app.main/entrypoint
                          #(js/console.log "reactor success:" %)
                          #(js/console.error "reactor failure:" %))))

     (defn ^:dev/before-load stop-app! []
       (when app-reactor (app-reactor)) ; stop the reactor
       (set! app-reactor nil))))

#?(:cljs
   (do
     (defonce admin-reactor nil)

     (defn ^:dev/after-load ^:export start-admin!
       ;specified in shadow-cljs.edn
       []
       (set! admin-reactor (admin.main/entrypoint
                            #(js/console.log "reactor success:" %)
                            #(js/console.error "reactor failure:" %))))

     (defn ^:dev/before-load stop-admin! []
       (when admin-reactor (admin-reactor)) ; stop the reactor
       (set! admin-reactor nil))))