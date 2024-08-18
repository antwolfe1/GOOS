package auctionsniper;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jxmpp.jid.EntityBareJid;

import java.util.HashMap;

public class AuctionMessageTranslator implements OutgoingChatMessageListener, IncomingChatMessageListener {
    private final AuctionEventListener listener;

    public AuctionMessageTranslator(AuctionEventListener listener) {
        this.listener = listener;
    }

    public void processMessage(Chat chat, Message message) {
        HashMap<String, String> event = unpackEventFrom(message);
        System.out.println(event);
        String type = event.get("Event");
        if ("CLOSE".equals(type)) {
            listener.auctionClosed();
        } else if ("PRICE".equals(type)) {
            listener.currentPrice((Integer.parseInt(event.get("CurrentPrice"))), (Integer.parseInt(event.get("Increment"))));
        }
    }

    private HashMap<String, String> unpackEventFrom(Message message) {
        HashMap<String, String> event = new HashMap<>();
        for (String element : message.getBody().split(";")) {
            String[] pair = element.split(":");
            event.put(pair[0].trim(), pair[1].trim());
        }
        return event;
    }

    @Override
    public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {

    }

    @Override
    public void newOutgoingMessage(EntityBareJid entityBareJid, MessageBuilder messageBuilder, Chat chat) {
//        System.out.println(messageBuilder.build().getBody());
//        processMessage(chat, messageBuilder.build());
        listener.auctionClosed();
    }
}
