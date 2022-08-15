;----------------------------------------------------------------------
; loaddb.clj
; written by: Mike Mattie
;----------------------------------------------------------------------
(ns operator.loaddb
  (:refer-clojure :exclude [distinct conj! compile drop take sort disj!])
  (:use [clojureql.core]))

;; (def db {:classname "org.h2.Driver"
;;          :subprotocol "h2"
;;          :subname "mem:appdb"
;;          :auto-commit true})

(def db {:classname "org.postgresql.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/dev"
         :user "mattie"
         :password "what.about.bob"})

(defn populate-accounts-table []
  (let
    [accounts  (table db :accounts)]

    (conj! accounts [{:name "checking" :user_id 1}
                     {:name "savings"  :user_id 1}

                     {:name "checking" :user_id 2}
                     {:name "savings"  :user_id 2}]) ))

