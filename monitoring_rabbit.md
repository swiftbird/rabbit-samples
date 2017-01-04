
# Monitoring interface for RabbitMQ

# Foreword
This document details a strategy for ensuring a RabbitMQ cluster in _cloud foundry_ is running properly.

  - Detailed documentation for your RabbitMQ instance can be found at https://pivotal-rabbitmq.your.cfinstallation.com/api/
  - The RabbitMQ Manager page is https://pivotal-rabbitmq.your.cfinstallation.com/#/

# Overview
This recipe covers two steps for monitoring RabbitMQ. The first step is a simple "is it running" API which will verify the RabbitMQ cluster is running and able to publish and consume messages from a test queue. The second section details more complex monitoring which can be used for performance analysis, scaling and statistical analysis.

Apart from this help page, all URIs will serve only resources of type application/json, and will require HTTP basic authentication (using the standard RabbitMQ user database). The default user is guest/guest.

Many URIs require the name of a virtual host as part of the path, since names only uniquely identify objects within a virtual host. As the default virtual host is called "/", this will need to be encoded as "%2f".

PUTing a resource creates it. The JSON object you upload must have certain mandatory keys (documented below) and may have optional keys. Other keys are ignored. Missing mandatory keys constitute an error.

Since bindings do not have names or IDs in AMQP we synthesise one based on all its properties. Since predicting this name is hard in the general case, you can also create bindings by POSTing to a factory URI. See the example below.

Many URIs return lists. Such URIs can have the query string parameters sort and sort_reverse added. sort allows you to select a primary field to sort by, and sort_reverse will reverse the sort order if set to true. The sort parameter can contain subfields separated by dots. This allows you to sort by a nested component of the listed items; it does not allow you to sort by more than one field. See the example below.

You can also restrict what information is returned per item with the columns parameter. This is a comma-separated list of subfields separated by dots. See the example below.

Most of the GET queries return many fields per object. See the separate stats documentation.

# Is My RabbitMQ Cluster Up

The simplest approach to verifying a RabbitMQ cluster is _happy_ is to execute the aliveness-test API

| HTTP Method | API Path | Description |
|----|----|----|
| GET | /api/aliveness-test/_host_ |Declares a test queue, then publishes and consumes a message. Intended for use by monitoring tools. If everything is working correctly, will return HTTP status 200 with body: *{"status":"ok"}* Note: the test queue will not be deleted (to to prevent queue churn if this is repeatedly pinged).|

# Detailed Performance Monitoring

The RabbitMQ Admin APIs expose many useful features for performance tuning and application monitoring. In general, the most common elements to monitor are:

  * Nodes -- The individual MQ Server instances in a cluster. This API can be used to determine scaling needs.
  * Connections -- The client connections. Useful for finding (and killing) zombie or harmful client connections.
  * Queues -- The named Queue used by one or more applications. The queues API can be used to detect hotspots or unused queues

**Warning: Many of these APIs expose functionality that can damage or destroy application data in the RabbitMQ system. To avoid unintended issues, it is recommended that only GET methods be used for monitoring.**

| HTTP Method | API Path | Description |
|----|----|----|
| GET |/api/nodes|A list of nodes in the RabbitMQ cluster.|
| GET |/api/nodes/name|An individual node in the RabbitMQ cluster. Add "?memory=true" to get memory statistics, and "?binary=true" to get a breakdown of binary memory use (may be expensive if there are many small binaries in the system).|
| GET |/api/connections	|A list of all open connections.|
| GET/DELETE |/api/connections/name|An individual connection. DELETEing it will close the connection. Optionally set the "X-Reason" header when DELETEing to provide a reason.|
| GET |/api/connections/name/channels|List of all channels for a given connection.|
| GET |/api/queues|A list of all queues.|
| GET |/api/queues/vhost|A list of all queues in a given virtual host.|
| GET/PUT/DELETE |/api/queues/vhost/name	|An individual queue. To PUT a queue, you will need a body looking something like this: {"auto_delete":false,"durable":true,"arguments":{},"node":"rabbit@smacmullen"} All keys are optional. When DELETEing a queue you can add the query string parameters if-empty=true and / or if-unused=true. These prevent the delete from succeeding if the queue contains messages, or has consumers, respectively.|
| GET |/api/queues/vhost/name/bindings	|A list of all bindings on a given queue.|
| DELETE |/api/queues/vhost/name/contents	|Contents of a queue. DELETE to purge. Note you can't GET this.|
| GET |/api/queues/vhost/name/actions	|Actions that can be taken on a queue. POST a body like: {"action":"sync"} Currently the actions which are supported are sync and cancel_sync.|
| GET |/api/queues/vhost/name/get	|Get messages from a queue. (This is not an HTTP GET as it will alter the state of the queue.) You should post a body looking like: {"count":5,"requeue":true,"encoding":"auto","truncate":50000}

