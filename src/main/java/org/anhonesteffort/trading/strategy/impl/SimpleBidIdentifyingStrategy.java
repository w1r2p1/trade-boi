/*
 * Copyright (C) 2016 An Honest Effort LLC.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.anhonesteffort.trading.strategy.impl;

import org.anhonesteffort.trading.compute.SpreadComputation;
import org.anhonesteffort.trading.compute.SummingComputation;
import org.anhonesteffort.trading.compute.TakeVolumeComputation;
import org.anhonesteffort.trading.http.request.model.PostOrderRequest;
import org.anhonesteffort.trading.http.request.RequestFactory;
import org.anhonesteffort.trading.state.GdaxState;
import org.anhonesteffort.trading.strategy.BidIdentifyingStrategy;
import org.anhonesteffort.trading.book.Order;

import java.util.Optional;

public class SimpleBidIdentifyingStrategy extends BidIdentifyingStrategy {

  private final Params params;

  private final SummingComputation buyVolumeRecent;
  private final SummingComputation buyVolumeNow;
  private final SummingComputation sellVolumeRecent;
  private final SummingComputation sellVolumeNow;
  private final SpreadComputation  spread;

  public SimpleBidIdentifyingStrategy(Params params, RequestFactory requests) {
    super(requests);
    this.params = params;

    buyVolumeRecent  = new SummingComputation(new TakeVolumeComputation(Order.Side.BID), params.recentMs);
    buyVolumeNow     = new SummingComputation(new TakeVolumeComputation(Order.Side.BID), params.nowMs);
    sellVolumeRecent = new SummingComputation(new TakeVolumeComputation(Order.Side.ASK), params.recentMs);
    sellVolumeNow    = new SummingComputation(new TakeVolumeComputation(Order.Side.ASK), params.nowMs);
    spread           = new SpreadComputation();

    addChildren(spread, buyVolumeRecent, buyVolumeNow, sellVolumeRecent, sellVolumeNow);
  }

  private Optional<Double> getRecentBuyRatio() {
    if (!buyVolumeRecent.getResult().isPresent() || !sellVolumeRecent.getResult().isPresent()) {
      return Optional.empty();
    } else if (buyVolumeRecent.getResult().get() < params.recentBuyBtcMin) {
      return Optional.of(-1d);
    } else if (sellVolumeRecent.getResult().get() == 0d) {
      return Optional.of(Double.MAX_VALUE);
    } else {
      return Optional.of(
          buyVolumeRecent.getResult().get() / sellVolumeRecent.getResult().get()
      );
    }
  }

  private Optional<Boolean> isNowBearish() {
    if (!buyVolumeNow.getResult().isPresent() || !sellVolumeNow.getResult().isPresent()) {
      return Optional.empty();
    } else {
      return Optional.of(buyVolumeNow.getResult().get() < sellVolumeNow.getResult().get());
    }
  }

  private boolean isBullish() {
    Optional<Double>  recentBuyRatio = getRecentBuyRatio();
    Optional<Boolean> isNowBearish   = isNowBearish();

    if (recentBuyRatio.isPresent() && isNowBearish.isPresent()) {
      return recentBuyRatio.get() >= params.recentBuyRatioMin && !isNowBearish.get();
    } else {
      return false;
    }
  }

  @Override
  protected Optional<PostOrderRequest> identifyBid(GdaxState state, long nanoseconds) {
    if (isBullish()) {
      double bidFloor   = state.getOrderBook().getBidLimits().peek().get().getPrice();
      double askCeiling = state.getOrderBook().getAskLimits().peek().get().getPrice();

      if (spread.getResult().get() > 0.02d) {
        return Optional.of(bidRequest(
            bidFloor + ((askCeiling - bidFloor) * params.bidPlacement),
            params.bidSize
        ));
      } else {
        return Optional.of(bidRequest(bidFloor, params.bidSize));
      }

    } else {
      return Optional.empty();
    }
  }

  public static class Params {
    private final double bidSize;
    private final double bidPlacement;
    private final long   recentMs;
    private final double recentBuyRatioMin;
    private final double recentBuyBtcMin;
    private final long   nowMs;

    public Params(
        double bidSize, double bidPlacement, long recentMs,
        double recentBuyRatioMin, double recentBuyBtcMin, long nowMs
    ) {
      this.bidSize           = bidSize;
      this.bidPlacement      = bidPlacement;
      this.recentMs          = recentMs;
      this.recentBuyRatioMin = recentBuyRatioMin;
      this.recentBuyBtcMin   = recentBuyBtcMin;
      this.nowMs             = nowMs;
    }
  }

}