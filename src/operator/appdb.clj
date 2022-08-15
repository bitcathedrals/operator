;----------------------------------------------------------------------
; appdb.clj
; written by: Mike Mattie
;----------------------------------------------------------------------
(ns operator.appdb
  (:refer-clojure :exclude [distinct conj! compile drop take sort disj!])
  (:use [clojureql.core]))

;; (def query-db {:classname "org.h2.Driver"
;;                :subprotocol "h2"
;;                :subname "mem:appdb"
;;                :auto-commit true})

(def db {:classname "org.postgresql.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/dev"
         :user "mattie"
         :password "what.about.bob"})

(def accounts (table db :accounts))

(defn accounts-for-user [ user-id ]
  (map
    (fn [ tuple ]
      (:name tuple))
    @(->
       (select accounts (where (= :user_id user-id)))
       (project [:name]) )) )
