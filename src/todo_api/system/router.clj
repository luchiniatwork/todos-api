(ns todo-api.system.router
  (:require [integrant.core :as ig]
            [reitit.ring :as ring]
            [ring.logger :as logger]
            [ring.middleware.format :as ring-format]
            [ring.middleware.defaults :as defaults]))

(defn ^:private create-handler-todos-get [db-box]
  (fn [req]
    {:status 200
     :body (-> db-box
               .todos-ops
               .get-all)}))

(defn ^:private create-handler-todos-post [db-box]
  (fn [{:keys [body-params] :as req}]
    (if body-params
      (if-let [entity (-> db-box .todos-ops
                          (.create! body-params))]
        {:status 200
         :body entity})
      {:status 400
       :body "Bad request!"})))


(defn ^:private placeholder
  [{:keys [query] :as req}]
  #_(clojure.pprint/pprint query)
  {:status 200
   :body {:msg "Ok from placeholder"}})




(defmethod ig/init-key :router/ring-handler [_ {:keys [db-box]}]
  (ring/ring-handler
   (ring/router
    [["/assets/*" (ring/create-resource-handler {:root "public/"})]
     ["/api/todos" {:get {:handler (create-handler-todos-get db-box)}
                    :post {:handler (create-handler-todos-post db-box)}}]
     ["/api/todos/:id" {:get {:handler placeholder}
                        :post {:handler placeholder}
                        :delete {:handler placeholder}}]
     ["/api/todos/:id/history" {:get {:handler placeholder}}]
     ["/api/sub-tasks" {:get {:handler placeholder}
                        :post {:handler placeholder}}]
     ["/api/sub-tasks/:id" {:get {:handler placeholder}
                            :post {:handler placeholder}
                            :delete {:handler placeholder}}]
     ["/api/sub-tasks/:id/history" {:get {:handler placeholder}}]]
    {:data {:middleware [[logger/wrap-with-logger]
                         [defaults/wrap-defaults defaults/api-defaults]
                         [ring-format/wrap-restful-format
                          :formats [:json-kw
                                    :transit-json
                                    :transit-msgpack
                                    :edn]]]}})
   (constantly {:status 404, :body "Not Found!"})))