* count controls the maximum number of messages to get. You may get fewer messages than this if the queue cannot immediately provide them.
* requeue determines whether the messages will be removed from the queue. If requeue is true they will be requeued - but their redelivered flag will be set.
* encoding must be either "auto" (in which case the payload will be returned as a string if it is valid UTF-8, and base64 encoded otherwise), or "base64" (in which case the payload will always be base64 encoded).
* If truncate is present it will truncate the message payload if it is larger than the size given (in bytes). truncate is optional; all other keys are mandatory. Please note that the get path in the HTTP API is intended for diagnostics etc - it does not implement reliable delivery and so should be treated as a sysadmin's tool rather than a general API for messaging.|


# The Full Monitoring API

This is the entire RabbitMQ API available through PCF

<table>
     <tr>
       <th>GET</th>
       <th>PUT</th>
       <th>DELETE</th>
       <th>POST</th>
       <th>Path</th>
       <th>Description</th>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/overview</td>
       <td>Various random bits of information that describe the whole
       system.</td>
     </tr>
     <tr>
       <td>X</td>
       <td>X</td>
       <td></td>
       <td></td>
       <td class="path">/api/cluster-name</td>
       <td>Name identifying this RabbitMQ cluster.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/nodes</td>
       <td>A list of nodes in the RabbitMQ cluster.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/nodes/<i>name</i></td>
       <td>
         An individual node in the RabbitMQ cluster. Add
         "?memory=true" to get memory statistics, and "?binary=true"
         to get a breakdown of binary memory use (may be expensive if
         there are many small binaries in the system).
       </td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/extensions</td>
       <td>A list of extensions to the management plugin.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td>X</td>
       <td class="path">/api/definitions<br/>
                        /api/all-configuration <em>(deprecated)</em>
       </td>
       <td>
         The server definitions - exchanges, queues, bindings, users,
         virtual hosts, permissions and parameters. Everything apart from
         messages. POST to upload an existing set of definitions. Note
         that:
         <ul>
           <li>
             The definitions are merged. Anything already existing on
             the server but not in the uploaded definitions is
             untouched.
           </li>
           <li>
             Conflicting definitions on immutable objects (exchanges,
             queues and bindings) will cause an error.
           </li>
           <li>
             Conflicting definitions on mutable objects will cause
             the object in the server to be overwritten with the
             object from the definitions.
           </li>
           <li>
             In the event of an error you will be left with a
             part-applied set of definitions.
           </li>
         </ul>
         For convenience you may upload a file from a browser to this
         URI (i.e. you can use <code>multipart/form-data</code> as
         well as <code>application/json</code>) in which case the
         definitions should be uploaded as a form field named
         "file".
       </td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/connections</td>
       <td>A list of all open connections.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td>X</td>
       <td></td>
       <td class="path">/api/connections/<i>name</i></td>
       <td>
         An individual connection. DELETEing it will close the
         connection. Optionally set the "X-Reason" header when
         DELETEing to provide a reason.
       </td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/connections/<i>name</i>/channels</td>
       <td>
         List of all channels for a given connection.
       </td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/channels</td>
       <td>A list of all open channels.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/channels/<i>channel</i></td>
       <td>Details about an individual channel.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/consumers</td>
       <td>A list of all consumers.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/consumers/<i>vhost</i></td>
       <td>A list of all consumers in a given virtual host.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/exchanges</td>
       <td>A list of all exchanges.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/exchanges/<i>vhost</i></td>
       <td>A list of all exchanges in a given virtual host.</td>
     </tr>
     <tr>
       <td>X</td>
       <td>X</td>
       <td>X</td>
       <td></td>
       <td class="path">/api/exchanges/<i>vhost</i>/<i>name</i></td>
       <td>
         An individual exchange. To PUT an exchange, you will need a body looking something like this:
         <pre>{"type":"direct","auto_delete":false,"durable":true,"internal":false,"arguments":{}}</pre>
         The <code>type</code> key is mandatory; other keys are optional.
         <p>
           When DELETEing an exchange you can add the query string
           parameter <code>if-unused=true</code>. This prevents the
           delete from succeeding if the exchange is bound to a queue
           or as a source to another exchange.
         </p>
       </td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/exchanges/<i>vhost</i>/<i>name</i>/bindings/source</td>
       <td>A list of all bindings in which a given exchange is the source.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/exchanges/<i>vhost</i>/<i>name</i>/bindings/destination</td>
       <td>A list of all bindings in which a given exchange is the destination.</td>
     </tr>
     <tr>
       <td></td>
       <td></td>
       <td></td>
       <td>X</td>
       <td class="path">/api/exchanges/<i>vhost</i>/<i>name</i>/publish</td>
       <td>
         Publish a message to a given exchange. You will need a body
         looking something like:
         <pre>{"properties":{},"routing_key":"my key","payload":"my body","payload_encoding":"string"}</pre>
         All keys are mandatory. The <code>payload_encoding</code>
         key should be either "string" (in which case the payload
         will be taken to be the UTF-8 encoding of the payload field)
         or "base64" (in which case the payload field is taken to be
         base64 encoded).<br/>
         If the message is published successfully, the response will
         look like:
         <pre>{"routed": true}</pre>
         <code>routed</code> will be true if the message was sent to
         at least one queue.
         <p>
           Please note that the HTTP API is not ideal for high
           performance publishing; the need to create a new TCP
           connection for each message published can limit message
           throughput compared to AMQP or other protocols using
           long-lived connections.
         </p>
       </td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/queues</td>
       <td>A list of all queues.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/queues/<i>vhost</i></td>
       <td>A list of all queues in a given virtual host.</td>
     </tr>
     <tr>
       <td>X</td>
       <td>X</td>
       <td>X</td>
       <td></td>
       <td class="path">/api/queues/<i>vhost</i>/<i>name</i></td>
       <td>
         An individual queue. To PUT a queue, you will need a body looking something like this:
         <pre>{"auto_delete":false,"durable":true,"arguments":{},"node":"rabbit@smacmullen"}</pre>
         All keys are optional.
         <p>
           When DELETEing a queue you can add the query string
           parameters <code>if-empty=true</code> and /
           or <code>if-unused=true</code>. These prevent the delete
           from succeeding if the queue contains messages, or has
           consumers, respectively.
         </p>
       </td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/queues/<i>vhost</i>/<i>name</i>/bindings</td>
       <td>A list of all bindings on a given queue.</td>
     </tr>
     <tr>
       <td></td>
       <td></td>
       <td>X</td>
       <td></td>
       <td class="path">/api/queues/<i>vhost</i>/<i>name</i>/contents</td>
       <td>Contents of a queue. DELETE to purge. Note you can't GET this.</td>
     </tr>

     <tr>
       <td></td>
       <td></td>
       <td></td>
       <td>X</td>
       <td class="path">/api/queues/<i>vhost</i>/<i>name</i>/actions</td>
       <td>
         Actions that can be taken on a queue. POST a body like:
         <pre>{"action":"sync"}</pre> Currently the actions which are
         supported are <code>sync</code> and <code>cancel_sync</code>.
       </td>
     </tr>

     <tr>
       <td></td>
       <td></td>
       <td></td>
       <td>X</td>
       <td class="path">/api/queues/<i>vhost</i>/<i>name</i>/get</td>
       <td>
         Get messages from a queue. (This is not an HTTP GET as it
         will alter the state of the queue.) You should post a body looking like:
         <pre>{"count":5,"requeue":true,"encoding":"auto","truncate":50000}</pre>
         <ul>
           <li><code>count</code> controls the maximum number of
           messages to get. You may get fewer messages than this if
           the queue cannot immediately provide them.</li>
           <li><code>requeue</code> determines whether the messages will be
           removed from the queue. If requeue is true they will be requeued -
           but their <code>redelivered</code> flag will be set.</li>
           <li><code>encoding</code> must be either "auto" (in which case the
           payload will be returned as a string if it is valid UTF-8, and
           base64 encoded otherwise), or "base64" (in which case the payload
           will always be base64 encoded).</li>
           <li>If <code>truncate</code> is present it will truncate the
           message payload if it is larger than the size given (in bytes).</li>
         </ul>
         <p><code>truncate</code> is optional; all other keys are mandatory.</p>
         <p>
           Please note that the get path in the HTTP API is intended
           for diagnostics etc - it does not implement reliable
           delivery and so should be treated as a sysadmin's tool
           rather than a general API for messaging.
         </p>
       </td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/bindings</td>
       <td>A list of all bindings.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/bindings/<i>vhost</i></td>
       <td>A list of all bindings in a given virtual host.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td>X</td>
       <td class="path">/api/bindings/<i>vhost</i>/e/<i>exchange</i>/q/<i>queue</i></td>
       <td>A list of all bindings between an exchange and a
       queue. Remember, an exchange and a queue can be bound
       together many times! To create a new binding, POST to this
       URI. You will need a body looking something like this:
         <pre>{"routing_key":"my_routing_key","arguments":{}}</pre>
         All keys are optional.
         The response will contain a <code>Location</code> header
         telling you the URI of your new binding.
       </td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td>X</td>
       <td></td>
       <td class="path">/api/bindings/<i>vhost</i>/e/<i>exchange</i>/q/<i>queue</i>/<i>props</i></td>
       <td>An individual binding between an exchange and a queue.
       The <i>props</i> part of the URI is a "name" for the binding
       composed of its routing key and a hash of its arguments.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td>X</td>
       <td class="path">/api/bindings/<i>vhost</i>/e/<i>source</i>/e/<i>destination</i></td>
       <td>
         A list of all bindings between two exchanges. Similar to
         the list of all bindings between an exchange and a queue,
         above.
       </td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td>X</td>
       <td></td>
       <td class="path">/api/bindings/<i>vhost</i>/e/<i>source</i>/e/<i>destination</i>/<i>props</i></td>
       <td>
         An individual binding between two exchanges. Similar to
         the individual binding between an exchange and a queue,
         above.
       </tD>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/vhosts</td>
       <td>A list of all vhosts.</td>
     </tr>
     <tr>
       <td>X</td>
       <td>X</td>
       <td>X</td>
       <td></td>
       <td class="path">/api/vhosts/<i>name</i></td>
       <td>An individual virtual host. As a virtual host usually only
       has a name, you do not need an HTTP body when PUTing one of
       these. To enable / disable tracing, provide a body looking like:
         <pre>{"tracing":true}</pre></td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/vhosts/<i>name</i>/permissions</td>
       <td>A list of all permissions for a given virtual host.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/users</td>
       <td>A list of all users.</td>
     </tr>
     <tr>
       <td>X</td>
       <td>X</td>
       <td>X</td>
       <td></td>
       <td class="path">/api/users/<i>name</i></td>
       <td>An individual user. To PUT a user, you will need a body looking something like this:
