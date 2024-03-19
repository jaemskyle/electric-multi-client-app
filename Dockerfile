FROM clojure:openjdk-11-tools-deps AS build
WORKDIR /app

COPY deps.edn deps.edn
RUN clojure -A:build:prod:app:admin -M -e ::ok   # preload and cache dependencies, only reruns if deps.edn changes

COPY .git .git
COPY shadow-cljs.edn shadow-cljs.edn
COPY src src
COPY src-client-app src-client-app
COPY src-client-admin src-client-admin
COPY src-build src-build
COPY src-prod src-prod
COPY resources resources
COPY node_modules node_modules

RUN clojure -X:build:prod:app:admin uberjar :build/jar-name app.jar

CMD java -cp app.jar clojure.main -m prod