(ns flux-master.component.bulb-scanner
  (:require [clojure.core.async :refer [>! >!! go go-loop chan close! alts! timeout]]
            [com.stuartsierra.component :as component]
            [flux-led.core :as led]))

(defn- setup-scan-chan [component scanned-bulbs-fn]
  (let [c (chan)
        delay 5000]
    (go-loop [[v _] (alts! [c (timeout delay)])]
      (when (not= v :stop)
        (do (scanned-bulbs-fn component (led/scan))
            (recur (alts! [c (timeout delay)])))))
    c))

(defrecord BulbScanner [scanned-bulbs-fn]
  component/Lifecycle

  (start [component]
    (if-not (:scan-chan component)
      (assoc component :scan-chan (setup-scan-chan component scanned-bulbs-fn))
      component))

  (stop [component]
    (if-let [scan-chan (:scan-chan component)]
      (do
        (>!! scan-chan :stop)
        (close! scan-chan)
        (dissoc component :scan-chan))
      component)))

(defn bulb-scanner [scanned-bulbs-fn]
  (->BulbScanner scanned-bulbs-fn))
