package auctionsniper;

import auctionsniper.ui.MainWindow;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;


public class Main {
    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;
    private static final int ARG_ITEM_ID = 3;

    private static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_AS_ID_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_AS_ID_LOGIN + "@%s/" + AUCTION_RESOURCE;
    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    private MainWindow ui;
    @SuppressWarnings("unused")
    private Chat notToBeGCd;


    public Main() throws Exception {
        startUserInterface();
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(() -> ui = new MainWindow());
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.joinAuction(connectTo(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]), args[ARG_ITEM_ID]);
    }


    private void joinAuction(XMPPTCPConnection connection, String itemId)
            throws XmppStringprepException {

        disconnectWhenUICloses(connection);
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        EntityBareJid jid = JidCreate.entityBareFrom(auctionId(itemId, connection));
        final Chat chat = chatManager.chatWith(jid);
        this.notToBeGCd = chat;

        Auction auction = new XMPPAuction(chat);

        chatManager.addOutgoingListener(new AuctionMessageTranslator(new AuctionSniper(auction, new SniperStateDisplayer())));
        chatManager.addIncomingListener(new AuctionMessageTranslator(new AuctionSniper(auction, new SniperStateDisplayer())));

        auction.join();

    }


    private static String auctionId(String itemId, XMPPTCPConnection connection) {
        return String.format(AUCTION_ID_FORMAT, itemId, connection.getXMPPServiceDomain());
    }

    public static XMPPTCPConnection connectTo(String hostname, String username, String password) throws XmppStringprepException {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(hostname).setHost(hostname).setPort(5222)
                .setResource(AUCTION_RESOURCE).addEnabledSaslMechanism(SASLMechanism.PLAIN).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build(); //.setUsernameAndPassword(username, password)
        XMPPTCPConnection connection = new XMPPTCPConnection(config);
        try {
            connection.connect();
            connection.login(username, password);
//            connection.login();
        } catch (IOException | SmackException | XMPPException | InterruptedException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private void disconnectWhenUICloses(final XMPPTCPConnection connection) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                connection.disconnect();
            }
        });
    }


    private class SniperStateDisplayer implements SniperListener {
        @Override
        public void sniperLost() {
            showStatus(MainWindow.STATUS_LOST);
        }

        @Override
        public void sniperBidding() {
            showStatus(MainWindow.STATUS_BIDDING);
        }

        private void showStatus(final String status) {
            SwingUtilities.invokeLater(() -> ui.showStatus(status));
        }
    }
}
