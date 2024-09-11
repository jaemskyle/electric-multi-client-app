# Electric multi-client App

A minimal Electric Clojure app based on [Electric Starter App](https://github.com/hyperfiddle/electric-starter-app) providing a server with **multiple independent clients**, routing and an [XTDB-in-a-box](https://v1-docs.xtdb.com/guides/in-a-box/) database. Each client has its own dependencies and electric reactor.

Instructions are provided on how to integrate it into an existing app. For more demos and examples, see [Electric Fiddle](https://github.com/hyperfiddle/electric-fiddle).

### Status

This project is being migrated to use [tailwindcss] and [daisyUI]. These are currently only integrated into the "Dev build".

### HTML Styling
This project uses [tailwindcss] and [daisyUI] for HTML styling in [app/views.cljc] and [admin/views.cljc].
I've included [settings](/.vscode/settings.json) for the [Tailwind CSS IntelliSense] plugin for VSCode to provide class autocomplete and tooltip css definitions for electric-hiccup. If you are using VSCode install the plugin to enable the features.

For more styling information see:
* [tailwindcss-cheatsheet]
* [daisyUI components]
* [electric-hiccup]

## Instructions

Run builds and execute using the [npm scripts](package.json)

Dev build:

* Shell: `npm run watch` *or* Repl: [`(dev/-main)`](src-dev/dev.cljc)
* http://localhost:8080 and http://localhost:8080/admin
* Electric root and configuration functions:
  * [src-client-app/app/main.cljc](src-client-app/app/main.cljc)
  * [src-admin-app/admin/main.cljc](src-admin-app/admin/main.cljc)
* Hot code reloading works: edit -> save -> see app reload in browser

Prod build:

```shell
npm run build-prod-client
npm run prod-server
```

*or to run both in sequence:*

```shell
npm run prod
```

Uberjar:

```shell
npm run build-uber-jar
npm run uber-jar
```

Deployment example: (not tested)
- [Dockerfile](Dockerfile)
- fly.io deployment through github actions: [.github/workflows/deploy.yml](.github/workflows/deploy.yml) & [fly.toml](fly.toml)

## Integrate it in an existing clojure app

1. Look at [src-prod/prod.cljc](src-prod/prod.cljc). It contains:
    - server entrypoint
    - client entrypoint
    - necessary configuration
2. Look at [src/server/server_jetty.clj](src/server/server_jetty.clj). It contains:
   - an example Jetty integration
   - routing
   - required ring middlewares

## Build documentation

Electric Clojure programs compile down to separate clients and server target programs, which are compiled from the same Electric application source code.

* For an Electric client/server pair to successfully connect, they must be built from matching source code. The server will reject mismatched clients (based on a version number handshake coordinated by the Electric build) and instruct the client to refresh (to get the latest javascript artifact).
* [src-build/build.cljc](src-build/build.clj bakes the Electric app version into both client and server artifacts.
  * server Electric app version is baked into `electric-manifest.edn` which is read in [src-prod/prod.cljc](src-prod/prod.cljc).
  * client Electric app version is baked into the .js artifact as `hyperfiddle.electric-client/ELECTRIC_USER_VERSION`

Consequently, you need **robust cache invalidation** in prod!
  * In this example, complied js files are fingerprinted with their respective hash, to ensure a new release properly invalidates asset caches. [index.html](resources/public/app/index.html) is templated with the generated js file name.
  * The generated name comes from shadow-cljs's `manifest.edn` file (in `resources/public/*/js/manifest.edn`). Watch out: this shadow-cljs compilation manifest is not the same manifest as `electric-manifest.edn`!
  * Notice that [src/server/server_jetty.clj](src/server/server_jetty.clj) -> `wrap-index-page` reads `:manifest-path` from config. The config comes from [src-prod/prod.cljc](src-prod/prod.cljc).
  * [src/server/server_jetty.clj](src/server/server_jetty.clj) also provides the uri router to server up the clients.

  [app/views.cljc]: /src-client-app/app/ui/views.cljc
  [admin/views.cljc]: /src-client-admin/admin/ui/views.cljc
  [tailwindcss]: https://tailwindcss.com/
  [daisyUI]: https://daisyui.com/
  [Tailwind CSS IntelliSense]: https://marketplace.visualstudio.com/items?itemName=bradlc.vscode-tailwindcss
  [electric-hiccup]:https://github.com/milelo/electric-hiccup
  [daisyUI components]: https://daisyui.com/components/
  [tailwindcss-cheatsheet]: https://tailwindcsscheatsheet.com/
