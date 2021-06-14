import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Worker {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        boolean durable = true;
        channel.queueDeclare("task_queue", durable, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.basicQos(1); // accept only one unack-ed message at a time (see below)

        //  Fake a second of work for every dot in the message body
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
          
            System.out.println(" [x] Received '" + message + "'");
            try {
              doWork(message);
            } catch(InterruptedException ie) {
                System.out.println(ie);
            } finally {
              System.out.println(" [x] Done");
              channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
          };
          boolean autoAck = false; // acknowledgment set to false
          channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });
    }

    // doWork - Our fake task to simulate execution time
    private static void doWork(String task) throws InterruptedException {
        for (char ch: task.toCharArray()) {
            if (ch == '.') Thread.sleep(1000);
        }
    }
}
