;----------------------------------------------------------------------
; view.clj
; written by: Mike Mattie
;----------------------------------------------------------------------
(ns operator.view
  (:use [compojure.core] [clojure.contrib.str-utils])

  (:require
    [hiccup.core :as render] ))

(defn logged-in-welcome [first-name last-name]
  (fn []
    (render/html
      [:html
        [:head
          [:title "Operator Logged In"]]

        [:body
          [:h1 "Welcome to Operator!"]
          [:p (str first-name " " last-name " has successfully logged in")]
          ]])) )



