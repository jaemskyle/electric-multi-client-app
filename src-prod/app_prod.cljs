(ns app-prod
  (:require
   [hyperfiddle.electric :as e]
   app.main))

(do
  (def entrypoint (e/boot-client {} app.main/Main nil))
  
  (defn ^:export start! []
    (entrypoint
     #(js/console.log "reactor success:" %)
     #(js/console.error "reactor failure:" %))
    ))

