(ns user
  (:require [datomic.api :as d]))

(comment ;; wrapped in comment just so namespace doesn't all eval on load

 (def db-uri "datomic:dev://localhost:4334/mbrainz-1968-1973")
 (def conn (d/connect db-uri))

 (def artist-names
   (d/q '[:find ?name
          :where
          [_ :artist/name ?name]]
        (d/db conn)))

 (def artist-maps
   (d/q '[:find (pull ?a [*])
          :where
          [?a :artist/name]]
        (d/db conn)))

 (def pink-floyd-id
   (d/q '[:find ?a .
          :where
          [?a :artist/name "Pink Floyd"]]
        (d/db conn)))

 (def db (d/db conn))

 (defn get-artist-id
   [db aname]
   (d/q '[:find ?a .
          :in $ ?aname
          :where
          [?a :artist/name ?aname]]
        db
        aname))

 (pprint
   (d/pull db '[*] pink-floyd-id))

 (d/pull db '[:artist/name :artist/type] pink-floyd-id)

 (d/pull db '[:artist/name {:artist/type [:db/ident]}] pink-floyd-id)

 (def groups
   (d/q '[:find [?a ...]
          :where
          [?a :artist/type :artist.type/group]]
        (d/db conn)))

 (defn same-type-as
   [db artist]
   (d/q '[:find [?a ...]
          :in $ ?base-a
          :where
          [?base-a :artist/type ?art-type]
          [?a :artist/type ?art-type]]
        db
        artist))

 (defn founded
   [db artists]
   (d/q '[:find [?founded ...]
          :in $ [?a ...]
          :where
          [?a :artist/startYear ?founded]]
        db
        artists))

;; hmmm
 (d/pull-many db '[:artist/startYear] (same-type-as db pink-floyd-id))

 (->> (d/datoms db :vaet pink-floyd-id)
      (map (fn [[e a v tx add]]
             [e (d/pull db '[:db/ident] a)  v tx add]))
      (into [])
      (pprint))

 (d/q '[:find [?rname ...]
        :in $ ?artist
        :where
        [?r :release/artists ?artist]
        [?r :release/name ?rname]]
      db
      pink-floyd-id)

;; reverse ref!
 (d/pull db '[{:release/_artists [:release/name]}] pink-floyd-id)

;; what is this schema anyways?
 (->> (d/q '[:find [(pull ?a [:db/ident]) ...]
             :where
             [_ :db.install/attribute ?a]]
           db)
      (map :db/ident)
      (filter #(.contains (str %) "track")))

;; also we could query that
 (d/q '[:find ?ident
        :where
        [_ :db.install/attribute ?a]
        [?a :db/ident ?ident]
        [(str ?ident) ?strident]
        [(.contains ^String ?strident "track")]]
       db)

 (d/pull db '[{:release/_artists [:release/name]}] pink-floyd-id)

 (d/q '[:find (count ?a) .
        :where
        [?a :artist/name]]
      db)

 (->>
  (d/q '[:find [(sample 10 ?a) ...]
         :in $
         :where
         [?a :artist/name]
         [(missing? $ ?a :artist/type)]]
       db)
  (first)
  (d/pull-many db '[*])
  (pprint))


 (def track-results
   (d/q '[:find ?track ?a ?r ?m
          :in $ ?a
          :where
          [?r :release/artists ?a]
          [?r :release/media ?m]
          [?m :medium/tracks ?track]]
       db
       pink-floyd-id))

 (pprint
  (d/pull-many db '[*] (first track-results)))

 (d/q '[:find ?aname
        :in $
        :where
        [?a :artist/name ?aname]
        [(missing? $ ?a :artist/type)]]
      db)

 (d/q '[:find (pull ?tr [:track/name {:track/artists [:artist/name]}])
        :in $
        :where
        [(datomic.api/q '[:find ?track (count ?artists)
                          :in $
                          :where
                          [?track :track/artists ?artists]] $)
         [[?tr ?count]]]
        [(> ?count 3)]]
      db)

 (def sample-q-result
   [[#:track{:name "Fountain of Tears, Part I and II",
             :artists [#:artist{:name "Bobo Stenson"}
                       #:artist{:name "Jan Garbarek"}
                       #:artist{:name "Arild Andersen"}
                       #:artist{:name "Jon Christensen"}
                       #:artist{:name "Terje Rypdal"}]}]
    [#:track{:name "Sart",
             :artists [#:artist{:name "Bobo Stenson"}
                       #:artist{:name "Jan Garbarek"}
                       #:artist{:name "Arild Andersen"}
                       #:artist{:name "Jon Christensen"}
                       #:artist{:name "Terje Rypdal"}]}]
    [#:track{:name "Close Enough for Jazz",
             :artists [#:artist{:name "Bobo Stenson"}
                       #:artist{:name "Jan Garbarek"}
                       #:artist{:name "Arild Andersen"}
                       #:artist{:name "Jon Christensen"}
                       #:artist{:name "Terje Rypdal"}]}]]))

