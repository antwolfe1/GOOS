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
    private static final String SNIPER_ID = "sniper id";
    public static final Chat UNUSED_CHAT = null;

    private final Mockery context = new Mockery();
    private final AuctionEventListener listener = context.mock(AuctionEventListener.class);
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(SNIPER_ID, listener);

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
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromOtherBidder(){
        context.checking(new Expectations() {{
            exactly(1).of(listener).currentPrice(192, 7, AuctionEventListener.PriceSource.FromOtherBidder);
        }});

        Message message = MessageBuilder.buildMessage().
                setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;").build();
        translator.processMessage(UNUSED_CHAT, message);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper(){
        context.checking(new Expectations() {{
            exactly(1).of(listener).currentPrice(234, 5, AuctionEventListener.PriceSource.FromSniper);
        }});

        Message message = MessageBuilder.buildMessage().
                setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";").build();
        translator.processMessage(UNUSED_CHAT, message);
    }


}
