(ns admin.ui.views
  (:require
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as d]
   [reagent.core]
   [clojure.string :as str]
   [admin.events :as events]
   [admin.subs :as subs]))

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
   (let [!filter-str (atom "")
         filter-str (e/watch !filter-str)]
     (binding [d/node js/document.body]
       (d/div
        (d/props {:class "mdl-card mdl-shadow--2dp"
                  ;:style {:width "512px"}
                  })
        (d/div
         (d/props {:class :mdl-card__title})
         (d/h2
          (d/props {:class "mdl-card__title-text"})
          (d/text "Admin client")))
        (d/p
         (d/text "people:"))
        (d/div
         (e/for-by :xt/id
                   [{:keys [full-name]} (subs/People-filtered. filter-str)]
                   (d/div
                    (d/text full-name))))
        (d/br)
        (d/div
         (d/props {:class "mdl-card__actions mdl-card--border"
                   :style {:display :flex
                           :align-items :center
                           :flex-direction :column}})
         (Input. "filter names" {:On-input (e/fn [text]
                                             (reset! !filter-str (str/upper-case text)))})
         (Button. "Dump-db" {:On-click events/On-dump-db})))))))

