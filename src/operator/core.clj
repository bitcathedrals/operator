;----------------------------------------------------------------------
; core.clj
; written by: Mike Mattie (2011)
;----------------------------------------------------------------------
(ns operator.core
  (:use [compojure.core] [clojure.contrib.str-utils])
  (:require
    [compojure.route :as route]
    [compojure.handler :as handler]

    [ring.adapter.jetty :as jetty]
    [ring.middleware.resource :as ring]
    [ring.util.response :as response]

    [operator.config    :as config]
    [operator.converse  :as conversation]
    [operator.interface :as interface]
    [operator.simulator :as simulator] ))

(defn sms-twilio-debug [ { from-number :From to-number :To :as params } ]
  (do
    (println (str-join "\n" ["SMS message:"
                             (str "these are my params: " params)
                             (str "From phone # " from-number)
                             (str "To phone # " to-number)]) ) ))

(defn sms-twilio-reply [ content interface-id ]
  { :headers {"Content-Type" "text/xml"}
    :body (str
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            "<Response>
              <Sms>"
            (str
              (if interface-id
                (str config/external-url interface-id "\n"))
              content)
              "</Sms>
            </Response>"
            ) } )

(defn sms-twilio-dispatch [ params ]
  (do
    (sms-twilio-debug params)
    (apply sms-twilio-reply
      (conversation/analyze (params :From) (params :Body))) ))

(defn test-debug [ params ]
  (println (str-join "\n" ["test-interface:"
                            (str "these are my params: " params)
                            ])) )

(defn test-dispatch [ params ]
  (test-debug params)
  (apply simulator/interface
    (conj (conversation/analyze (params :test-number) (params :test-message)) (params :test-number))) )

(defroutes static-routes
  (POST "/sms"               {params :params} (sms-twilio-dispatch params))
  (GET  "/"                  {}               (response/redirect "/index.html"))

  (GET  "/interface/:id"     [id]             (interface/render id))

  (POST "/test-interface"    {params :params} (test-dispatch params))

  (route/resources "/")
  (route/not-found "Boink! Page Not Found - are you a sneaky hacker ? bad Monkey !"))

(def uri-server
  (handler/site static-routes))

(defn start-server []
  (doto (Thread. #(jetty/run-jetty #'uri-server {:port 8000})) .start))
