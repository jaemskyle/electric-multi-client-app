(ns server-prod
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [contrib.assert :refer [check]]
   [server.server-jetty :as jetty]
   [hyperfiddle.electric :as e]
   app.main
   admin.main))

(defmacro compile-time-resource [filename] (some-> filename io/resource slurp edn/read-string))

(def server-config
  (merge
    ;; Client program's version and server program's versions must match in prod (dev is not concerned)
    ;; `src-build/build.clj` will compute the common version and store it in `resources/public/electric-manifest.edn`
    ;; On prod boot, `electric-manifest.edn`'s content is injected here.
    ;; Server is therefore aware of the program version.
    ;; The client's version is injected in the compiled .js file.
   (doto (compile-time-resource "electric-manifest.edn") prn)
   {:host "localhost"
    :port 8080}))

(def app-config {:manifest-path "/app/js/manifest.edn" ; contains Electric compiled program's version so client and server stays in sync
                 :index-page "/app/index.html"
                 :js-path "/app/js";main-fn prefix in index.html
                 :on-boot-server (fn [ring-request]
                                   (e/boot-server {} app.main/Main ring-request))})

(def admin-config {:manifest-path "/admin/js/manifest.edn" ; contains Electric compiled program's version so client and server stays in sync
                   :index-page "/admin/index.html"
                   :js-path "/admin/js"
                   :on-boot-server (fn [ring-request]
                                     (e/boot-server {} admin.main/Main ring-request))})

(defn -main [& {:strs [] :as args}] ; clojure.main entrypoint, args are strings
  (log/info (pr-str server-config))
  (check string? (::e/user-version server-config))
  (jetty/start-server!
   server-config
   app-config
   admin-config))