<pre>{"password":"secret","tags":"administrator"}</pre>
or:
<pre>{"password_hash":"2lmoth8l4H0DViLaK9Fxi6l9ds8=", "tags":"administrator"}</pre>
       The <code>tags</code> key is mandatory. Either
       <code>password</code> or <code>password_hash</code>
       must be set. Setting <code>password_hash</code> to "" will ensure the
       user cannot use a password to log in. <code>tags</code> is a
       comma-separated list of tags for the user. Currently recognised tags
       are "administrator", "monitoring" and "management".
       </td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/users/<i>user</i>/permissions</td>
       <td>A list of all permissions for a given user.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/whoami</td>
       <td>Details of the currently authenticated user.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/permissions</td>
       <td>A list of all permissions for all users.</td>
     </tr>
     <tr>
       <td>X</td>
       <td>X</td>
       <td>X</td>
       <td></td>
       <td class="path">/api/permissions/<i>vhost</i>/<i>user</i></td>
       <td>An individual permission of a user and virtual host. To PUT a permission, you will need a body looking something like this:
<pre>{"configure":".*","write":".*","read":".*"}</pre>
       All keys are mandatory.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/parameters</td>
       <td>A list of all parameters.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/parameters/<i>component</i></td>
       <td>A list of all parameters for a given component.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/parameters/<i>component</i>/<i>vhost</i></td>
       <td>A list of all parameters for a given component and virtual host.</td>
     </tr>
     <tr>
       <td>X</td>
       <td>X</td>
       <td>X</td>
       <td></td>
       <td class="path">/api/parameters/<i>component</i>/<i>vhost</i>/<i>name</i></td>
       <td>An individual parameter. To PUT a parameter, you will need a body looking something like this:
