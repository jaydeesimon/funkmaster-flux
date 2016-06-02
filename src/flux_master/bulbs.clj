(ns flux-master.bulbs
  (:require [flux-master.db.bulbs :as bdb]
            [clojure.set :refer [union difference]]
            [clojure.core.async :refer [chan]]))

(defn insert-bulbs [{{db :spec} :db} bulbs]
  (->> (map (partial bdb/insert-bulb db) bulbs)
       (reduce +)))

(defn- update-bulb-state-fn [db online?]
  (fn [{:keys [id ip]}]
    (bdb/update-bulb-state db {:id id :ip ip :online (if online? 1 0)})))

(defn with-offline-bulbs [{{db :spec} :db} bulbs]
  (reduce + (map (update-bulb-state-fn db false) bulbs)))

;; TODO: Clean this up!
(defn update-bulb-chans [{{bulb-chans :bulb-chans} :bulb-chans} db-bulbs scanned-bulbs]
  (let [db-ids (set (map :id db-bulbs))
        scanned-ids (set (map :id scanned-bulbs))
        all-ids (union db-ids scanned-ids)]
    (swap! bulb-chans
           (fn [bulb-chans all-ids]
             (into bulb-chans
                   (map (fn [id]
                          (if (not (get bulb-chans id))
                            [id (chan)]
                            [id (get bulb-chans id)]))
                        all-ids)))
           all-ids)))

(defn with-all-bulbs [{{db :spec} :db :as component} db-bulbs scanned-bulbs]
  (do (update-bulb-chans component db-bulbs scanned-bulbs)
      (reduce + (map (update-bulb-state-fn db true) scanned-bulbs))))