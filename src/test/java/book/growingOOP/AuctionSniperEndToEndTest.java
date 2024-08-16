package book.growingOOP;

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
        auction.startSellingItem();

        // Step 1
        application.startBiddingIn(auction);

        // Step 2
        auction.hasReceivedJoinRequestFromSniper();

        // Step 3
        auction.announceClosed();

        // Step 4
        application.showsSniperHasLostAuction();


    }


}
