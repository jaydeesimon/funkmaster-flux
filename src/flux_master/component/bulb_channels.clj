(ns flux-master.component.bulb-channels
  (:require [clojure.core.async :refer [>! <! <!! go go-loop chan close! alts! timeout]]
            [flux-master.db.bulbs :refer [all-bulbs]]
            [com.stuartsierra.component :as component]))

(defn setup-listener [c]
  (go-loop []
    (let [v (<! c)]
      (if (vector? v)
        (apply (first v) (rest v))
        (<! (timeout 2000))))
    (recur))
  c)

(defn- bulb-chans [{{db :spec} :db}]
  (let [db-ids (map :id (all-bulbs db))]
    (into {} (map (fn [id] [id (setup-listener (chan))]) db-ids))))

(defn- close-bulb-chans! [bulb-chans]
  (doseq [[_ c] bulb-chans]
    (close! c)))

(defrecord BulbChannels []
  component/Lifecycle

  (start [component]
    (if (not (:bulb-chans component))
      (assoc component :bulb-chans (atom (bulb-chans component)))
      component))

  (stop [component]
    (if (:bulb-chans component)
      (do (close-bulb-chans! @(:bulb-chans component))
          (dissoc component :bulb-chans))
      component)))

(defn bulb-chans-component []
  (->BulbChannels))