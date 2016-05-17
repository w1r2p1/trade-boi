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

public class MarketOrderTest {

  @Test
  public void testWithSize() {
    final MarketOrder ORDER = MarketOrder.withSize("lol", Order.Side.BID, 100);

    assert ORDER.getOrderId().equals("lol");
    assert ORDER.getSide().equals(Order.Side.BID);
    assert ORDER.getPrice()          <   0;
    assert ORDER.getSize()          == 100;
    assert ORDER.getSizeRemaining() == 100;
    assert ORDER.getValueRemoved()  ==   0;

    assert ORDER.getFunds()                  <   0;
    assert ORDER.getFundsRemaining()         <   0;
    assert ORDER.getVolumeRemoved()         ==   0;
    assert ORDER.getSizeRemainingFor(1.337) == 100;

    ORDER.subtract(75, 1.337);

    assert ORDER.getVolumeRemoved()         == 75;
    assert ORDER.getSizeRemainingFor(1.337) == 25;

    ORDER.subtract(25, 31.337);

    assert ORDER.getVolumeRemoved()          == 100;
    assert ORDER.getSizeRemainingFor(31.337) ==   0;
    assert ORDER.getSizeRemaining()          ==   0;
    assert ORDER.getValueRemoved()           ==   0;
  }

  @Test
  public void testWithFunds() {
    final MarketOrder ORDER = MarketOrder.withFunds("lol", Order.Side.BID, 100);

    assert ORDER.getOrderId().equals("lol");
    assert ORDER.getSide().equals(Order.Side.BID);
    assert ORDER.getPrice()          < 0;
    assert ORDER.getSize()           < 0;
    assert ORDER.getSizeRemaining()  < 0;
    assert ORDER.getValueRemoved()  == 0;

    assert ORDER.getFunds()              == 100;
    assert ORDER.getFundsRemaining()     == 100;
    assert ORDER.getVolumeRemoved()      ==   0;
    assert ORDER.getSizeRemainingFor(25) ==   4;

    ORDER.subtract(3, 25);

    assert ORDER.getVolumeRemoved()      == 3;
    assert ORDER.getSizeRemainingFor(25) == 1;

    ORDER.subtract(1, 25);

    assert ORDER.getVolumeRemoved()     ==  4;
    assert ORDER.getSizeRemainingFor(1) ==  0;
    assert ORDER.getSizeRemaining()      <  0;
    assert ORDER.getValueRemoved()      ==  0;
  }

  @Test
  public void testWithSizeAndFunds() {
    final MarketOrder ORDER = new MarketOrder("lol", Order.Side.BID, 100, 50);

    assert ORDER.getOrderId().equals("lol");
    assert ORDER.getSide().equals(Order.Side.BID);
    assert ORDER.getPrice()          <   0;
    assert ORDER.getSize()          == 100;
    assert ORDER.getSizeRemaining() == 100;
    assert ORDER.getValueRemoved()  ==   0;

    assert ORDER.getFunds()               ==  50;
    assert ORDER.getFundsRemaining()      ==  50;
    assert ORDER.getVolumeRemoved()       ==   0;
    assert ORDER.getSizeRemainingFor(1)   ==  50;
    assert ORDER.getSizeRemainingFor(0.5) == 100;

    ORDER.subtract(50, 0.5);

    assert ORDER.getVolumeRemoved()       == 50;
    assert ORDER.getSizeRemainingFor(0.5) == 50;

    ORDER.subtract(25, 1);

    assert ORDER.getVolumeRemoved()     == 75;
    assert ORDER.getSizeRemainingFor(1) ==  0;
    assert ORDER.getSizeRemaining()     == 25;
    assert ORDER.getValueRemoved()      ==  0;
  }

}