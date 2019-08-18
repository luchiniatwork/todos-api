(ns todo-api.adapters.db.todos)

(defprotocol ITodosOps
  (create! [_ todo])
  (get-by-id [_ id])
  (get-all [_])
  (update! [_ todo])
  (delete! [_ id])
  (history [_ id]))
