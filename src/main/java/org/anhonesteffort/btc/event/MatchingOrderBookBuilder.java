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

package org.anhonesteffort.btc.event;

import org.anhonesteffort.btc.book.HeuristicLimitOrderBook;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.book.OrderPool;
import org.anhonesteffort.btc.book.TakeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchingOrderBookBuilder extends MarketOrderBookBuilder {

  private static final Logger log = LoggerFactory.getLogger(MatchingOrderBookBuilder.class);

  public MatchingOrderBookBuilder(HeuristicLimitOrderBook book, OrderPool pool) {
    super(book, pool);
  }

  protected Order takePooledTakerOrder(OrderEvent match) throws OrderEventException {
    if (match.getPrice() > 0 && match.getSize() > 0) {
      if (match.getSide().equals(Order.Side.ASK)) {
        return pool.take(match.getTakerId(), Order.Side.BID, match.getPrice(), match.getSize());
      } else {
        return pool.take(match.getTakerId(), Order.Side.ASK, match.getPrice(), match.getSize());
      }
    } else {
      throw new OrderEventException("match event has invalid taker price or size");
    }
  }

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    super.onEvent(event);
    if (event.getType().equals(OrderEvent.Type.MATCH)) {
      Order      taker    = takePooledTakerOrder(event);
      TakeResult result   = book.add(taker);

      if (result.getTakeSize() > 0) {
        log.info("matched taker -> " + taker.getOrderId() + " with maker -> " + result.getMakers().get(0).getOrderId());
        log.info("match event says maker was -> " + event.getMakerId());
      }

      if (result.getMakers().size() > 1) {
        throw new OrderEventException("match event took " + result.getMakers().size() + " makers from the book");
      } else if (!doublesEqual(result.getTakeSize(), taker.getSize())) {
        throw new OrderEventException(
            "take size for match event does not agree with our book " +
                event.getSize() + " vs " + result.getTakeSize()
        );
      } else if (taker.getSizeRemaining() > 0) {
        throw new OrderEventException("taker for match event was left on the book");
      } else {
        onOrderMatched(taker, result);
        returnPooledOrder(taker);
        returnPooledOrders(result);
      }
    }
  }

  protected void onOrderMatched(Order taker, TakeResult result) {
    log.info("matched order " + taker.getOrderId() + " for size " + result.getTakeSize());
  }

}
