# Sample PCF demo using RabbitMQ

# Foreword
This document gives two examples of using RabbigMQ in _cloud foundry_ in a SpringBoot application. There are also several POJO examples in the _simplemains_ package.

  - Detailed documentation for your RabbitMQ instance can be found at https://pivotal-rabbitmq.your.cfinstallation.com/api/
  - The RabbitMQ Manager page is https://pivotal-rabbitmq.your.cfinstallation.com/#/
  - The RabbitMQ tutorial page is
  https://www.rabbitmq.com/getstarted.html

# Overview
There are two examples as part of this sample pack: a PUB/SUB as well as RPC style using a work queue and dynamic queues for responses.

The Swagger page for these examples will be available here:
http://rabbit-sandbox.your.cfinstallation.com/swagger-ui.html#/

# Publish and Subscribe

- GET /async/\_startpubsubconsumer
- POST /async/pubsub

# Work Queue RPC style with multiple subsribers

- POST /async/usagerequests
- GET /async/usages/{requestId}
