(ns admin.ui.views
  (:require
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as d]
   [clojure.string :as str]
   [admin.events :as events]
   [admin.subs :as subs]))

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

(e/defn Toggle [label & [{:keys [checked? On-change]}]]
  (e/client
   #electric-hiccup
    [:.form-control
     [:label.label.cursor-pointer
      [:span.label-text.m-1 (d/text label)]
      [:input {:type :checkbox
               :class :checkbox.checkbox-primary
               :checked checked?}
       (d/on "change" (e/fn [e]
                        (and On-change
                             (On-change. e))))]]]))

(e/defn Button [label & [{:keys [On-click disabled]}]]
  (e/client
   #electric-hiccup
    [:button.btn.m-4 {:disabled disabled}
     (d/text label)
     (d/on "click" On-click)]))

(e/defn Root []
  (e/client
   (let [!filter-str (atom "")
         filter-str (e/watch !filter-str)
         !signed-in? (atom false)
         signed-in? (e/watch !signed-in?)]
     (binding [d/node js/document.body]
       #electric-hiccup
        [:div.flex.flex-col.justify-center
         [:.text-2xl.font-bold "Admin client"]
         [:br]
         (d/text (if signed-in?
                   "signed-in:"
                   "sign-in events today:"))
         [:br]
         [:div
          (e/for-by :xt/id
                    [{:keys [full-name event-type]} (new (if signed-in?
                                                           subs/Events-signed-in-filtered
                                                           subs/Events-today-filtered) filter-str)]
                    #electric-hiccup
                     [:div
                      (d/text full-name " - " (name event-type))])]
         [:br]
         [:div.flex.items-center
          (Input. "filter names" {:On-input (e/fn [text]
                                              (reset! !filter-str (str/upper-case text)))})
          (Toggle. "signed-in?:" {:checked? signed-in?
                                  :On-change (e/fn [_] (swap! !signed-in? not))})
          (Button. "Dump" {:On-click events/On-dump-db})]]))))