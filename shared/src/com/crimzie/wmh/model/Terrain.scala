package com.crimzie.wmh
package model

sealed abstract class Terrain(val rsc: String) extends Product with Serializable

object Terrain {

  trait LosBlock
  object LosBlock {
    case object Forest extends Terrain("/ter-forest.png") with LosBlock
    case object Cloud extends Terrain("/ter-cloud.png") with LosBlock
    case object Obstruction extends Terrain("/ter-obstr.png") with LosBlock
  }

  object Other {
    case object Fence extends Terrain("/ter-fence.png")
    case object Rough extends Terrain("/ter-rubble.png")
    case object Rubble extends Terrain("/ter-rough.png")
    case object Trench extends Terrain("/ter-trench.png")
    case object Wall extends Terrain("/ter-wall.png")
    case object Water extends Terrain("/ter-water.png")
  }

  val adt2param: Map[Terrain, String] = Map(
      LosBlock.Forest -> "forest",
      LosBlock.Cloud -> "cloud",
      LosBlock.Obstruction -> "obstr",
      Other.Fence -> "fence",
      Other.Rough -> "rough",
      Other.Rubble -> "rubble",
      Other.Trench -> "trench",
      Other.Wall -> "wall",
      Other.Water -> "water",
  )

  val param2adt: Map[String, Terrain] = adt2param map { _.swap }

  val adt2name: Map[Terrain, String] = Map(
     LosBlock.Forest -> "Forest",
     LosBlock.Cloud -> "Cloud",
     LosBlock.Obstruction -> "Obstruction",
     Other.Fence -> "Obstacle, Concealment",
     Other.Rough -> "Rough Terrain",
     Other.Rubble -> "Rubble",
     Other.Trench -> "Trench",
     Other.Wall -> "Obstacle, Cover",
     Other.Water -> "Shallow Water",
  )
}

