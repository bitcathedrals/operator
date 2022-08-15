;----------------------------------------------------------------------
; interface.clj
; written by: Mike Mattie
;----------------------------------------------------------------------
(ns operator.interface
  (:use [compojure.core] [clojure.contrib.str-utils])

  (:require
    [hiccup.core :as render] ))

(defn not-found []
  (render/html
    [:html
      [:head
        [:title "Operator Test Interface!"]]

      [:body
        [:h1 "This interface is no longer active !"]
        ]]))

(def uri-table (ref {"test" (fn []
                              (render/html
                                [:html
                                  [:head
                                    [:title "Operator Test Interface!"]]

                                  [:body
                                    [:h1 "Welcome to the Matrix!"]
                                    ]]))
                      }))

(defn add [ render-fn ]
 (let
   [rnd (new java.util.Random)
    uri (apply str (repeatedly 2 (fn [] (.nextInt rnd))))]

   (do
     (dosync (alter uri-table conj [ uri render-fn ]))
     uri)))

(defn render [ interface ]
  ((get @uri-table interface not-found)))
