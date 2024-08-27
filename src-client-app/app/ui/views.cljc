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
             :class :mdl-textfield__input
             :value value}
     (d/on "input" (e/fn [e]
                     (and On-input
                          (On-input. (.. e -target -value)))))]))

(e/defn Button [label & [{:keys [On-click disabled]}]]
  (e/client
   #electric-hiccup
    [:button.mdl-button.mdl-js-ripple-effect {:disabled disabled}
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
        [:div.mdl-card.mdl-shadow--2dp
         [:div.mdl-card__title
          [:h2.mdl-card__title-text "App client"]]
         (eval '(prn :eval-2 (+ 1 1)))
         (if signed-in?
           #electric-hiccup
            [:div.mdl-card__actions.mdl-card--border {:style {:display :flex :align-items :center}}
             (Button. "Sign-out" {:On-click (e/fn [_] (events/On-sign-out.))})
             [:br]
             (Button. "Dump-db" {:On-click events/On-dump-db})]
           #electric-hiccup
            [:div
             "Please sign in:" [:br]
             "(sign in a different person from another browser)"
             [:div.mdl-textfield.mdl-js-textfield
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
             [:div.mdl-card__actions.mdl-card--border
              (Button. "Sign-in" {:On-click (e/fn [_] (events/On-sign-in. @!data))
                                  :disabled (not (and first-name last-name))})
              [:br]
              (Button. "Dump-db" {:On-click events/On-dump-db})]])]))))