;----------------------------------------------------------------------
; userdb.clj
; written by: Mike Mattie 2011
;----------------------------------------------------------------------
(ns operator.userdb
  (:use [clojure.core])
  (require [clojure.string :as string]))

(defstruct user-record :first-name :last-name :passphrase :phone-number)

(def user-table (ref {"15097033231" (struct-map user-record
                                      :first-name   "Heidi"
                                      :last-name    "Grimes"
                                      :passphrase   "doll dolls"
                                      :e-mail       "heidigrimes411@gmail.com"
                                      :phone-number "15097033231"
                                      :user-id 1)

                      "15094817193" (struct-map user-record
                                      :first-name   "Mike"
                                      :last-name    "Mattie"
                                      :passphrase   "code ninja"
                                      :e-mail       "codermattie@gmail.com"
                                      :phone-number "15094817193"
                                      :user-id 2)
                       }))

(defn authenticate-for-number [ number message ]
  (let
    [lookup (@user-table number)]

    (if lookup
      (= message (lookup :passphrase))) ))

(defn user-id-for-number [ number ]
  (:user-id (@user-table number)) )

(defn first-and-last-name [ number ]
  [(:first-name (@user-table number)) (:last-name (@user-table number))] )
