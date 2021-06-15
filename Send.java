// The RabbitMQ Java client is in the central Maven repository, 
// with the groupId com.rabbitmq and the artifactId amqp-client.
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

public class Send {

    // Name of message buffer queue
    private final static String QUEUE_NAME = "hello";
    public static void main(String[] argv) throws Exception {
        // Create a connection
        ConnectionFactory factory = new ConnectionFactory();

        // Connect a RabbitMQ node on the local machine
        // if we want to connect to different machine - specify hostname/IP address
        factory.setHost("localhost");

        // We use a try-with-resources statement because both Connection and Channel implement java.io.Closeable
        // This way we don't need to close them explicitly in our code
        try (Connection connection = factory.newConnection();

            // Create a channel
            Channel channel = connection.createChannel()) {
            
            // Declare a queue - created only if it does not exist
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            
            String message = "Hello World!";
            
            // Publish the message
            // The message content is a byte array, so you can encode whatever you like there
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}