<pre>{"vhost": "/","component":"federation","name":"local_username","value":"guest"}</pre>
</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/policies</td>
       <td>A list of all policies.</td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/policies/<i>vhost</i></td>
       <td>A list of all policies in a given virtual host.</td>
     </tr>
     <tr>
       <td>X</td>
       <td>X</td>
       <td>X</td>
       <td></td>
       <td class="path">/api/policies/<i>vhost</i>/<i>name</i></td>
       <td>
         An individual policy. To PUT a policy, you will need a body looking something like this:
<pre>{"pattern":"^amq.", "definition": {"federation-upstream-set":"all"}, "priority":0, "apply-to": "all"}</pre>
         <code>pattern</code> and <code>definition</code> are mandatory, <code>priority</code> and <code>apply-to</code> are optional.
       </td>
     </tr>
     <tr>
       <td>X</td>
       <td></td>
       <td></td>
       <td></td>
       <td class="path">/api/aliveness-test/<i>vhost</i></td>
       <td>
         Declares a test queue, then publishes and consumes a
         message. Intended for use by monitoring tools. If everything
         is working correctly, will return HTTP status 200 with
         body: <pre>{"status":"ok"}</pre> Note: the test queue will
         not be deleted (to to prevent queue churn if this is
         repeatedly pinged).
       </td>
     </tr>
   </table>
