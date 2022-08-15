(defproject operator "0.0.2"
  :description "a twilio based telephone/sms operator"

  :repositories [["releases" "http://localhost:8080/artifactory/ext-release-local"]]

  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]

                 [compojure "0.6.4"]
                 [lein-ring "0.4.5"]
                 [hiccup "0.3.6"]

                 [clojureql "1.0.0"]
                 [lobos     "0.7.0"]

                 [com.h2database/h2 "1.3.161"]

                 [swank-clojure "1.4.0-SNAPSHOT"]

                 [TwilioJava/TwilioJava "3.0.0"]]

  :dev-dependencies [[postgresql/postgresql "9.1-901.jdbc4"]]
  :main operator.main)
