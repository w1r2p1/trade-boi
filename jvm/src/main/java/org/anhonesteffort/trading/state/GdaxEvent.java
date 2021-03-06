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

package org.anhonesteffort.trading.state;

import org.anhonesteffort.trading.book.Orders.Side;

public class GdaxEvent {

  public enum Type {
    LIMIT_RX, MARKET_RX, LIMIT_OPEN,
    LIMIT_DONE, MARKET_DONE, MATCH,
    LIMIT_CHANGE, MARKET_CHANGE,
    REBUILD_START, REBUILD_END
  }

  private long   nanoseconds;
  private Type   type;
  private String orderId;
  private String clientOid;
  private Side   side;
  private double price;
  private double size;
  private double funds;
  private String makerId;
  private String takerId;
  private double oldSize;
  private double newSize;
  private double oldFunds;
  private double newFunds;

  // F12
  private void init(
      long nanoseconds, Type type, String orderId, String clientOid, Side side,
      double price, double size, double funds, String makerId, String takerId,
      double oldSize, double newSize, double oldFunds, double newFunds
  ) {
    this.nanoseconds = nanoseconds;
    this.type        = type;
    this.orderId     = orderId;
    this.clientOid   = clientOid;
    this.side        = side;
    this.price       = price;
    this.size        = size;
    this.funds       = funds;
    this.makerId     = makerId;
    this.takerId     = takerId;
    this.oldSize     = oldSize;
    this.newSize     = newSize;
    this.oldFunds    = oldFunds;
    this.newFunds    = newFunds;
  }

  public void initLimitRx(long nanoseconds, String orderId, String clientOid, Side side, double price, double size) {
    init(nanoseconds, Type.LIMIT_RX, orderId, clientOid, side, price, size, -1d, null, null, -1d, -1d, -1d, -1d);
  }

  public void initMarketRx(long nanoseconds, String orderId, Side side, double size, double funds) {
    init(nanoseconds, Type.MARKET_RX, orderId, null, side, -1d, size, funds, null, null, -1d, -1d, -1d, -1d);
  }

  public void initLimitOpen(long nanoseconds, String orderId, Side side, double price, double openSize) {
    init(nanoseconds, Type.LIMIT_OPEN, orderId, null, side, price, openSize, -1d, null, null, -1d, -1d, -1d, -1d);
  }

  public void initLimitDone(long nanoseconds, String orderId, Side side, double price, double doneSize) {
    init(nanoseconds, Type.LIMIT_DONE, orderId, null, side, price, doneSize, -1d, null, null, -1d, -1d, -1d, -1d);
  }

  public void initMarketDone(long nanoseconds, String orderId, Side side) {
    init(nanoseconds, Type.MARKET_DONE, orderId, null, side, -1d, -1d, -1d, null, null, -1d, -1d, -1d, -1d);
  }

  public void initMatch(long nanoseconds, String makerId, String takerId, Side side, double price, double size) {
    init(nanoseconds, Type.MATCH, null, null, side, price, size, -1d, makerId, takerId, -1d, -1d, -1d, -1d);
  }

  public void initLimitChange(long nanoseconds, String orderId, Side side, double price, double oldSize, double newSize) {
    init(nanoseconds, Type.LIMIT_CHANGE, orderId, null, side, price, -1d , -1d, null, null, oldSize, newSize, -1d, -1d);
  }

  public void initMarketChange(long nanoseconds, String orderId, Side side, double oldSize, double newSize, double oldFunds, double newFunds) {
    init(nanoseconds, Type.MARKET_CHANGE, orderId, null, side, -1d, -1d, -1d, null, null, oldSize, newSize, oldFunds, newFunds);
  }

  public void initRebuildStart(long nanoseconds) {
    init(nanoseconds, Type.REBUILD_START, null, null, null, -1d, -1d, -1d, null, null, -1d, -1d, -1d, -1d);
  }

  public void initRebuildEnd(long nanoseconds) {
    init(nanoseconds, Type.REBUILD_END, null, null, null, -1d, -1d, -1d, null, null, -1d, -1d, -1d, -1d);
  }

  public long getNanoseconds() {
    return nanoseconds;
  }

  public Type getType() {
    return type;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getClientOid() {
    return clientOid;
  }

  public Side getSide() {
    return side;
  }

  public double getPrice() {
    return price;
  }

  public double getSize() {
    return size;
  }

  public double getFunds() {
    return funds;
  }

  public String getMakerId() {
    return makerId;
  }

  public String getTakerId() {
    return takerId;
  }

  public double getOldSize() {
    return oldSize;
  }

  public double getNewSize() {
    return newSize;
  }

  public double getOldFunds() {
    return oldFunds;
  }

  public double getNewFunds() {
    return newFunds;
  }

}
