(ns flux-master.component.channels
  (:require [clojure.core.async :refer [>! <! <!! go go-loop chan close! alts! timeout]]
            [flux-master.db :refer [all-bulbs]]
            [com.stuartsierra.component :as component]))

(defn- init-listener [c]
  (go-loop []
    (let [v (<! c)]
      (if (vector? v)
        (do (go (>! c :throttle))
            (apply (first v) (rest v)))
        (<! (timeout 2000))))
    (recur))
  c)

(defn- init-bulb-chans [{{db :spec} :db}]
  (let [db-ids (map :id (all-bulbs db))]
    (into {} (map (fn [id] [id (init-listener (chan))]) db-ids))))

(defn- close-bulb-chans! [bulb-chans]
  (doseq [[_ c] bulb-chans]
    (close! c)))

(defrecord BulbChannels []
  component/Lifecycle

  (start [this]
    (if (not (:bulb-chans this))
      (assoc this :bulb-chans (atom (init-bulb-chans this)))
      this))

  (stop [this]
    (if (:bulb-chans this)
      (do (close-bulb-chans! @(:bulb-chans this))
          (dissoc this :bulb-chans))
      this)))

(defn bulb-chans []
  (->BulbChannels))