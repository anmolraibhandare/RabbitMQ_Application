import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class RPCClient implements AutoCloseable {

    // Establish connection, channel
    private Connection connection;
    private Channel channel;
    // Declaring the queue
    private String requestQueueName = "rpc_queue";

    // Create a connection
    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public static void main(String[] argv) {
        // Fibonnaci task
        try (RPCClient fibonacciRpc = new RPCClient()) {
            for (int i = 0; i < 32; i++) {
                String i_str = Integer.toString(i);
                System.out.println(" [x] Requesting fib(" + i_str + ")");
                // Call function used to make RPC request
                String response = fibonacciRpc.call(i_str);
                System.out.println(" [.] Got '" + response + "'");
            }
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String call(String message) throws IOException, InterruptedException {

        // 1. Create a correlationId - consumer uses this value to match response
        final String corrId = UUID.randomUUID().toString();

        // 2. Reply queue declared - callback queue with properties that go with message
        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        // 3. Publish the request message with replyTo Queue and correlationId (corrId)
        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        // 4. Blocking queue to suspend the main thread before response arrives
        // with capacity set to 1 as we need to wait for only one response
        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        // Task of consumer: for every consumed response message it checks if the correlationId (corrId) is the one we're looking for
        // If so, it puts the response to BlockingQueue (response)
        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });

        String result = response.take();
        channel.basicCancel(ctag);

        // Return response
        return result;
    }

    public void close() throws IOException {
        connection.close();
    }
}