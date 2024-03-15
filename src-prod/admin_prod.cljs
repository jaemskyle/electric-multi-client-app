(ns admin-prod
  (:require
   [hyperfiddle.electric :as e]
   admin.main))

(do
  (def entrypoint (e/boot-client {} admin.main/Main nil))

  (defn ^:export start! []
    (entrypoint
     #(js/console.log "reactor success:" %)
     #(js/console.error "reactor failure:" %))))

