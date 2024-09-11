(ns app.ui.views
  (:require
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as d]
   [clojure.string :as str]
   [app.events :as events]
   [app.subs :as subs]
   [electric-hiccup.reader]))

(defn capitalize-words [s]
  (str/join (map str/capitalize (str/split (str s) #"\b"))))

(e/defn Input [label & [{:keys [value On-input]}]]
  (e/client
   #electric-hiccup
    [:input {:type :text
             :placeholder label
             :class :input.input-primary.m-1
             :value value}
     (d/on "input" (e/fn [e]
                     (and On-input
                          (On-input. (.. e -target -value)))))]))

(e/defn Button [label & [{:keys [On-click disabled]}]]
  (e/client
   #electric-hiccup
    [:button.btn.m-4 {:disabled disabled}
     (d/text label)
     (d/on "click" On-click)]))

(e/defn Root []
  (e/client
   (let [!data (atom (e/server (subs/Person.)))
         ;watch !data so capitalized inputs are immediately displayed
         {:keys [person/first-name person/last-name]} (e/watch !data)
         signed-in? (e/server (subs/Signed-in?.))]
     (binding [d/node js/document.body]
       #electric-hiccup
        [:div.flex.flex-col.justify-center 
         [:.text-2xl.font-bold "App client"]
         [:br]
         (if signed-in?
           #electric-hiccup
            [:div.flex
             (Button. "Sign-out" {:On-click (e/fn [_] (events/On-sign-out.))})
             (Button. "Dump-db" {:On-click events/On-dump-db})]
           #electric-hiccup
            [:div
             "Please sign in:" [:br]
             "(sign in a different person from another browser)"
             [:div
              (Input. "first-name"
                      {:value first-name
                       :On-input (e/fn [text]
                                   (swap! !data assoc
                                          :person/first-name (-> text capitalize-words not-empty)))})
              (Input. "last-name"
                      {:value last-name
                       :On-input (e/fn [text]
                                   (swap! !data assoc
                                          :person/last-name (-> text capitalize-words not-empty)))})]
             [:br]
             [:div "Number of dependants under 14 (maximum 2 per adult supervisor)"]
             [:input.range.range-primary
              {:type :range :min 0 :max 2 :value 0}]
             [:.flex.w-full.justify-between.px-2
              [:span "0"]
              [:span "1"]
              [:span "2"]]
             [:div
              (Button. "Sign-in" {:On-click (e/fn [_] (events/On-sign-in. @!data))
                                  :disabled (not (and first-name last-name))})
              (Button. "Dump-db" {:On-click events/On-dump-db})]])]))))