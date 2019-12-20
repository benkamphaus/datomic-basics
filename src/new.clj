(ns new
  (:require [datomic.api :as d]))

(require '[datomic.api :as d])

(def uri "datomic:mem://test1")
(d/create-database uri)
(def conn (d/connect uri))

;; Make two transactions, 11 months apart.
@(d/transact conn [{:db/id (d/tempid :db.part/user)
                    :db/doc "Foo"}
                   {:db/id (d/tempid :db.part/tx)
                    :db/txInstant #inst "2000-01-01"}])

@(d/transact conn [{:db/id (d/tempid :db.part/user)
                    :db/doc "Bar"}
                   {:db/id (d/tempid :db.part/tx)
                    :db/txInstant #inst "2000-12-01"}])

;; When you ask for a database as-of a date that
;; exactly matches a txInstant, you get what you expect.

(d/as-of-t (d/as-of (d/db conn) #inst "2000-01-01"))
;;=> 1000
(:db/txInstant (d/entity (d/db conn) (d/t->tx 1000)))
;;=> #inst "2000-01-01T00:00:00.000-00:00"

(d/as-of-t (d/as-of (d/db conn) #inst "2000-12-01"))
;;=> 1002
(:db/txInstant (d/entity (d/db conn) (d/t->tx 1002)))
;;=> #inst "2000-12-01T00:00:00.000-00:00"

;; But if you ask for an as-of date *between* txInstants,
;; you get a database with an as-of-t value
;; that does not correspond to a transaction.

(d/as-of-t (d/as-of (d/db conn) #inst "2000-01-02"))
;;=> 1001
(:db/txInstant (d/entity (d/db conn) (d/t->tx 1001)))
;;=> nil
