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
        AuctionEvent event = AuctionEvent.from(message.getBody());

        String eventType = event.type();
        if ("CLOSE".equals(eventType)) {
            listener.auctionClosed();
        } else if ("PRICE".equals(eventType)) {
            listener.currentPrice(event.currentPrice(), event.increment());
        }
    }

        @Override
        public void newIncomingMessage (EntityBareJid entityBareJid, Message message, Chat chat){
            processMessage(chat, message);
        }

        @Override
        public void newOutgoingMessage (EntityBareJid entityBareJid, MessageBuilder messageBuilder, Chat chat){
            processMessage(chat, messageBuilder.build());

        }

        private static class AuctionEvent {
            private final HashMap<String, String> fields = new HashMap<>();

            public String type() {
                return get("Event");
            }

            public int currentPrice() {
                return getInt("CurrentPrice");
            }

            public int increment() {
                return getInt("Increment");
            }

            private String get(String fieldName) {
                return fields.get(fieldName);
            }

            private int getInt(String fieldName) {
                return Integer.parseInt(get(fieldName));
            }


            private static String[] fieldsIn(String messageBody) {
                return messageBody.split(";");
            }


            private void addField(String field) {
                String[] pair = field.split(":");
                fields.put(pair[0].trim(), pair[1].trim());
            }

            public static AuctionEvent from(String messageBody) {
                AuctionEvent event = new AuctionEvent();
                for (String field : fieldsIn(messageBody)) {
                    event.addField(field);
                }
                return event;
            }
        }
    }
