package com.crimzie.wmh
package model

case class ChickenData(
    playerA: Player,
    playerB: Option[Player],
) {
  def state: ChickenState =
    ChickenState(playerA.name, playerB map { b => Chicken(playerA, b) })
}
