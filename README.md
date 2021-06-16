# RabbitMQ_Application
 
## Introduction - Creating "Hello World" and Setting up RabbitMQ
![](Images/java-one.png)

### Step 1: Creating a Sender 
The publisher will connect to RabbitMQ, send a single message, then exit
- Create a connection which will generate a RabbitMQ node on the local machine
- Create a channel, which is where most of the API for getting things done resides
- To send, we must declare a queue for us to send to
- Publish a message to queue

![](Images/sending.png)

### Step 2: Creating a Receiver
Consumer listens for messages from RabbitMQ.
- Declare the queue (Because we might start the consumer before the publisher, we want to make sure the queue exists before we try to consume messages from it)
- Write a try-with-resource statement to automatically close the channel and the connection
- Provide a callback in the form of an object that will buffer the messages until we're ready to use them

![](Images/receiving.png)

### Step 3: Putting it all together

You can compile both of these with just the RabbitMQ java client on the classpath:
```javac -cp amqp-client-5.7.1.jar Send.java Recv.java ```

To run them, you'll need rabbitmq-client.jar and its dependencies on the classpath. In a terminal, run the consumer (receiver):
```java -cp .:amqp-client-5.7.1.jar:slf4j-api-1.7.26.jar:slf4j-simple-1.7.26.jar Recv```

then, run the publisher (sender):
```java -cp .:amqp-client-5.7.1.jar:slf4j-api-1.7.26.jar:slf4j-simple-1.7.26.jar Send```

#### Basics
- A _**producer**_ is a user application that sends messages.
- A _**queue**_ is a buffer that stores messages.
- A _**consumer**_ is a user application that receives messages.

## Work Queues
Create a Work Queue that will be used to distribute time-consuming tasks among multiple workers.

![](Images/prefetch-count.png)

> [Round-robin dispatching] The main idea behind Work Queues (aka: Task Queues) is to avoid doing a resource-intensive task immediately and having to wait for it to complete. Instead we schedule the task to be done later. We encapsulate a task as a message and send it to a queue. A worker process running in the background will pop the tasks and eventually execute the job. When you run many workers the tasks will be shared between them.

- **Message acknowledgment:** If the consumer dies, ensure the message is never lost using message acknowledgment; RabbitMQ will understand that a message wasn't processed fully and will re-queue it. If there are other consumers online at the same time, it will then quickly redeliver it to another consumer.
- **Message durability:** If the server crashes; Two things are required to make sure that messages aren't lost: we need to mark both the queue and messages as durable.
- **Fair dispatch:** When RabbitMQ dispatches a message when the message enters the queue. It just blindly dispatches every n-th message to the n-th consumer. Make sure that each worker receives one message at a time.

## Publisher/Subscriber - logging system

**Exchange?**
In RabbitMQ messaging, producer never sends any messages directly to a queue. Instead, the producer can only send messages to an exchange. On one side it receives messages from producers and the other side it pushes them to queues. The exchange must know exactly what to do with a message it receives.
Exchange types: direct, topic, headers and fanout

![](Images/python-three-overall.png)

Exchange: Fanout
1. Create an exchange called logs of type fanout
2. We can now publish the named exchange instead
3. Previously, we were creating Temporary queues - hello, task queues. But for logs, we want to hear about all log messages, not just a subset of them. We're also interested only in currently flowing messages not in the old ones.
     - Create an empty queue with random name
     - On disconnecting the consumer, we want the queue to be deleted automatically
4. Create a Binding (relationship between exchange and a queue) - tell the exchange to send messages to our queue

![](Images/bindings.png)

## Routing 
In this part we will direct only critical error messages to the log file (to save disk space), while still being able to print all of the log messages on the console. We want to filter messages based on their severity. For example we may want a program which writes log messages to the disk to only receive critical errors, and not waste disk space on warning or info log messages.

Exchange: Direct
1. Create Binding with routingKey
2. Use a direct exchange - a message goes to the queues whose binding key exactly matches the routing key of the message

**Example setup 1:**
![](Images/direct-exchange.png) \
In the setup you can see that direct exchange X is binded with two queue where Q1 is bounded with binding key `orange`. Q2 and Q3 are bounded with binding key `black` and `green`. When a message is published to the exchange with a routing key orange will be routed to queue Q1. Messages with a routing key of black or green will go to Q2. All other messages will be discarded.

**Example setup 2:**
![](Images/direct-exchange-multiple.png) \
A message with routing key `black` will be delivered to both Q1 and Q2

3. We receive messages exactly the same way we did before except we're going to create a new binding for each severity we're interested in.

![Example setup 2:](Images/java-four.png) \

## Topics
Exchange: Topics
The routing key for type topic exchange must be a list of words, delimited by dots. The binding key must also be in the same form. The message sent with a routing key is delivered to all queues that are bound to the matching binding key. Important cases for binding keys: \
`*` (star) can substitute for exactly one word \
`#` (hash) can substitute for zero or more words

![Example setup 2:](Images/java-five.png) \

In this example, we're going to send messages which all describe animals. The messages will be sent with a routing key that consists of three words (two dots). The first word in the routing key will describe speed, second a colour and third a species: `<speed>.<colour>.<species>`.

In the example we will create three bindings: Q1 is bound with binding key `"*.orange.*"` and Q2 with `"*.*.rabbit"` and `"lazy.#"`.
 
 - Q1 is interested in orange animals
 - Q2 is interested in hearing about rabbits and lazy animals
 
 For example:
 - `quick.orange.rabbit` -> delivered to both Q1 and Q2
 - `lazy.orange.elephant` -> delivered to both Q1 and Q2
 - `quick.orange.fox` -> delivered to Q1
 - `lazy.brown.fox` -> delivered to Q2
 - `lazy.pink.rabbit` -> delivered to Q2 only once
 - `quick.brown.fox` -> discarded
 - `lazy.orage.male.rabbit` -> delivered to Q2

## Remote procedure call (RPC)




_Access Management Console http://localhost:15672/#/_ \
_Ref https://www.rabbitmq.com/getstarted.html_
