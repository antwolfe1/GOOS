package auctionsniper;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jxmpp.jid.EntityBareJid;

public class AuctionMessageTranslator implements OutgoingChatMessageListener, IncomingChatMessageListener {
    private final AuctionEventListener listener;

    public AuctionMessageTranslator(AuctionEventListener listener) {
        this.listener = listener;
    }

    public void processMessage(Chat chat, MessageBuilder message) {
        listener.auctionClosed();
    }

    @Override
    public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {

    }

    @Override
    public void newOutgoingMessage(EntityBareJid entityBareJid, MessageBuilder messageBuilder, Chat chat) {
    }
}
