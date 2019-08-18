(ns todo-api.adapters.db.impls.crux
  (:require [crux.api :as crux]
            [todo-api.adapters.db.box :refer [IDBBox]]
            [todo-api.adapters.db.todos :refer [ITodosOps]]
            [todo-api.adapters.db.sub-tasks :refer [ISubTasksOps]]))

(defn ^:private rand-uuid []
  (java.util.UUID/randomUUID))

(defn ^:private create! [db-box entry-type entry]
  (let [uuid (rand-uuid)]
    (crux/submit-tx (.conn db-box)
                    [[:crux.tx/put uuid
                      (assoc entry
                             :crux.db/id uuid
                             :todo-api/type entry-type)]])))

(declare ^:private get-by-id)

(defn ^:private assoc-relations
  [db entity relations]
  (cond->> entity
    relations (reduce-kv (fn [m k v]
                           (if (get relations k)
                             (cond-> m
                               (seqable? v) (assoc k (map #(get-by-id db %) v))
                               (not (seqable? v)) (assoc k (get-by-id db v)))
                             m))
                         entity)))

(defn ^:private get-by-id
  ([db id]
   (get-by-id db id nil))
  ([db id relations]
   (let [e (crux/entity db id)]
     (assoc-relations db e relations))))

(defn ^:private get-all
  ([db entry-type]
   (get-all db entry-type nil))
  ([db entry-type relations]
   (let [coll (crux/q db {:find ['?t]
                          :where [['?t :todo-api/type entry-type]]})]
     (map #(let [e (crux/entity db (first %))]
             (assoc-relations db e relations))
          coll))))

(defn ^:private update! [db-box entry-type entry]
  (let [{:keys [:crux.db/id]} entry]
    (crux/submit-tx (.conn db-box)
                    [[:crux.tx/put id
                      (assoc entry
                             :todo-api/type entry-type)]])))

(defn ^:private delete! [db-box id]
  (crux/submit-tx (.conn db-box)
                  [[:crux.tx/delete id]]))

(defn ^:private history [db-box id]
  (let [conn (.conn db-box)
        history (crux/history conn id)]
    (map (fn [{:keys [crux.db/id crux.db/valid-time] :as h}]
           (merge h (crux/entity (crux/db conn valid-time) id)))
         history)))


(defrecord TodosOps [db-box]
  ITodosOps

  (create! [{:keys [db-box]} todo]
    (create! db-box :todo-entry todo))

  (get-by-id [{:keys [db-box]} id]
    (-> db-box
        .conn
        crux/db
        (get-by-id id #{:todo/sub-tasks})))
  
  (get-all [{:keys [db-box]}]
    #_(println db-box)
    (println "aqui")
    (-> db-box
        .conn
        crux/db
        (get-all :todo-entry #{:todo/sub-tasks})))
  
  (update! [{:keys [db-box]} todo]
    (update! db-box :todo-entry todo))
  
  (delete! [{:keys [db-box]} id]
    (delete! db-box id))

  (history [{:keys [db-box]} id]
    (history db-box id)))


(defrecord SubTasksOps [db-box]
  ISubTasksOps

  (create! [{:keys [db-box]} sub-task]
    (create! db-box :sub-task-entry sub-task))

  (get-by-id [{:keys [db-box]} id]
    (-> db-box
        .conn
        crux/db
        (get-by-id id)))
  
  (get-all [{:keys [db-box]}]
    (-> db-box
        .conn
        crux/db
        (get-all :sub-task-entry)))
  
  (update! [{:keys [db-box]} sub-task]
    (update! db-box :sub-task-entry sub-task))
  
  (delete! [{:keys [db-box]} id]
    (delete! db-box id))

  (history [{:keys [db-box]} id]
    (history db-box id)))


(defrecord Crux []
  IDBBox

  (start [this {:keys [type config]}]
    (assoc this :conn (case type
                        :standalone (crux/start-standalone-system config)
                        :clusternode (crux/start-cluster-node config))
           :foo :bar))

  (stop [this]
    (-> this :conn .close))
  
  (conn [this]
    (:conn this))

  (todos-ops [this]
    (->TodosOps this))

  (sub-tasks-ops [this]
    (->SubTasksOps this)))

(comment
  (def inst (.start (Crux.)
                    {:type :standalone
                     :config {:kv-backend "crux.kv.memdb.MemKv"
                              :db-dir "data/db-dir-1"}}))

  (def db (-> inst .conn crux/db))


  (crux/entity db "e032d0cd-8cfd-4b42-ae80-526bbb427c5f")

  (db/disconnect inst)


  (.create! (->TodoOperations) inst {:foo :bar})

  (.create! (->SubTaskOperations) inst {:foo :bar})

  #_(.create! (->SubTaskOperations) inst {:has-parent? true
                                          :todo-api/parent #uuid "ca3683c1-6fc2-431d-833b-cc5742d0b0b6"})

  (.create! (->TodoOperations) inst {:i-have-kids :yes
                                     :todo/sub-tasks #{#uuid "159ea8d7-a90e-42b2-82ed-08ce515975cc"}})
  
  (-> inst .todos-ops .get-all)

  (.get-all (->SubTaskOperations) inst)

  (.get-by-id (->TodoOperations) inst #uuid "e032d0cd-8cfd-4b42-ae80-526bbb427c5f")

  (def e (.get-by-id (->TodoOperations) inst #uuid "e032d0cd-8cfd-4b42-ae80-526bbb427c5f"))

  (.update! (->TodoOperations) inst (assoc e :foo2 :bar2))

  (.delete! (->TodoOperations) inst #uuid "e032d0cd-8cfd-4b42-ae80-526bbb427c5f")

  (clojure.pprint/pprint
   (.history (->TodoOperations) inst #uuid "e032d0cd-8cfd-4b42-ae80-526bbb427c5f"))

  (crux/q db '{:find [?t]
               :where [[?t :crux.db/valid-time #inst "2019-08-17T21:07:33.089-00:00"]
                       [?t :crux.db/id "ef6e7a51a3caf237714bca71d15e50727751f0b4"]]})


  (crux/entity (crux/db (.conn inst) #inst "2019-08-17T21:40:41.682-00:00")
               "e032d0cd-8cfd-4b42-ae80-526bbb427c5f")

  (crux/q db '{:find [?t]
               :where [[?t :crux.db/valid-time #inst "2019-08-17T21:07:33.089-00:00"]
                       [?t :crux.db/id "ef6e7a51a3caf237714bca71d15e50727751f0b4"]]})

  (crux/q db '{:find [?t]
               :where [[?t :crux.db/id _]]})
  
  )
