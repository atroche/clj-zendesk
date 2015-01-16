(ns clj-zendesk.util
  (:require [clojure.walk :refer [postwalk]]
            [camel-snake-kebab.core :refer [->snake_case ->kebab-case]]))

(defn map-all-keys
  "Recursively transforms all map keys from snake_case to kebab-case"
  [f m]
  (letfn [(transform-key [[k v]] [(f k) v])
          (transform-maps [form]
            (if (map? form)
              (into {} (map transform-key form))
              form))]
    (postwalk transform-maps m)))

(def kebabify-map (partial map-all-keys ->kebab-case))
(def underscorify-map (partial map-all-keys ->snake_case))
