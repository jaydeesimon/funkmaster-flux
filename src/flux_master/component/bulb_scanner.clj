(ns flux-master.component.bulb-scanner
  (:require [clojure.core.async :refer [>! go go-loop chan close! alts! timeout]]
            [com.stuartsierra.component :as component]
            [flux-led.core :as led]))

(defn do-with-scan [db bulbs]
  (println db bulbs))

(defn bulb-scan-chan [{{db :spec} :db} f]
  (let [c (chan)]
    (go-loop []
      (let [[v _] (alts! [c (timeout 10000)])
            f (or f identity)]
        (if (not= v :shutdown)
          (do (f db (led/scan))
              (recur))
          (close! c))))
    c))

(defrecord BulbScanner [found-bulbs-fn]
  component/Lifecycle

  (start [component]
    (if-not (:scan-chan component)
      (assoc component :scan-chan (bulb-scan-chan component found-bulbs-fn))
      component))

  (stop [component]
    (if-let [scan-chan (:scan-chan component)]
      (do
        (go (>! scan-chan :shutdown))
        (dissoc component :scan-chan))
      component)))

(defn bulb-scanner [found-bulbs-fn]
  (->BulbScanner found-bulbs-fn))
