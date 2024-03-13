(ns app.ui.views
  (:require
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as d]
   [clojure.string :as str]
   [app.events :as events]
   [app.subs :as subs]))

(defn capitalize-words [s]
  (str/join (map str/capitalize (str/split (str s) #"\b"))))

(e/defn Input [label & [{:keys [value On-input]}]]
  (e/client
   (d/input
    (d/props {:type :text
              :placeholder label
              :class "mdl-textfield__input"
              :value value})
    (d/on "input" (e/fn [e]
                    (and On-input
                         (On-input. (.. e -target -value))))))))

(e/defn Button [label & [{:keys [On-click disabled]}]]
  (e/client
   (d/button
    (d/props {:class "mdl-button mdl-js-ripple-effect"
              :disabled disabled})
    (d/text label)
    (d/on "click" On-click))))

(e/defn Root []
  (e/client
   (let [!data (atom {})
         ;watch !data so capitalized inputs are immediately displayed
         {:keys [person/first-name person/last-name]} (e/watch !data)]
     (binding [d/node js/document.body]
       (d/div
        (d/props {:class "mdl-card mdl-shadow--2dp"
                  ;:style {:width "512px"}
                  })
        (d/div
         (d/props {:class :mdl-card__title})
         (d/h2
          (d/props {:class "mdl-card__title-text"})
          (d/text "App client")))
        (d/p
         (d/text "add a name:"))
        (d/div
         (d/props {:class "mdl-textfield mdl-js-textfield"})
         (Input. "first-name"
                 {:value first-name
                  :On-input (e/fn [text]
                              (swap! !data assoc
                                     :person/first-name (-> text capitalize-words not-empty)))})
         (Input. "last-name"
                 {:value last-name
                  :On-input (e/fn [text]
                              (swap! !data assoc
                                     :person/last-name (-> text capitalize-words not-empty)))}))
        (d/div
         (d/props {:class "mdl-card__actions mdl-card--border"
                   :style {:display :flex :align-items :center}})
         (Button. "Add-name" {:On-click (e/fn [_] (events/On-add-person. @!data))
                              :disabled (not (and first-name last-name))})
         (d/br)
         (Button. "Dump-db" {:On-click events/On-dump-db})))))))

