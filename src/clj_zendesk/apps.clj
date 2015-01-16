(ns clj-zendesk.apps
  (:require [clj-http.client :refer [request post] :as http]
            [clj-zendesk.core :refer [api-call setup auth-creds api-url] :as api]
            [clj-zendesk.util :refer [map-all-keys]]
            [inflections.core :refer [singular plural capitalize]]
            [cemerick.url :as url]
            [camel-snake-kebab.core :refer [->kebab-case ->snake_case
                                            ->CamelCase]]
            [cheshire.core :refer [generate-string parse-string]]))


(defn upload-new-app-package
  "Upload a new zip file package for an app.
  Returns an upload id which can be used to create or update an app."
  [app-zip-path]
  (let [response  (post (str api-url "apps/uploads.json")
        {:basic-auth auth-creds
                        :multipart [{:name "uploaded_data"
                                     :mime-type "application/octet-stream"
                                     :content (clojure.java.io/file app-zip-path)}]})]
    (-> response
        :body
        (parse-string true)
        :id)))

(defn create-app
  "Takes upload ID, enqueues app creation, returns job id"
  [upload-id app-name short-description]
  (let [response (post (str api-url "apps.json")
        {:basic-auth auth-creds
         :body ()})]
    (-> response
        :body
        (parse-string true)
        :id)))

(defn get-job-status
  "Takes upload ID, enqueues app creation, returns job id"
  [job-id]
  (let [response (http/get (str api-url (str "job_statuses/" job-id ".json"))
        {:basic-auth auth-creds})]
    (-> response
        :body
        (parse-string true))))

(api-call :post "apps.json" nil {:name "RQO via API"
                                 :short-description "this is via the API"
                                 :upload-id 5487})

(setup "alistair-pod101" "aroche@zendesk.com" "HAdMqK6wHALLIRaVxxTdvYq6zXiZOrSsKCV0bO5Z")


(api/defresource :apps {:includes-root-element true})


(upload App zip-file)
(create App upload-id)
(update App app-id data)
(get App id)
(get-all Apps)
(delete App id)
(notify App {:event "whatever" :app-id 3})

(api/defsubresource :apps :job-statuses)
(get AppJobStatus job-id)


(api/subresource :apps :installations)

(get-all AppInstallations {:include :app})
(create AppInstallation {:app-id 1 :settings {:name "blah"}})
(get-one AppInstallation 10)
(update AppInstallation 1)
(delete AppInstallation 3)

(api/subresource :apps :installations :job-statuses)
(get-one AppInstallationJobStatus job-id)

(api/subresource :apps :installations :requirements)
(get-all AppInstallationRequirements)
