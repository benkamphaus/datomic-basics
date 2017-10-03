Datomic Basics
================

---

> Dog chases mud balls,
> Lion bites thrower.

--- 

# Database of Facts

- Datomic stores **facts** in a univeral schema.
- this schema is composed of **datoms**
- Datoms take the form "something _about_ something is true/false _as of_ some time"
- or, prefix "I know/believe..."
  - *I like that this suggests an empirical time basis*

--- 

# Datom

Entity, attribute, value

```clojure
[:dog :chases :mud-balls]
```

And a point in time

```clojure
[:dog :chases :mud-balls :today]
```

And assertion or retraction

```clojure
[:dog :chases :mudballs :today true]
```

---

# Entities

```clojure
{:dog/name     "mu"
 :chases       :mud-balls}
 ```

are projected from facts about them

```clojure
[:dog :dog/name "mu"       12 true]
[:dog :chases   :mud-balls 20 true]
```

---

## Develop an Intuition for this!

---

# Entities ...

```clojure
{:db/id    8
 :dog/name "mu"
 :chases   :mud-balls}
```

projected from:

```clojure
[8 :dog/name "mu"]
[8 :chases   :mud-balls]
```
---

# Entities ...

```clojure
{:db/id    8
 :dog/name "mu"
 :chases   :mud-balls}
```

projected from:

```clojure
[8 :dog/name "mu"]
[8 :chases   4]
[4 :db/ident :mud-balls]
```

---

# Entities ...

```clojure
{:db/id    8
 :dog/name "mu"
 :chases   #{:mud-balls :rabbits}}
```

projected from:

```clojure
[8 :dog/name "mu"]
[8 :chases   4]
[8 :chases   7]
[4 :db/ident :mud-balls]
[7 :db/ident :rabbits]
```

---


# Entities ...

```clojure
{:db/id    4
 :db/ident :mud-balls
 :_chases  #{8}}
```

projected from:

```clojure
[8 :dog/name "mu"]
[8 :chases   4]
[8 :chases   7]
[4 :db/ident :mud-balls]
[7 :db/ident :rabbits]
```

---

# Time

```clojure
[8 :dog/name "mu"        1 true]
[8 :chases   4           1 true]
[8 :chases   7           1 true]
[4 :db/ident :mud-balls  1 true]
[7 :db/ident :rabbits    1 true]
[8 :chases   7           2 false]
```

---

Look at time 1:

```clojure
{:dog/name "mu"
 :chases   #{:mud-balls :rabbits}}
```

Look at time 2:

```clojure
{:dog/name "mu"
 :chases   #{:mud-balls}}
```

---

```clojure
[8 :dog/name "mu"        1 true]
[8 :chases   4           1 true]
[8 :chases   7           1 true]  ;; X
[4 :db/ident :mud-balls  1 true]
[7 :db/ident :rabbits    1 true]
[8 :chases   7           2 false]  ;; X
```

Retract cancels the assert, leaving us:

```clojure
[8 :dog/name "mu"        1 true]
[8 :chases   7           1 true]
[4 :db/ident :mud-balls  1 true]
[7 :db/ident :rabbits    1 true]
```

---

# Again, understanding entities<->datoms is crucial!

---

# Assert something

What datom does this add to the database?

```clojure
[:db/add 8 :chases :dreams]
```

---

reorder:

```clojure
[8 :chases :dreams true]
```

Right?

---

Missing:

as of,

```clojure
[8 :chases :dreams 3 true]
```

---

So far:

```clojure
[8 :dog/name "mu"        1 true]
[8 :chases   4           1 true]
[8 :chases   7           1 true]  ;; X
[4 :db/ident :mud-balls  1 true]
[7 :db/ident :rabbits    1 true]
[8 :chases   7           2 false]  ;; X
[8 :chases   9           3 true]
[9 :db/ident :dreams     3 true]
```

---

What was the real assertion?

```clojure
[:db/add 8 :chases 9]
[:db/add 9 :db/ident :dreams]
```

---

or: 

```clojure
[{:db/id "dreams"
  :db/ident :dreams}
 [:db/add 8 :chases "dreams"]]
```

---

# entity<->facts

# map<->list

---

Find the datoms!

```clojure
[{:db/id         "bk"
  :person/name   "Ben"
  :person/age    who-told-you-ive-been-alive-forever
  :person/degree :geography
  :person/children [{:person/name "Sam"
                     :person/age 2}
                    {:person/name "Oliver"
                     :person/age 4}]}
 {:company/name "Cognitect"
  :company/employees ["bk"]}]
```

---

# Ops and Architecture Detour!

Ditributed database:

* transactor writes
* peers read
* storage is independent of either
* new:
  * peer server
  * client
* admin/interact:
  * console
* deprecated:
  * REST (sort of)

---

# Obtaining

* Sign up for license key
* licenses:
  * no time out
  * previous: peer limited, unlimited new releases
  * current: unlimited peers, will have to pay eventually
* Yes, it's proprietary

---

# Deploying

AWS:
  - cloud formation
  - dynamo storage
  - tl;dr this is the happy path

Local/Remote modularity:
  - roll your own
  - e.g. docker compose
  - voltron deployment model
    - maybe a future discussion on this if it pans out

---

# Query

Select:

- query via Datomic's datalog implementation
- low level constructs like `datoms` available

Project entites:

- eagerly to data with `pull`
- lazily as an entity map with `entity`

---

Back to our previous universe:


```clojure
[8 :dog/name "mu"        1 true]
[8 :chases   4           1 true]
[8 :chases   7           1 true]  ;; X
[4 :db/ident :mud-balls  1 true]
[7 :db/ident :rabbits    1 true]
[8 :chases   7           2 false] ;; X
[8 :chases   9           3 true]
[9 :db/ident :dreams     3 true]
```

---

Ask a question:

```clojure
[:find ?e
 :where
 [?e :chases :mud-balls]]
```

Get an answer:

```clojure
#{[8]}
```

---

# Schema

- Datomic enforces constraints with schema.
- Attributes are entities!

```clojure
[8 100 "mu" 1 true]
```

implies:

```clojure
{:db/id          100
 :db/ident       :dog/name
 :db/valueType   :db.type/string
 :db/cardinality :db.cardinality/one}
```

---

# Optimizations

* Datomic stores all data in covering indexes
* Sorts are: `:eavt` `:aevt` `:avet` `:vaet`
* Indexes are shallow trees of segments
 - segments: clumps of datoms

----

To reason about index use, look for known to unknown variable relations:

```clojure
[:find ?e
 :where
 [?e :chases :mud-balls]]
```

- we know `:mud-balls` (value position)
- we know `:chases` (attribute position)

For ref traversal, we use `:vaet`:

```clojure
[:mud-balls :chases 9 1]
```

----

## Let's go interactive!
