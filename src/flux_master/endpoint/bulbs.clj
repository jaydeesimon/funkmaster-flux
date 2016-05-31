(ns flux-master.endpoint.bulbs
  (:require [compojure.core :refer :all]
            [ring.util.response :refer [response not-found]]
            [flux-master.db.bulbs :as db]))

(defn bulbs-endpoint [{{db :spec} :db}]
  (context "/api/1" []
    (GET "/bulb/:id" [id]
      (if-let [bulb (db/get-bulb db {:id id})]
        (response bulb)
        (not-found {:message (str "Bulb " id " not found.")})))

    (GET "/bulbs" []
      (response (db/all-bulbs db)))

    (POST "/bulb/:id/rgb" [id :as {{rgb :rgb} :body}]
      (response [id rgb]))

    (POST "/bulb/:id/white" [id :as {{percent :percent} :body}]
      (response [id percent]))

    (POST "/bulb/:id" [id :as {{:keys [description]} :body}]
      (response {:id id :description description}))))