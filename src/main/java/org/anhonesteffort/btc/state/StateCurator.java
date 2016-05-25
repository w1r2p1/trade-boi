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

package org.anhonesteffort.btc.state;

import com.lmax.disruptor.EventHandler;
import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.book.OrderPool;
import org.anhonesteffort.btc.compute.Computation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public abstract class StateCurator implements EventHandler<OrderEvent> {

  private static final Logger log = LoggerFactory.getLogger(StateCurator.class);

  protected final State            state;
  protected final OrderPool        pool;
  protected final Set<Computation> computations;

  private boolean rebuilding    = false;
  private long    nanosecondSum = 0l;

  public StateCurator(LimitOrderBook book, OrderPool pool, Set<Computation> computations) {
    state             = new State(book);
    this.pool         = pool;
    this.computations = computations;
  }

  protected boolean isRebuilding() {
    return rebuilding;
  }

  protected void returnPooledOrder(Order order) {
    pool.returnOrder(order);
  }

  private void returnTakersAndMakers() {
    state.getTakes().forEach(take -> {
      returnPooledOrder(take.getTaker());
      take.getMakers().forEach(maker -> {
        if (maker.getSizeRemaining() <= 0l) {
          returnPooledOrder(maker);
        } else {
          maker.clearValueRemoved();
        }
      });
    });
    state.getTakes().clear();
  }

  protected abstract void onEvent(OrderEvent event) throws OrderEventException;

  @Override
  public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws OrderEventException {
    switch (event.getType()) {
      case REBUILD_START:
        state.clear();
        pool.returnAll();
        rebuilding = true;
        log.info("rebuilding order book");
        computations.forEach(Computation::onStateReset);
        break;

      case REBUILD_END:
        rebuilding = false;
        log.info("order book rebuild complete");
        break;

      default:
        onEvent(event);
        if (!rebuilding) {
          computations.forEach(compute -> compute.onStateChange(state, event.getNanoseconds()));
        }
        returnTakersAndMakers();
    }

    if ((sequence % 50l) == 0l) {
      if (!rebuilding) { log.info("avg latency -> " + (nanosecondSum / 50d) + "ns"); }
      nanosecondSum = System.nanoTime() - event.getNanoseconds();
    } else {
      nanosecondSum += System.nanoTime() - event.getNanoseconds();
    }
  }

}
