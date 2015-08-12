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


(def ^:dynamic defaults {:domain    "zendesk.com"
                         :subdomain nil
                         :email     nil
                         :token     nil})

(defmacro with-defaults
  [options & body]
  `(binding [defaults ~options]
     ~@body))

(defn format-url
  "Creates a URL out of end-point and positional-args (i.e. values to put in the URL).
   URL encodes the elements of positional-args and then formats them in.

  E.g. `(format-url \"/users/%s.json\" [3]) returns \"/users/3.json\""
  [end-point positional-args]
  (let [{:keys [subdomain domain]} defaults
        api-url
        (format "https://%s.%s/api/v2/" subdomain domain)]
    (str api-url (apply format end-point (map url/url-encode positional-args)))))

(defn make-request
  "Prepares a map representing a request for passing into `clj-http.client/request`
  in our `api-call` function.

  Combines the passed in URL args and form data with some basic defaults for headers."
  [{:keys [method end-point positional-args query url]}]
  (let [{:keys [email token]} defaults
        auth-creds [(str email "/token") token]
        req {:url          (or url
                               (format-url (str end-point ".json") positional-args))
             :basic-auth   auth-creds
             :accept       :json
             :content-type :json
             :method       (or method :get)}
        underscorified-query (underscorify-map query)
        form-or-query-params (if (#{:post :put :delete} method)
                               :form-params
                               :query-params)
        req (assoc req form-or-query-params underscorified-query)]
    (-> req request
        :body
        (parse-string true)
        kebabify-map)))


(defn api-call
  "Actually make the HTTP request.

  Parses the JSON in the response body and turns the underscored keys
  back into kebab-case."
  ([method end-point] (api-call method end-point nil {}))
  ([method end-point positional-args] (api-call method end-point positional-args {}))
  ([method end-point positional-args query]
   (make-request {:method          method
                  :end-point       end-point
                  :positional-args positional-args
                  :query           (or query {})})))


(defprotocol StandardOperations
  "Many resources have a standard range of things you can do with them (CRUD, basically)."
  (get-all [_])
  (get-one [_ id])
  (create-one [_ data])
  (update-one [_ id data])
  (delete-one [_ id]))

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

;; I haven't implemented `update` yet because I forgot and haven't had time!
(defrecord Resource
  [resource-name]
  StandardOperations

  (get-all [_]
    (let [initial-response (api-call :get (base-url resource-name))
          ; better with letfn?
          handle-pagination (fn handle-pages [resp]
                              (let [next-page (:next-page resp)
                                    resources ((plural resource-name) resp)]
                                (if next-page
                                  (lazy-cat resources
                                            (handle-pages (make-request {:url next-page})))
                                  resources)))]
      (handle-pagination initial-response)))
  (get-one [_ id]
    ((singular resource-name) (api-call :get (str (base-url resource-name) "/%s") [id])))
  (create-one [_ data]
    ((singular resource-name) (api-call :post
                                        (base-url resource-name)
                                        nil
                                        {(singular resource-name) data})))
  (delete-one [_ id]
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


(defmacro defresources
  "Takes a bunch of resource names and does defresource on all of them.

  E.g. `(defresource :tickets :users)` becomes `(do (defresource :tickets) (defresource :users))`"
  [& args]
  `(do
     ~@(for [resource args]
         `(defresource ~resource))))


;; Define a bunch of resources. These are ones where standard operations Just Work™
;; out of the box. There are probably more, I just haven't bothered to check and add them :)

;; Good news is that if there's one you want to use you can just do `(defresource :my-new-resource)` and you'll be able to e.g. `(get-all MyNewResources)`. Or you could just add it to the list below and make a pull request =)
(defresources :tickets
              :views
              :ticket-fields
              :users
              :macros
              :automations
              :triggers
              :targets
              :user-fields
              :groups)

