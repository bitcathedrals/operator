;----------------------------------------------------------------------
; language.clj
; written by: Mike Mattie
;----------------------------------------------------------------------
(ns operator.language
  (:use compojure.core)

  (:require
    [clojure.string     :as string]))

(defstruct extractor
  ; regex,string, or fn
  :keyword

  ; number or fn
  :left-arity
  :right-arity

  ; a conflict map
  :conflict
  )

(def arity-* -1)
(def arity-none nil)

(defn- compile-numbered-arity [ arity ]
  (let
    [target-arity  arity
     current-arity (atom 0)]

    (fn [ word ]
      (if (< @current-arity target-arity)
        (do
          (reset! current-arity (inc @current-arity))
          true)
        false) )))

(defn- compile-arity [ arity ]
  (cond
    (fn? arity)         arity
    (number? arity)     (if (< arity 0)
                          (fn [ word ] true)
                          (compile-numbered-arity arity))
    (string? arity)     (fn [ word ] (re-find (re-pattern arity)  word))
    :else               (fn [ word ] (re-find arity word)) ))

(defn- compile-keyword [ keyword ]
  (cond
    (fn? keyword)       keyword
    (string? keyword)   (fn [ word ] (re-find (re-pattern keyword) word))
    :else               (fn [ word ] (re-find keyword word)) ))

(defn compile-extractor [ keyword left right conflict-map ]
  (let
    [fn-keyword    (compile-keyword keyword)
     fn-left       (if (nil? left)  nil (compile-arity left))
     fn-right      (if (nil? right) nil (compile-arity right))]

    (struct-map extractor :keyword      fn-keyword
                          :left-arity   fn-left
                          :right-arity  fn-right
                          :conflict     conflict-map) ))

(defn- analysis-output [ input runtime rt-meta ]
  (let
    [output (ref {})]

    (do
      (dorun (map
               (fn [ extractor ]
                 (let
                   [array           (runtime extractor)
                    keyword-index   (rt-meta extractor)
                    collect-all     (ref [])
                    collect-left    (ref [])
                    collect-right   (ref [])
                    collect-keyword (ref [])]

                   (do
                     (dorun (map-indexed
                       (fn [ i v ]
                         (if (> v 0)
                           (do
                             (cond
                               (< i keyword-index) (dosync (alter collect-left    conj (nth input i)))
                               (= i keyword-index) (dosync (alter collect-keyword conj (nth input i)))
                               (> i keyword-index) (dosync (alter collect-right   conj (nth input i))) )

                             (dosync (alter collect-all    conj (nth input i))) )
                           ))
                       array))

                     (if (> (count @collect-all) 0)
                       (dosync (alter output conj {extractor {:index   keyword-index
                                                              :all     @collect-all
                                                              :left    @collect-left
                                                              :right   @collect-right
                                                              :keyword @collect-keyword}})) )
                     )))
               (keys runtime)))

      (dosync (alter output conj {"_input" input}))

      (println (str "[analysis-output] output is: " @output))

      @output)))

(defn analysis-engine [ message extractor-table ]
  (let
    [input         (if (string? message) (apply vector (string/split message #"[\t\r\n ]+")) message)
     input-length  (count input)

     runtime     (ref {})
     rt-meta     (ref {})

     do-arity  (fn [ start rt arity-fn bound-fn next-fn]
                 (if arity-fn
                   (let
                    [stopped-at (atom start)]

                     (if (bound-fn start)
                       (loop [ i start ]
                         (if (arity-fn (nth input i))
                           (do
                             (aset-short rt i (inc (aget rt i)))
                             (reset! stopped-at i)

                             (let
                               [ next (next-fn i) ]
                               (if (bound-fn next) (recur next)))
                             ))
                         ))
                     @stopped-at)
                   start))

     do-left-arity   (fn [ start rt arity-fn ]
                       (do-arity start rt arity-fn (fn [ index ] (>= index 0)) dec))

     do-right-arity  (fn [ start rt arity-fn ]
                       (do-arity start rt arity-fn (fn [ index ] (< index input-length)) inc)) ]
    (do
      (println (str "[analysis-engine] input is: " input))
      (println (str "[analysis-engine] I have these extractors: " (keys extractor-table)))

      ; first create the runtime map. The runtime is keyed by the name
      ; of the extractor. The value is an array of shorts sized to the
      ; number of words extracted from the message. The default value
      ; is zero.

      (dorun (map
               (fn [ ex-name ]
                 (dosync (alter runtime conj {ex-name (short-array input-length (short 0))})) )
               (keys extractor-table)))

      ; run the extractors on the input vector of words.
      (dorun (map
        (fn [ ex-name ]
          (let
            ; bind the extractor and the runtime array for conveinance
            [ex  (extractor-table ex-name)
             cf  (ex :conflict)
             rt  (@runtime ex-name)]
            (do
              (println (str "[analysis-engine] running extractor: " ex-name))

              (loop [i 0]
                (let
                  [next (if (and (< i input-length) ((ex :keyword) (nth input i)))
                          (do
                            (println (str "[analysis-engine] got a keyword match [" i "]"))
                            (aset-short rt i (inc (aget rt i)))

                            ; mark where the keyword is in the meta table
                            (dosync (alter rt-meta conj {ex-name i}))

                            ; run keyword conflicts
                            (if (and (map? cf) (contains? cf :keyword))
                              (dorun
                                (map (fn [ cf-ex ]
                                       (aset-short (@runtime cf-ex) i (dec (aget (@runtime cf-ex) i)))
                                       ) (cf :keyword))) )

                            (do-left-arity  (dec i) rt (ex :left-arity))
                            (do-right-arity (inc i) rt (ex :right-arity))
                            (inc i) )
                          (inc i)) ]
                  (if (< next input-length) (recur next)) )) )))
               (keys extractor-table) ))

      (analysis-output input @runtime @rt-meta) )))

;----------------------------------------------------------------------
; extraction helpers
;----------------------------------------------------------------------

(defn extract-input [ parse ]
  (parse "_input"))

(defn extract-keyword [ parse name ]
  (if (contains? parse name)
    (:keyword (parse name)) )
  false)

(defn extract-left [ parse name ]
  (if (contains? parse name)
    (:left (parse name)) ))

(defn extract-right [ parse name ]
  (if (contains? parse name)
    (:right (parse name)) ))

(defn extract-all [ parse name ]
  (if (contains? parse name)
    (:all (parse name)) ))

;----------------------------------------------------------------------
; arity-helpers
;----------------------------------------------------------------------

(defn one-of [ & args ]
  (fn [ word ]
    (let
      [ result (atom false) ]

      (loop [ x (if (seq? (first args)) (first args) args) ]
        (if (= word (first x))
          (reset! result true)
          (when (not (empty? (rest x))) (recur (rest x))) ))

      @result)) )

(defn numerical []
  (one-of
    "one" "two" "three" "four" "five" "six" "seven" "eight" "nine" "ten"
    "eleven" "twelve" "thirteen" "fourteen" "fifteen" "sixteen" "seventeen" "eighteen" "nineteen"
    "twenty" "thirty" "fourty" "fifty" "sixty" "seventy" "eighty" "ninety"
    "hundred" "thousand"))

(defn phrase-engine []

  )
