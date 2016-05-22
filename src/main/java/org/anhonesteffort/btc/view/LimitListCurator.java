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

package org.anhonesteffort.btc.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.util.LongCaster;

import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;

public class LimitListCurator implements Observer {

  private final ObservableList<LimitView> limits       = FXCollections.observableArrayList();
  private final SortedList<LimitView>     sortedLimits = new SortedList<>(limits, new AskSorter());

  private final LimitOrderBook orderBook;
  private final LongCaster caster;

  public LimitListCurator(LimitOrderBook orderBook, LongCaster caster) {
    this.orderBook = orderBook;
    this.caster    = caster;
    orderBook.getAskLimits().addObserver(this);
    orderBook.getBidLimits().addObserver(this);
  }

  public SortedList<LimitView> getLimits() {
    return sortedLimits;
  }

  @Override
  public void update(Observable o, Object arg) {
    Platform.runLater(() -> {
      limits.clear();
      orderBook.getAskLimits().stream().map(
          limit -> new LimitView(limit, caster)
      ).limit(10).forEach(limits::add);
    });
  }

  private static class AskSorter implements Comparator<LimitView> {
    @Override
    public int compare(LimitView ask1, LimitView ask2) {
      if (ask1.getPrice() < ask2.getPrice()) {
        return -1;
      } else if (ask1.getPrice() == ask2.getPrice()) {
        return 0;
      } else {
        return 1;
      }
    }
  }

  private static class BidSorter implements Comparator<LimitView> {
    @Override
    public int compare(LimitView bid1, LimitView bid2) {
      if (bid1.getPrice() > bid2.getPrice()) {
        return -1;
      } else if (bid1.getPrice() == bid2.getPrice()) {
        return 0;
      } else {
        return 1;
      }
    }
  }

}