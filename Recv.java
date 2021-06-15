import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
// DeliverCallback interface is used to buffer the messages pushed to us by the server

public class Recv {

    // Declare queue
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        // Create a connection
        // No try-with-resource - we want the process to stay alive 
        // while the consumer is listening asynchronously for messages to arrive
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // Since the messafes are pushed asynchronously, we provide a callback
        // in form of object that will buffer messages until we are ready to use them
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}
