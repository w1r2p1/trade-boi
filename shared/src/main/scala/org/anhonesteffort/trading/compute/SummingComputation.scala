package org.anhonesteffort.trading.compute

import org.anhonesteffort.trading.state.GdaxState

import scala.collection.mutable

class SummingComputation(child: Computation[Double], periodMs: Long) extends Computation[Option[Double]](None) {

  private val history  : mutable.Queue[(Long, Double)] = new mutable.Queue[(Long, Double)]()
  private val periodNs : Long = periodMs * 1000 * 1000
  private var sum      : Double = 0d
  private var filled   : Boolean = false

  addChild(child)

  override def computeNextResult(state: GdaxState, ns: Long): Option[Double] = {
    if (isSyncing) { return None }

    sum += child.getResult
    history.enqueue((ns, child.getResult))

    while (history.nonEmpty && (ns - history.head._1) > periodNs) {
      filled = true
      sum -= history.dequeue()._2
    }

    if (filled) {
      Some(sum)
    } else {
      None
    }
  }

  override def onStateSyncStart(ns: Long): Unit = {
    super.onStateSyncStart(ns)
    history.clear()
    sum = 0d
    filled = false
  }

}
