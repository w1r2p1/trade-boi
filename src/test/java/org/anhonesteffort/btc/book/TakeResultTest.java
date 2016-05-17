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

package org.anhonesteffort.btc.book;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class TakeResultTest {

  private Order newAsk(String orderId, double price, double size) {
    return new Order(orderId, Order.Side.ASK, price, size);
  }

  private Order newBid(String orderId, double price, double size) {
    return new Order(orderId, Order.Side.BID, price, size);
  }

  @Test
  public void testEmpty() {
    final List<Order> MAKERS = new LinkedList<>();
    final TakeResult  RESULT = new TakeResult(MAKERS, 0);

    assert RESULT.getTakeSize()      == 0;
    assert RESULT.getTakeValue()     == 0;
    assert RESULT.getMakers().size() == 0;
  }

  @Test
  public void testAskMakers() {
    final List<Order> MAKERS = new LinkedList<>();
    final Order       ASK0   = newAsk("00", 10, 1);
    final Order       ASK1   = newAsk("01", 10, 3);
    final Order       ASK2   = newAsk("02", 12, 2);

    ASK0.takeSize(ASK0.getSize());
    ASK1.takeSize(ASK1.getSize());
    ASK2.takeSize(ASK2.getSize());

    MAKERS.add(ASK0);
    MAKERS.add(ASK1);
    MAKERS.add(ASK2);

    final double     TAKE_SIZE  = ASK0.getSize() + ASK1.getSize() + ASK2.getSize();
    final double     TAKE_VALUE = ASK0.getValueRemoved() + ASK1.getValueRemoved() + ASK2.getValueRemoved();
    final TakeResult RESULT     = new TakeResult(MAKERS, TAKE_SIZE);

    assert RESULT.getTakeSize()      == TAKE_SIZE;
    assert RESULT.getTakeValue()     == TAKE_VALUE;
    assert RESULT.getMakers().size() == 3;

    RESULT.clearMakerValueRemoved();

    assert ASK0.getValueRemoved() == 0;
    assert ASK1.getValueRemoved() == 0;
    assert ASK2.getValueRemoved() == 0;
  }

  @Test
  public void testBidMakers() {
    final List<Order> MAKERS = new LinkedList<>();
    final Order       BID0   = newBid("00", 12, 1);
    final Order       BID1   = newBid("01", 10, 3);
    final Order       BID2   = newBid("02", 10, 2);

    BID0.takeSize(BID0.getSize());
    BID1.takeSize(BID1.getSize());
    BID2.takeSize(BID2.getSize());

    MAKERS.add(BID0);
    MAKERS.add(BID1);
    MAKERS.add(BID2);

    final double     TAKE_SIZE  = BID0.getSize() + BID1.getSize() + BID2.getSize();
    final double     TAKE_VALUE = BID0.getValueRemoved() + BID1.getValueRemoved() + BID2.getValueRemoved();
    final TakeResult RESULT     = new TakeResult(MAKERS, TAKE_SIZE);

    assert RESULT.getTakeSize()      == TAKE_SIZE;
    assert RESULT.getTakeValue()     == TAKE_VALUE;
    assert RESULT.getMakers().size() == 3;

    RESULT.clearMakerValueRemoved();

    assert BID0.getValueRemoved() == 0;
    assert BID1.getValueRemoved() == 0;
    assert BID2.getValueRemoved() == 0;
  }

}