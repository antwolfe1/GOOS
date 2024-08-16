package test.auctionsniper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;


public class AuctionSniperEndToEndTest {
    private final FakeAuctionServer auction = new FakeAuctionServer("item-54321");
    private final ApplicationRunner application = new ApplicationRunner();

    // Additional Cleanup
    @AfterEach
    public void stopAuctionAndApplication() {
        auction.stop();
        application.stop();
    }

    @Test
    public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
        // fake stub to start bidding
        auction.startSellingItem();
        // start synchronizing the app and stub
        application.startBiddingIn(auction);
        // assert that the synchronization has worked
        auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);

        auction.announceClosed();
        application.showsSniperHasLostAuction();
    }

    @Test
    public void sniperMakesAHigherBidButLoses() throws Exception {
        auction.startSellingItem();
        application.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
        // send the price details to the stub
        auction.reportPrice(1000, 98, "other bidder");
        // asks application runner to check that sniper shows it is now bidding after receiving price update
        application.hasShownSniperIsBidding();
        // ask stub to check sniper bid is equal to the last price + min increment
        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);
        auction.announceClosed();
        application.showsSniperHasLostAuction();


    }


}
