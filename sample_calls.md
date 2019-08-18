$ clojure -m todo-api.main

$ clojure -A:dev
(go)
(halt)
(reset)

$ curl -X POST -i -d '{"foo": "bar from curl"}' -H "Content-Type: application/json" http://localhost:8080/api/todos

$ curl -X GET -i http://localhost:8080/api/todosq
