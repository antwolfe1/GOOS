package test.auctionsniper;

import auctionsniper.Main;
import org.hamcrest.Matcher;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class FakeAuctionServer {
    // a stub for an auction server

    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_RESOURCE = "Auction";
    public static final String XMPP_HOSTNAME = "localhost";
    private static final String AUCTION_PASSWORD = "auction";

    private final SingleMessageListener messageListener = new SingleMessageListener();
    private final String itemId;
    private final XMPPTCPConnectionConfiguration config;
    private final XMPPTCPConnection connection;
    private Chat currentChat;

    {
        try {
            config = XMPPTCPConnectionConfiguration.builder().
                    setXmppDomain(XMPP_HOSTNAME).setHost(XMPP_HOSTNAME).setResource(AUCTION_RESOURCE).setPort(5222)
                    .addEnabledSaslMechanism(SASLMechanism.PLAIN).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled).build();
        } catch (XmppStringprepException e) {
            throw new RuntimeException(e);
        }
    }


    public FakeAuctionServer(String itemId) {
        this.itemId = itemId;
        this.connection = new XMPPTCPConnection(config);
    }

    public void startSellingItem() throws XMPPException, SmackException, IOException, InterruptedException {
        connection.connect();
        connection.login(format(ITEM_ID_AS_LOGIN, itemId), AUCTION_PASSWORD);
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
                chatManager.chatWith(chat.getXmppAddressOfChatPartner());
                messageListener.processMessage(message);
                currentChat = chat;

            }
        });
    }

    public String getItemId() {
        return itemId;
    }

    public void reportPrice(int price, int increment, String bidder) throws SmackException.NotConnectedException, InterruptedException {
        currentChat.send(format("SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s;",
                price, increment, bidder));

    }

    public void hasReceivedJoinRequestFromSniper(String sniperId) throws InterruptedException {
        receivesAMessageMatching(sniperId, equalTo(Main.JOIN_COMMAND_FORMAT));
    }

    public void hasReceivedBid(int bid, String sniperId) throws InterruptedException {
        receivesAMessageMatching(sniperId, equalTo(format(Main.BID_COMMAND_FORMAT, bid)));
    }

    private void receivesAMessageMatching(String sniperId, Matcher<? super String> messageMatcher) throws InterruptedException {
        messageListener.receivesAMessage(messageMatcher);
        assertThat(currentChat.getXmppAddressOfChatPartner() + "/" + AUCTION_RESOURCE, equalTo(sniperId));
    }


    public void announceClosed() throws SmackException.NotConnectedException, InterruptedException {
        currentChat.send("SOL Version: 1.1; Event: CLOSE;");
    }

    public void stop() {
        connection.disconnect();
    }


    public static class SingleMessageListener {
        // class to help process and receive messages
        private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(1);

        public void processMessage(Message message) {
            messages.add(message);
        }


        public void receivesAMessage(Matcher<? super String> messageMatcher) throws InterruptedException {
            final Message message = messages.poll(5, TimeUnit.SECONDS);
            assertThat("Message", message, is(notNullValue()));
            assertThat(message.getBody(), messageMatcher);

        }
    }

}



