package book.growingOOP;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class FakeAuctionServer {

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
            config = XMPPTCPConnectionConfiguration.builder().setXmppDomain(XMPP_HOSTNAME).setHost(XMPP_HOSTNAME).setResource(AUCTION_RESOURCE).setPort(5222).addEnabledSaslMechanism("PLAIN").setSecurityMode(ConnectionConfiguration.SecurityMode.disabled).build();
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
        Chat chat = chatManager.chatWith(JidCreate.entityBareFrom(format(ITEM_ID_AS_LOGIN, itemId) + "@" + XMPP_HOSTNAME + "/" + AUCTION_RESOURCE));
        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
                System.out.println(message.getBody());
                messageListener.processMessage(message);
            }
        });
        currentChat = chat;
    }

    public String getItemId() {
        return itemId;
    }

    public void hasReceivedJoinRequestFromSniper() throws InterruptedException {
        messageListener.receivesAMessage();
    }

    public void announceClosed() throws SmackException.NotConnectedException, InterruptedException {
        currentChat.send("new FakeAuctionServerSaysClosedMessage()");
    }

    public void stop() {
        connection.disconnect();
    }

    public static class SingleMessageListener {
        private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(1);
        private final ArrayList<Message> messageArrayList = new ArrayList<>();

        public void processMessage(Message message) {
            System.out.println(message);
            messages.add(message);
            System.out.println(messages);
//            messageArrayList.add(message);
        }

        public void receivesAMessage() throws InterruptedException {
            System.out.println("receivesAMsg: " + messages);
//            System.out.println(messageArrayList);
            assertThat("Message", messages.poll(5, TimeUnit.SECONDS), is(notNullValue()));
        }

    }

}



