(ns todo-api.system.db
  (:require [integrant.core :as ig]
            [todo-api.adapters.db.impls.crux :as crux]))

(defmethod ig/init-key :db/impl [_ {:keys [impl config]}]
  (case impl
    :crux (-> (crux/->Crux)
              (.start config))))

(defmethod ig/halt-key! :db/impl [_ db-box]
  (.stop db-box))
