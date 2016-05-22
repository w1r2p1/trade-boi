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

package org.anhonesteffort.btc.compute;

import org.anhonesteffort.btc.state.CoinbaseState;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class Computation<T> {

  private final Set<Computation> children = new HashSet<>();
  protected T result;

  public Computation() { }
  public Computation(Computation ... children) {
    addChildren(children);
  }

  protected void addChildren(Computation ... children) {
    this.children.addAll(Arrays.asList(children));
  }

  protected abstract T computeResult(CoinbaseState state);

  protected void onResult(T result) { }

  public T getResult() {
    return result;
  }

  public void onStateChange(CoinbaseState state) {
    children.forEach(child -> child.onStateChange(state));
    result = computeResult(state);
    onResult(result);
  }

}
