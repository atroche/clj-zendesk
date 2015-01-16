# clj-zendesk

A client for the [Zendesk REST API](https://developer.zendesk.com/rest_api/docs/core/introduction). PRs, feature requests and bug reports very welcome!

Check out this [extensively annotated source code / documentation](http://atroche.github.io/clj-zendesk).

It currently only supports the following operations:

   * ```create```
   * ```get``` (aka “show”)
   * `get-all` (aka “list”)
   * `delete`

…on the following resources: tickets, views, ticket fields, users, macros, automations, triggers, targets, user fields and groups.

Don't worry, more resources and endpoints are coming soon — this is mainly a one-hundred-lines-of-code, hacked-together-in-an-afternoon proof-of-concept. But there's still lots of stuff you could build with it.

The library takes care of turning your idiomatic Clojure data (e.g. ```{:custom-field {:id 20 :value 13}}```) into the format that the API is expecting (e.g. ```{"custom_field": {"id": 20, "value": 13}}```). Your app doesn't need to deal with JSON or hyphens-vs-underscores or any of those boring things!

Currently only supports auth-ing via OAuth token.

## Installation

Just add ```[clj-zendesk "0.1.0"]``` to `:dependencies` in your project's `profile.clj`.

## Usage

```clojure
user> (use 'clj-zendesk.core)
user> (setup "zendesk_subdomain" "zendesk_email@zendesk.com" "YOURAPITOKEN")
user> (get-all Users)
[{:role "end-user", :updated-at "2013-11-29T00:16:30Z", :tags [], :email "end@user.com", :chat-only false,  … etc.


user=> (def my-new-ticket (create Ticket {:subject "Help me!" :comment {:body "my app is broken!"} }))
{:description "my app is broken!", :updated-at "2014-12-19T08:48:11Z", :assignee-id nil, :tags [], :custom-fields [{:id 24146696, :value nil} {:id 24099383, :value nil} {:id 24056003, :value nil}], :group-id 21407398, :via {:channel "api", :source {:from {}, :to {}, :rel nil}}, :has-incidents false, :fields [{:id 24146696, :value nil} {:id 24099383, :value nil} {:id 24056003, :value nil}], :recipient nil, :type nil, :organization-id 29244208, :sharing-agreement-ids [], :requester-id 533561168, :external-id nil, :satisfaction-rating nil, :priority nil, :status "new", :id 12, :problem-id nil, :ticket-form-id nil, :url "https://zendesk_subdomain.zendesk.com/api/v2/tickets/12.json", :collaborator-ids [], :raw-subject "Help me!", :created-at "2014-12-19T08:48:11Z", :subject "Help me!", :forum-topic-id nil, :due-at nil, :submitter-id 533561168}

user=> (:id my-new-ticket)
12

user=> (get-one Ticket 12)
{:description "my app is broken!", :updated-at "2014-12-19T08:48:11Z", :assignee-id nil, :tags [], :custom-fields [{:id 24146696, :value nil} {:id 24099383, :value nil} {:id 24056003, :value nil}], :group-id 21407398, :via {:channel "api", :source {:from {}, :to {}, :rel nil}}, :has-incidents false, :fields [{:id 24146696, :value nil} {:id 24099383, :value nil} {:id 24056003, :value nil}], :recipient nil, :type nil, :organization-id 29244208, :sharing-agreement-ids [], :requester-id 533561168, :external-id nil, :satisfaction-rating nil, :priority nil, :status "new", :id 12, :problem-id nil, :ticket-form-id nil, :url "https://zendesk_subdomain.zendesk.com/api/v2/tickets/12.json", :collaborator-ids [], :raw-subject "Help me!", :created-at "2014-12-19T08:48:11Z", :subject "Help me!", :forum-topic-id nil, :due-at nil, :submitter-id 533561168}
```

Rinse and repeat for Users, TicketFields, Macros, etc., etc.

## License

Copyright © 2014-2015 Alistair Roche

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
