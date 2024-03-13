(ns prod
  (:require
   #?(:clj [clojure.edn :as edn])
   #?(:clj [clojure.java.io :as io])
   #?(:clj [clojure.tools.logging :as log])
   [contrib.assert :refer [check]]
   app.main
   #?(:clj [server.server-jetty :as jetty])
   [hyperfiddle.electric :as e])
  #?(:cljs (:require-macros [prod :refer [compile-time-resource]])))

(defmacro compile-time-resource [filename] (some-> filename io/resource slurp edn/read-string))

(def config
  (merge
    ;; Client program's version and server program's versions must match in prod (dev is not concerned)
    ;; `src-build/build.clj` will compute the common version and store it in `resources/electric-manifest.edn`
    ;; On prod boot, `electric-manifest.edn`'s content is injected here.
    ;; Server is therefore aware of the program version.
    ;; The client's version is injected in the compiled .js file.
   (doto (compile-time-resource "electric-manifest.edn") prn)
   {:host "localhost", :port 8080,
    :resources-path "public/app"
     ;; shadow build manifest path, to get the fingerprinted main.sha1.js file to ensure cache invalidation
    }))

(def app-config
  {:resources-path "public"
   :manifest-path ; contains Electric compiled program's version so client and server stays in sync
   "public//app/js/manifest.edn"
   :index-page "/app/index.html"
   :asset-path "/app/js"
   :on-boot-server (fn [ring-request]
                     (e/boot-server {} app.main/Main ring-request))})

(def admin-config
  {:resources-path "public"
   :manifest-path ; contains Electric compiled program's version so client and server stays in sync
   "public//admin/js/manifest.edn"
   :index-page "/admin/index.html"
   :asset-path "/admin/js"
   :on-boot-server (fn [ring-request]
                     (e/boot-server {} admin.main/Main ring-request)); 
   })

;;; Prod server entrypoint

#?(:clj
   (defn -main [& {:strs [] :as args}] ; clojure.main entrypoint, args are strings
     (log/info (pr-str config))
     (check string? (::e/user-version config))
     (jetty/start-server!
      config
      app-config
      admin-config)))

;;; Prod client entrypoint

#?(:cljs
   (do
     (def app-entrypoint (e/boot-client {} app.main/Main nil))
     (def admin-entrypoint (e/boot-client {} app.main/Main nil))
     (defn ^:export start! []
       (app-entrypoint
        #(js/console.log "app reactor success:" %)
        #(js/console.error "app reactor failure:" %))
       (admin-entrypoint
        #(js/console.log "admin reactor success:" %)
        #(js/console.error "admin reactor failure:" %)))))

