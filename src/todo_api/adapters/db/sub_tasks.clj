(ns todo-api.adapters.db.sub-tasks)

(defprotocol ISubTasksOps
  (create! [_ sub-task])
  (get-by-id [_ id])
  (get-all [_])
  (update! [_ sub-task])
  (delete! [_ id])
  (history [_ id]))
