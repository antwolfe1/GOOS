package test.auctionsniper;

import auctionsniper.AuctionEventListener;
import auctionsniper.AuctionMessageTranslator;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.jupiter.api.Test;


public class AuctionMessageTranslatorTest {
    private final Mockery context = new Mockery();
    private final AuctionEventListener listener = context.mock(AuctionEventListener.class);
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(listener);
    public static final Chat UNUSED_CHAT = null;

    @Test
    public void notifiesAuctionClosedWhenClosesMessageReceived() {
        context.checking(new Expectations(){{
            oneOf(listener).auctionClosed();
        }});

       Message message = MessageBuilder.buildMessage().
               setBody("SOLVersion: 1.1; Event: CLOSE;").build();
       translator.processMessage(UNUSED_CHAT, message);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceived(){
        context.checking(new Expectations() {{
            exactly(1).of(listener).currentPrice(192, 7);
        }});

        Message message = MessageBuilder.buildMessage().
                setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;").build();
        translator.processMessage(UNUSED_CHAT, message);
    }
}
