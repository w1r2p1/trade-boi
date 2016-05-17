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

import java.util.List;

public class LimitTest {

  private Order newOrder(String orderId, double size) {
    return new Order(orderId, Order.Side.BID, 10.20, size);
  }

  @Test
  public void testGettersAndAddRemoveVolume() {
    final Limit LIMIT = new Limit(10.20);

    assert LIMIT.getPrice()  == 10.20;
    assert LIMIT.getVolume() == 0;

    LIMIT.add(newOrder("00", 10));
    assert LIMIT.getVolume() == 10;
    LIMIT.add(newOrder("01", 20));
    assert LIMIT.getVolume() == 30;

    LIMIT.remove("00");
    assert LIMIT.getVolume() == 20;
    LIMIT.remove("01");
    assert LIMIT.getVolume() == 0;
  }

  @Test
  public void testTakerWithNoMaker() {
    final Limit       LIMIT   = new Limit(10.20);
    final Order       TAKER1  = newOrder("00", 10);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getRemaining() == 10;
    assert MAKERS1.size()        == 0;
  }

  @Test
  public void testOneFullMakeOneFullTake() {
    final Limit LIMIT = new Limit(10.20);

    LIMIT.add(newOrder("00", 10));

    final Order       TAKER1  = newOrder("01", 10);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getRemaining()         == 0;
    assert MAKERS1.size()                == 1;
    assert MAKERS1.get(0).getRemaining() == 0;
    assert LIMIT.getVolume()             == 0;
  }

  @Test
  public void testPartialTake() {
    final Limit LIMIT = new Limit(10.20);

    LIMIT.add(newOrder("00", 10));

    final Order       TAKER1  = newOrder("01", 8);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getRemaining()         == 0;
    assert MAKERS1.size()                == 1;
    assert MAKERS1.get(0).getRemaining() == 2;
    assert LIMIT.getVolume()             == 2;
  }

  @Test
  public void testOneFullTakeOnePartialTake() {
    final Limit LIMIT = new Limit(10.20);

    LIMIT.add(newOrder("00", 10));

    final Order       TAKER1  = newOrder("01", 8);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getRemaining()         == 0;
    assert MAKERS1.size()                == 1;
    assert MAKERS1.get(0).getRemaining() == 2;
    assert LIMIT.getVolume()             == 2;

    final Order       TAKER2  = newOrder("02", 4);
    final List<Order> MAKERS2 = LIMIT.takeLiquidity(TAKER2);

    assert TAKER2.getRemaining()         == 2;
    assert MAKERS2.size()                == 1;
    assert MAKERS2.get(0).getRemaining() == 0;
    assert LIMIT.getVolume()             == 0;
  }

  @Test
  public void testTwoFullMakesOneFullTake() {
    final Limit LIMIT = new Limit(10.20);

    LIMIT.add(newOrder("00", 10));
    LIMIT.add(newOrder("01", 30));

    final Order       TAKER1  = newOrder("02", 40);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getRemaining()         == 0;
    assert MAKERS1.size()                == 2;
    assert MAKERS1.get(0).getRemaining() == 0;
    assert MAKERS1.get(1).getRemaining() == 0;
    assert LIMIT.getVolume()             == 0;
  }

  @Test
  public void testOneFullMakeOnePartialMakeOneFullTake() {
    final Limit LIMIT = new Limit(10.20);

    LIMIT.add(newOrder("00", 10));
    LIMIT.add(newOrder("01", 30));

    final Order       TAKER1  = newOrder("02", 30);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getRemaining()         ==  0;
    assert MAKERS1.size()                ==  2;
    assert MAKERS1.get(0).getRemaining() ==  0;
    assert MAKERS1.get(1).getRemaining() == 10;
    assert LIMIT.getVolume()             == 10;
  }

}
