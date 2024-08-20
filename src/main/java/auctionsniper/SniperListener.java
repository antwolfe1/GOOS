package auctionsniper;

import java.util.EventListener;

public interface SniperListener extends EventListener {
    void sniperLost();
    void sniperBidding(final SniperState sniperState);
    void sniperWinning();
    void sniperWon();
}
