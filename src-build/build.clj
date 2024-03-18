(ns build
  (:require
   [clojure.tools.build.api :as b]
   [clojure.tools.logging :as log]
   [shadow.cljs.devtools.api :as shadow-api]
   [shadow.cljs.devtools.server :as shadow-server]))

(def electric-user-version (b/git-process {:git-args "describe --tags --long --always --dirty"}))

(defn build-client ; invoke with `clj -X ...`
  "build Electric app client, invoke with -X
  e.g. `clojure -X:build:prod build-client :debug false :verbose false :optimize true`
  Note: Electric shadow compilation requires application classpath to be available, so do not use `clj -T`"
  [argmap]
  (let [config (assoc argmap :hyperfiddle.electric/user-version electric-user-version)
        {:keys [optimize debug verbose] :or {optimize true, debug false, verbose false}} config 
        build-opts {:debug   debug,
                    :verbose verbose,
                    :config-merge
                    [{:compiler-options {:optimizations (if optimize :advanced :simple)}
                      :closure-defines  {'hyperfiddle.electric-client/ELECTRIC_USER_VERSION electric-user-version}}]}
        ]
    (b/delete {:path "resources/public/app/js"}) 
    (b/delete {:path "resources/public/admin/js"})
    (b/delete {:path "resources/electric-manifest.edn"})

    ; bake user-version into artifact, cljs and clj
    (b/write-file {:path "resources/electric-manifest.edn" :content config})

    ; "java.lang.NoClassDefFoundError: com/google/common/collect/Streams" is fixed by
    ; adding com.google.guava/guava {:mvn/version "31.1-jre"} to deps,
    ; see https://hf-inc.slack.com/archives/C04TBSDFAM6/p1692636958361199
    (shadow-server/start!)
    (log/info "Building app client:")
    (as->
     (shadow-api/release :app-prod build-opts)
     shadow-status (assert (= shadow-status :done) "app shadow-api/release error")) ; fail build on error
    (log/info "Building admin client:")
    (as->
     (shadow-api/release :admin-prod build-opts)
     shadow-status (assert (= shadow-status :done) "admin shadow-api/release error")) ; fail build on error
    (shadow-server/stop!)
    (log/info "Client build successful. Version:" electric-user-version)))

(def class-dir "target/classes")

(defn uberjar
  [{:keys [optimize debug verbose ::jar-name, ::skip-client]
    :or {optimize true, debug false, verbose false, skip-client false}
    :as args}]
  ; careful, shell quote escaping combines poorly with clj -X arg parsing, strings read as symbols
  (log/info `uberjar (pr-str args))
  (b/delete {:path "target"})

  (when-not skip-client
    (build-client {:optimize optimize, :debug debug, :verbose verbose}))

  (b/copy-dir {:target-dir class-dir :src-dirs ["src" "src-prod" "src-client-app" "src-client-admin" "resources"]})
  (let [jar-name (or (some-> jar-name str) ; override for Dockerfile builds to avoid needing to reconstruct the name
                     (format "target/electricfiddle-%s.jar" electric-user-version))
        aliases [:prod :app :admin]]
    (log/info `uberjar "included aliases:" aliases)
    (b/uber {:class-dir class-dir
             :uber-file jar-name
             :basis     (b/create-basis {:project "deps.edn" :aliases aliases})})
    (log/info jar-name)))

