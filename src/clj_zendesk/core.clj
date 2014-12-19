(ns clj-zendesk.core
  (:require [clj-http.client :refer [request]]
            [clj-zendesk.util :refer [map-all-keys]]
            [inflections.core :refer [singular plural capitalize]]
            [cemerick.url :as url]
            [camel-snake-kebab.core :refer [->kebab-case ->snake_case
                                            ->CamelCase]]
            [cheshire.core :refer [generate-string parse-string]]))

(def ^:dynamic auth-creds [])
(def ^:dynamic api-url "")

(defn setup [subdomain email token]
  (def auth-creds [(str email "/token") token])
  (def api-url (format "https://%s.zendesk.com/api/v2/" subdomain)))

(def kebabify-map (partial map-all-keys ->kebab-case))
(def underscorify-map (partial map-all-keys ->snake_case))

(defn format-url
  "Creates a URL out of end-point and positional. URL encodes the elements of
   positional and then formats them in."
  [end-point positional]
  (str api-url (apply format end-point (map url/url-encode positional))))

(defn make-request [method end-point positional query]
  (let [req (merge-with merge
                        {:url (format-url end-point positional)
                         :basic-auth auth-creds
                         :accept :json
                         :content-type :json
                         :method method})
        proper-query (dissoc (underscorify-map query) :auth)
        req (if (#{:post :put :delete} method)
              (assoc req :body (generate-string proper-query))
              (assoc req :query-params proper-query))]
    req))

(defn api-call
  ([method end-point] (api-call method end-point nil {}))
  ([method end-point positional] (api-call method end-point positional {}))
  ([method end-point positional query]
     (let [req (make-request method end-point positional (or query {}))]
       (-> req request
               :body
               (parse-string true)
               kebabify-map))))


(defprotocol StandardOperations
  (get-all [_ ])
  (get-one [_ id])
  (create  [_ data])
  (update  [_ id data])
  (delete  [_ id]))

(defn base-url [resource-name]
  (-> resource-name
      name
      ->snake_case))

(defrecord Resource [resource-name]
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


(defmacro defresource [resource-name]
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

