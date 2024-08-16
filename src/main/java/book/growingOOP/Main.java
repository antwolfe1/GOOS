package book.growingOOP;

import book.growingOOP.ui.MainWindow;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;


import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;


public class Main {
    public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;
    private static final int ARG_ITEM_ID = 3;
    private static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_AS_ID_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_AS_ID_LOGIN + "@%s/" + AUCTION_RESOURCE;
    private MainWindow ui;

    @SuppressWarnings("unused") private Chat notToBeGCd;


    public Main() throws Exception {
        startUserInterface();
    }
    
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.joinAuction(connectTo(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]), args[ARG_ITEM_ID]);
    }

    private void joinAuction(XMPPTCPConnection connection, String itemId) throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        EntityBareJid jid = JidCreate.entityBareFrom(auctionId(itemId, connection));
        Chat chat = chatManager.chatWith(jid);
        chatManager.addOutgoingListener(new OutgoingChatMessageListener() {
            @Override
            public void newOutgoingMessage(EntityBareJid entityBareJid, MessageBuilder messageBuilder, Chat chat) {
                SwingUtilities.invokeLater(() -> ui.showStatus(MainWindow.STATUS_LOST));
            }
        });
//        chatManager.addIncomingListener(new IncomingChatMessageListener() {
//            @Override
//            public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
//                System.out.println(message + "" + chat);
//                SwingUtilities.invokeLater(() -> ui.showStatus(MainWindow.STATUS_LOST));
//            }
//        });
        chat.send("Lost");
        this.notToBeGCd = chat;
    }

    private static String auctionId(String itemId, XMPPTCPConnection connection) {
        return String.format(AUCTION_ID_FORMAT, itemId, connection.getXMPPServiceDomain());
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ui = new MainWindow();
            }
        });
    }
    
    public static XMPPTCPConnection connectTo(String hostname, String username, String password) throws XmppStringprepException {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder().setXmppDomain(hostname).setHost(hostname).setPort(5222).setResource(AUCTION_RESOURCE).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled).build(); //.setUsernameAndPassword(username, password)
        XMPPTCPConnection connection = new XMPPTCPConnection(config);
        try {
            connection.connect();
            connection.login(username, password);
//            connection.login();
        } catch (IOException | SmackException| XMPPException | InterruptedException e){
            e.printStackTrace();
        }
        return connection;
    }


    
    

}
