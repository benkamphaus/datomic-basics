(ns user
  (:require [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/mbrainz-1968-1973")

(def conn (d/connect db-uri))

(d/q '[:find [?a ?name]
       :where
       [?a :artist/name ?name]]
     (d/db conn))





