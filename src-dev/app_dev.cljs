(ns app-dev
  (:require
   app.main
   [hyperfiddle.electric :as e]))

(do
  (defonce reactor nil)

  (defn ^:dev/after-load ^:export start!
    ;specified in shadow-cljs.edn
    []
    (set! reactor ((e/boot-client {} app.main/Main nil)
                   #(js/console.log "reactor success:" %)
                   #(js/console.error "reactor failure:" %))))

  (defn ^:dev/before-load stop! []
    (when reactor (reactor)) ; stop the reactor
    (set! reactor nil)))


