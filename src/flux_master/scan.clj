(ns flux-master.scan
  (:require [flux-master.db :as db]
            [clojure.core.async :refer [chan]]))

(defn- upsert-bulbs! [db bulbs]
  (reduce + (map (fn [{:keys [id ip] :as bulb}]
                   (if (not (some? (db/get-bulb db id)))
                     (db/insert-bulb db bulb)
                     (db/update-bulb db {:ip ip :id id})))
                 bulbs)))

(defn- add-bulb-chans! [bulb-chans bulbs]
  (doall (map (fn [{:keys [id]}]
                (swap! bulb-chans
                       (fn [bulb-chans id]
                         (if (not (get bulb-chans id))
                           (assoc bulb-chans id (chan))
                           bulb-chans)) id))
              bulbs)))

(defn with-scanned-bulbs [component bulbs]
  (let [db (-> component :db :spec)
        bulb-chans (-> component :bulb-chans-comp :bulb-chans)]
    (do (upsert-bulbs! db bulbs)
        (add-bulb-chans! bulb-chans bulbs))))

