(ns flux-master.endpoint.example
  (:require [compojure.core :refer :all]))

(defn example-endpoint [{{db :spec} :db}]
  (context "/example" []
    (GET "/" []
      (println db)
      "This is an example endpoint")))
