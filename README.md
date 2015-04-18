Hi Alistair,

Tweets are too short, and no kidding, I think it is the first time I used tweeter, so I don't know how we can exchange email etc without getting spam latter.

So... I create this PR to talk to you.

"what's your use case? very happy to spend some time improving it if you let me know =)"

The usage I need is probably quite basic at the moment.

I am monitoring a bunch of Cassandra (big database) nodes using Riemann (stream event processor in clojure) to detect anomalies. On anomalies of a specific node / service pair (a service is: cpu usage to high, disk space low etc...), I want the support team to be notified.

At the moment, I have something that looks like:
riemann -> Pagerduty -> Zapier -> Zendesk.
(pagerduty is an alerting system, send you txt messages at night to tell you to get up and fix the problem)
(zapier is a web service that connects webapps with other webapps, pretty cool stuff)

But ideally, I would like something like:

riemann -> pagerduty
and
riemann -> zendesk

Ideally, I would like riemann to keep track of the zendesk ticket id (or whatever is used to identify the zendesk ticket) so that I can append more alerts on the specific nodes in the zendesk ticket (or maybe specific node / service)

To be honest, the ideal support workflow is not yet identified, and I am negociating with the support team a solution. I saw your repo, and I was a bit concerned about the disclaimer of your readme, and the fact that it hasn't been updated in a while, so I was wondering if you could share with me how mature is your work, if you intent to work on it more (I might contribute, but I am a real beginner in clojure, just good enough to use riemann), or maintain it if Zendesk update their API etc...

Do you use it btw? Do you think the current draft of support workflow I mentioned could be supported by clj-zendesk in its current state?

Thanks!

Christophe

