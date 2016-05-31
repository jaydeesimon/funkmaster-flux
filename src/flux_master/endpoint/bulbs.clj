(ns flux-master.endpoint.bulbs
  (:require [compojure.core :refer :all]
            [ring.util.response :refer [response]]))

(defn bulbs-endpoint [{{db :spec} :db}]
  (context "/api/1" []
    (GET "/bulb/:id" [id]
      id)

    (GET "/bulbs" []
      (response {:hello "there" 1 [2 3 4 5 6 "yeah"]}))

    (POST "/bulb/:id/rgb" [id :as request]
      (let [{:keys [body]} request]
        (response (get-in body ["hello"]))))))