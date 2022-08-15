;----------------------------------------------------------------------
; initdb.clj
; written by: Mike Mattie
;----------------------------------------------------------------------
(ns operator.initdb
  (:refer-clojure :exclude [alter compile drop bigint boolean char double float time])
  (:use [lobos.connectivity] [lobos.core] [lobos.schema]) )

;; (def db {:classname "org.h2.Driver"
;;          :subprotocol "h2"
;;          :subname "mem:appdb;DB_CLOSE_DELAY=-1"})

(def db {:classname "org.postgresql.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/dev"
         :user "mattie"
         :password "what.about.bob"})

(defn open-db []
  (open-global db))

(defn create-accounts-table []
  (create db
    (table :accounts
      (integer :id   :primary-key :unique :auto-inc)
      (varchar :name 50)
      (integer :user_id :foreign-key) )) )

(defn create-transactions-table []
  (create db
    (table :transactions

      (integer :id :primary-key :unique :auto-inc)

      (varchar :label 50)

      (integer :user_id    :foreign-key)
      (integer :account_id :foreign-key)

      (float   :amount)
      )) )

(defn initdb []
  (do
    (open-db)
    (create-accounts-table)
    (create-transactions-table) ))
