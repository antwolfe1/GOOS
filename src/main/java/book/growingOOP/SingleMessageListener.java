package book.growingOOP;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class SingleMessageListener implements MessageListener {
    private final ArrayBlockingQueue<Message> messages = ArrayBlockingQueue<Message>(1);

    public void processMessage(Chat chat, Message message){
        messages.add(message);
    }

    public void receivesAMessage(Chat chat, Message message) throws InterruptedException {
        assertThat("Message", messages.poll(5, TimeUnit.SECONDS), is(notNullValue()));
    }
}
