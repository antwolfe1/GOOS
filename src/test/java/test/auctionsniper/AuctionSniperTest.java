package test.auctionsniper;

import auctionsniper.Auction;
import auctionsniper.AuctionSniper;
import auctionsniper.SniperListener;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.jupiter.api.Test;

public class AuctionSniperTest {
    private final Mockery context = new Mockery();
    private final Auction auction = context.mock(Auction.class);
    private final SniperListener sniperListener = context.mock(SniperListener.class);
    private final AuctionSniper sniper = new AuctionSniper(auction, sniperListener);



    @Test
    public void reportsLostWhenAuctionCloses() {
        context.checking(new Expectations() {{
            atMost(1).of(sniperListener).sniperLost();
        }});

        sniper.auctionClosed();
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrives(){
        final int price = 1001;
        final int increment = 25;
        context.checking(new Expectations(){{
            atMost(1).of(auction).bid(price + increment);
            atLeast(1).of(sniperListener).sniperBidding();
        }});

        sniper.currentPrice(price, increment);
    }

}
