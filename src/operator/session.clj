;----------------------------------------------------------------------
; session.clj
; written by: Mike Mattie
;----------------------------------------------------------------------
(ns operator.session
  (:use compojure.core)
  )

; -> query
; a query is a message sent by the user to the system either by
; voice or SMS.
(defstruct query :body :response :uri)

; -> session
; a session is one or more messages sent by the user to the system.
(defstruct session :state :queries)

(def session-table (ref {}))

(defn sessions []
  (keys @session-table))

(defn add-session! [ from ]
  (do
    (dosync (alter session-table
              conj { from
                     (struct-map session :authenticated (atom 'no) :queries (ref [])) }) )))

(defn messages-for [ from ]
  (deref (-> (@session-table from)
              :queries
           )) )

(defn set-authenticated! [ from ]
  (reset! (-> (@session-table from)
              :authenticated )
          'yes))

(defn authenticated? [ from ]
   (= 'yes (deref (-> (@session-table from)
                      :authenticated)) ))

(defn add-message! [ from message response uri]
  (dosync
    (alter (-> (@session-table from)
               :queries)
      conj (struct-map query :body message :response response :uri uri)))
  uri)

(defn new-conversation? [ from ]
  (not (contains? @session-table from)))
