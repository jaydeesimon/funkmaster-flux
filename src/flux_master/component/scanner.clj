(ns flux-master.component.scanner
  (:require [clojure.core.async :refer [>! >!! go go-loop chan close! alts! timeout]]
            [com.stuartsierra.component :as component]
            [flux-led.core :as led]))

(defn- setup-scan-chan [component scanned-bulbs-fn]
  (let [c (chan)
        delay 4000]
    (go-loop [[v _] (alts! [c (timeout delay)])]
      (when (not= v :stop)
        (do (scanned-bulbs-fn component (led/scan))
            (recur (alts! [c (timeout delay)])))))
    c))

(defrecord Scanner [scanned-bulbs-fn]
  component/Lifecycle

  (start [this]
    (if-not (:scan-chan this)
      (assoc this :scan-chan (setup-scan-chan this scanned-bulbs-fn))
      this))

  (stop [this]
    (if-let [scan-chan (:scan-chan this)]
      (do
        (>!! scan-chan :stop)
        (close! scan-chan)
        (dissoc this :scan-chan))
      this)))

(defn bulb-scanner [scanned-bulbs-fn]
  (->Scanner scanned-bulbs-fn))
