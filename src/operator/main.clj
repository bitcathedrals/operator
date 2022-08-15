;----------------------------------------------------------------------
; main.clj
; written by: Mike Mattie (2011)
;----------------------------------------------------------------------
(ns operator.main
  (:gen-class))

(require 'operator.core)
(require 'swank.swank)

(defn -main [& args]
  (do
    (println "---------> Welcome to Operator! <-----------------")
    (println "I am going to start the server on port 8000")
    (operator.core/start-server)

    (println "I am going to start the Swank Server ... Jack In!")
    (swank.swank/start-repl) ))
