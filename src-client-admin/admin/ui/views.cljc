(ns admin.ui.views
  (:require
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as d]
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

(e/defn Toggle [label & [{:keys [checked? On-change]}]]
  (e/client
   (d/input
    (d/props {:type :checkbox
              :class "mdl-checkbox__input"
              :checked checked?})
    (d/on "change" (e/fn [e]
                     (and On-change
                          (On-change. e)))))
   (d/label
    (d/props {:class "mdl-checkbox"})
    (d/span
     (d/props {:class "mdl-checkbox__label"})
     (d/text label)))))

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
         filter-str (e/watch !filter-str)
         !signed-in? (atom false)
         signed-in? (e/watch !signed-in?)]
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
        (d/div
         (d/text (if signed-in?
                   "signed-in:"
                   "sign-in events today:")))
        (d/br)
        (d/div
         (e/for-by :xt/id
                   [{:keys [full-name event-type]} (new (if signed-in?
                                                          subs/Events-signed-in-filtered
                                                          subs/Events-today-filtered) filter-str)]
                   (d/div
                    (d/text full-name " - " (name event-type)))))
        (d/br)
        (d/div
         (d/props {:class "mdl-card__actions mdl-card--border"
                   :style {:display :flex
                           :align-items :center
                           ;:flex-direction :column
                           }})
         (Input. "filter names" {:On-input (e/fn [text]
                                             (reset! !filter-str (str/upper-case text)))})
         (Toggle. "signed-in" {:checked? signed-in?
                               :On-change (e/fn [_] (swap! !signed-in? not))})
         (Button. "Dump" {:On-click events/On-dump-db})))))))

