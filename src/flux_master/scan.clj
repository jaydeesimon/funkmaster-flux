(ns flux-master.scan
  (:require [flux-master.db :as db]
            [clojure.core.async :refer [chan]]))

(defn- upsert-bulb! [db {:keys [id ip] :as bulb}]
  (if (nil? (db/get-bulb db id))
    (db/insert-bulb db bulb)
    (db/update-bulb db {:ip ip :id id})))

(defn- add-bulb-chan! [bulb-chans {:keys [id]}]
  (swap! bulb-chans
         (fn [bulb-chans id]
           (if (not (get bulb-chans id))
             (assoc bulb-chans id (chan))
             bulb-chans))
         id))

(defn with-scanned-bulbs [component bulbs]
  (let [db (-> component :db :spec)
        bulb-chans (-> component :bulb-chans-comp :bulb-chans)]
    (do (run! (partial upsert-bulb! db) bulbs)
        (run! (partial add-bulb-chan! bulb-chans) bulbs))))

