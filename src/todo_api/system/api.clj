(ns todo-api.system.api
  (:require [integrant.core :as ig]))




(defmethod ig/init-key :api/query-handler [_ {:keys [db-conn] :as opts}]
  (fn [{:keys [query] :as req}]
    #_(clojure.pprint/pprint query)
    {:status 200
     :body {:msg "Ok from :pathom/query-handler"}}))

(defmethod ig/init-key :api/command-handler [_ {:keys [db-conn] :as opts}]
  (fn [{:keys [command] :as req}]
    #_(clojure.pprint/pprint (:body-params req))
    {:status 200
     :body {:msg "Ok from :pathom/command-handler"}}))
