;----------------------------------------------------------------------
; simulator.clj
; written by: Mike Mattie
;----------------------------------------------------------------------
(ns operator.simulator
  (:use [clojure.core])

  (:require [hiccup.core         :as render]
            [hiccup.page-helpers :as helper]
            [operator.config     :as config]
            [operator.session    :as session]) )

(defn echo [ message ]
  (fn []
    (render/html
      [:html
        [:head
          [:title "Operator Test Interface!"]]

        [:body
          [:h1 "Welcome to the Matrix!"]
          [:p message]
          ]])) )

(defn interface [ content interface-id phone-number]
  (render/html
    [:html

      [:head
        [:title "Operator Test Interface"]
        (helper/include-css "/css/test-interface.css")]

      [:body
        [:div {:class "heading"}
          [:h1 "Operator Test Interface"]

        [:p "Welcome to operator ! the password for \"15094817193\" is \"code ninja\""]]

        [:div {:class "input"}
          [:form {:method "post" :action "test-interface"}
            [:p
              [:select {:name "test-number"}
                [:option "15094817193"]
                [:option "15097033231"]]

              [:textarea {:name "test-message" :rows "10" :cols "40"}]
              [:input    {:type "submit" :name "action" :value "submit"}] ]]]

        [:div {:class "output"}
          [:table
            [:tr
              [:td {:class "header"} "from number"] [:td {:class "header"} "message"]
              [:td {:class "header"} "response"] [:td {:class "header"} "URL"]]

            (map (fn [ query-struct ]
                   (render/html [:tr [:td "15094817193"]
                                     [:td (query-struct :body)]
                                     [:td (query-struct :response)]
                                     [:td (let
                                            [uri (query-struct :uri)
                                             url (str config/internal-url uri)]

                                            (render/html
                                              (if uri
                                                [:a {:href url} url]
                                                [:p "N/A"])) )] ]))
              (reverse (session/messages-for phone-number)))
            ]]

        ]]))


