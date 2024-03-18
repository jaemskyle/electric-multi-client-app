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
  (-main)) ; repl entrypoint

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
                 admin.main/config))

    (comment
      (do
        (.stop server)
        (-main)))))



