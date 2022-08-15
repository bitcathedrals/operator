;----------------------------------------------------------------------
; converse.clj
; written by: Mike Mattie (2011)
;----------------------------------------------------------------------
(ns operator.converse
  (:use compojure.core)

  (:require
    [clojure.string     :as string]

    [operator.language  :as lang]
    [operator.view      :as view]
    [operator.interface :as interface]
    [operator.userdb    :as userdb]
    [operator.session   :as session]
    [operator.simulator :as simulate]
    [operator.appdb     :as db]) )

(defn- try-to-authenticate [ from message ]
  (let
    [parse (lang/analysis-engine message
             {"password" (lang/compile-extractor #"^password" lang/arity-none lang/arity-* nil)
              "is"       (lang/compile-extractor #"^is"       lang/arity-none lang/arity-* {:keyword ["password"]}) })]

    (if (lang/extract-right parse "password")
      (userdb/authenticate-for-number from (string/join " " (lang/extract-right parse "password")))
     (if (lang/extract-right parse "is")
        (userdb/authenticate-for-number from (string/join " " (lang/extract-right parse "is")))
        (userdb/authenticate-for-number from (string/join " " (lang/extract-input parse))) ))
    ))

(defn- login [ from ]
  (do
    (session/set-authenticated! from)

    ["You have logged in ! Please tell me what I can do for you."
     (session/add-message!
       from
       "*password censored*"
       "You have logged in ! Please tell me what I can do for you."
       (interface/add (apply view/logged-in-welcome (userdb/first-and-last-name from))))] ))

(defn- transfer [ from message ]
  (let
    [account-name (lang/one-of (db/accounts-for-user (userdb/user-id-for-number from)))

     parse (lang/analysis-engine message
             {"transfer" (lang/compile-extractor #"^transfer" lang/arity-none lang/arity-none nil)
              "from"     (lang/compile-extractor #"^from"     account-name    account-name    nil)
              "to"       (lang/compile-extractor #"^to"       account-name    account-name    nil)
              "account"  (lang/compile-extractor "^account"   lang/arity-none lang/arity-none {:keyword ["from" "to"]}) }) ]

    (if (lang/extract-keyword parse "transfer")
      (do
        (println "I got a transfer parse: " parse)
        )
      false)
    ))


(defn- user-interface [ from message ]
  (let
    [uri (session/add-message! from message (str "You said: " message) (interface/add (simulate/echo message)))]

    (do
      (transfer from message)
      [(str "You Said: " message) uri])
  ))

(defn analyze [ from message ]
  (if (session/new-conversation? from)

    (do
      (session/add-session! from)
      (if (try-to-authenticate from message)
        (login from)
        ["you must login by sending or saying your passphrase."
          (session/add-message!
            from
            message
            "you must login by sending or saying your passphrase."
            nil)
          ]))

    (if (session/authenticated? from)
      (user-interface from message)
      (if (try-to-authenticate from message)
        (login from)

        ["The passphrase is wrong or I was unable to find the passphrase in your message. Please try again."
          (session/add-message!
            from
            message
            "The passphrase is wrong or I was unable to find the passphrase in your message. Please try again."
            nil)]
      )) ))
