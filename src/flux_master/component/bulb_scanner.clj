(ns flux-master.component.bulb-scanner
  (:require [clojure.core.async :refer [>! go go-loop chan close! alts! timeout]]
            [com.stuartsierra.component :as component]
            [flux-master.db.bulbs :as db]
            [flux-led.core :as led]))

(defn- new-bulbs [db-bulbs scanned-bulbs]
  (let [ids (set (map :id db-bulbs))]
    (remove #(ids (:id %)) scanned-bulbs)))

(defn- offline-bulbs [db-bulbs scanned-bulbs]
  (let [ids (set (map :id scanned-bulbs))]
    (remove #(ids (:id %)) db-bulbs)))

;; TODO: Clean this up! Lots of ugly code in here.
(defn bulb-scan-chan [{{db :spec} :db :as component} new-bulbs-fn offline-bulbs-fn all-bulbs-fn]
  (let [c (chan)]
    (go-loop []
      (let [[v _] (alts! [c (timeout 5000)])]
        (if (not= v :shutdown)
          (let [db-bulbs (db/all-bulbs db {} {} {:row-fn db/convert-bools})
                scanned-bulbs (led/scan)]
            (do (new-bulbs-fn component (new-bulbs db-bulbs scanned-bulbs))
                (offline-bulbs-fn component (offline-bulbs db-bulbs scanned-bulbs))
                (all-bulbs-fn component db-bulbs scanned-bulbs)
                (recur)))
          (close! c))))
    c))

(defrecord BulbScanner [new-bulbs-fn offline-bulbs-fn all-bulbs-fn]
  component/Lifecycle

  (start [component]
    (if-not (:scan-chan component)
      (assoc component :scan-chan (bulb-scan-chan component new-bulbs-fn offline-bulbs-fn all-bulbs-fn))
      component))

  (stop [component]
    (if-let [scan-chan (:scan-chan component)]
      (do
        (go (>! scan-chan :shutdown))
        (dissoc component :scan-chan))
      component)))

(defn bulb-scanner [new-bulbs-fn offline-bulbs-fn all-bulbs-fn]
  (->BulbScanner new-bulbs-fn offline-bulbs-fn all-bulbs-fn))
