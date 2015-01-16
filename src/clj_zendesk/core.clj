;; ## A Zendesk API client for Clojure

;; The Zendesk has a fairly massive and complex API. It can be hard to get your head around
;; and use. There's a brilliant client for Ruby, but what about for the Clojure community?
;; clj-zendesk is the answer.

;; ## A warning

;; This project is still new and incomplete. It supports basic functionality at this point,
;; but still doesn't even support a majority of the endpoints.

;; ## Credits

;; Inspiration for the design of the API call mechanism comes from Raynes' excellent tenctacles library.

(ns clj-zendesk.core
  (:require [clj-http.client :refer [request]]
            [clj-zendesk.util :refer [map-all-keys kebabify-map underscorify-map]]
            [inflections.core :refer [singular plural capitalize]]
            [cemerick.url :as url]
            [camel-snake-kebab.core :refer [->snake_case ->CamelCase]]
            [cheshire.core :refer [generate-string parse-string]]))

(defn setup
  "Tells the client which account you want to query against, and sets up the basic-auth
  headers used by all API calls.

  This isn't a great solution. It introduces global state and makes many of the functions
  in this library even *less* pure than they could be. Other projects like the GitHub API
  client tentacles pass the auth-creds into every API call. I'd like to get the best of
  both worlds at some stage. Suggestions welcome =)"

  [subdomain domain email token]
  (def auth-creds [(str email "/token") token])
  (def api-url (format "https://%s.%s/api/v2/" subdomain domain)))


(defn format-url
  "Creates a URL out of end-point and positional-args (i.e. values to put in the URL).
   URL encodes the elements of positional-args and then formats them in.

  E.g. `(format-url \"/users/%s.json\" [3]) returns \"/users/3.json\""
  [end-point positional-args]
  (str api-url (apply format end-point (map url/url-encode positional-args))))

(defn make-request
  "Prepares a map representing a request for passing into `clj-http.client/request`
  in our `api-call` function.

  Combines the passed in URL args and form data with some basic defaults for headers."
  [method end-point positional-args query]
  (let [req {:url (format-url (str end-point ".json") positional-args)
             :basic-auth auth-creds
             :accept :json
             :content-type :json
             :method method}
        underscorified-query (underscorify-map query)
        form-or-query-params (if (#{:post :put :delete} method)
                               :form-params
                               :query-params)]
    (assoc req form-or-query-params underscorified-query)))


(defn api-call
  "Actually make the HTTP request.

  Parses the JSON in the response body and turns the underscored keys
  back into kebab-case."
  ([method end-point] (api-call method end-point nil {}))
  ([method end-point positional-args] (api-call method end-point positional-args {}))
  ([method end-point positional-args query]
     (let [req (make-request method end-point positional-args (or query {}))]
       (-> req request
               :body
               (parse-string true)
               kebabify-map))))


(defprotocol StandardOperations
  "Many resources have a standard range of things you can do with them (CRUD, basically)."
  (get-all [_ ])
  (get-one [_ id])
  (create  [_ data])
  (update  [_ id data])
  (delete  [_ id]))

(defn base-url
  "Takes in e.g. :users and returns \"users\". Or :ticket-fields and
  returns \"ticket_fields\".

  So we can refer to resources using keywords but then use those when resolving endpoints."
  [resource-name]

  (-> resource-name
      name
      ->snake_case))

;; Represents an API resource like “tickets” or “users”.
;;
;; Implements the standard operations which, thanks to the fairly consistent
;; API means that we can reuse the same basic code for CRUD operations
;; across a wide range of resources.

;; `get-all` corresponds to a GET on the root of a resource (e.g. /api/v2/users.json)

;; `get-one` corresponds to a GET on a specific resource (e.g. /api/v2//users/3.json)

;; `create` corresponds to a POST on the root of a resource (e.g. /api/v2/users.json)

;; `delete` corresponds to a GET on a specific resource (e.g. /api/v2//users/3.json)
(defrecord Resource
  [resource-name]
  StandardOperations

  (get-all [_]
    ((plural resource-name) (api-call :get (base-url resource-name))))
  (get-one [_ id]
    ((singular resource-name) (api-call :get (str (base-url resource-name) "/%s") [id])))
  (create [_ data]
    ((singular resource-name) (api-call :post
                                        (base-url resource-name)
                                        nil
                                        {(singular resource-name) data})))
  (delete [_ id]
    (api-call :delete (str (base-url resource-name) "/%s") [id])))


(defmacro defresource
  "Takes e.g. :ticket-fields and will create the following `Resources`: TicketField and TicketFields. This is so we can do (get-one TicketField <id>) as well as (get-all TicketFields), which just makes the whole thing read nice.
  "
  [resource-name]
  (let [resource-symbol# `~(-> resource-name
                               name
                               ->CamelCase
                               symbol)
        singular-symbol# `~(singular resource-symbol#)]
    `(do
       (def ~resource-symbol#
         (->Resource ~resource-name))
       (def ~singular-symbol#
         ~resource-symbol#))))


;; Define a bunch of resources. These are ones where standard operations Just Work™
;; out of the box. There are probably more, I just haven't bothered to check and add them :)

;; Good news is that if there's one you want to use you can just do `(defresource :my-new-resource)` and you'll be able to e.g. `(get-all MyNewResources)`.

;; I want to create a `defresources` macro to DRY this up, I just haven't had time.
(defresource :tickets)
(defresource :views)
(defresource :ticket-fields)
(defresource :users)
(defresource :macros)
(defresource :automations)
(defresource :triggers)
(defresource :targets)
(defresource :user-fields)
(defresource :groups)

