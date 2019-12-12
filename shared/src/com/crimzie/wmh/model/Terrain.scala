package com.crimzie.wmh
package model

sealed trait Terrain extends Product with Serializable

object Terrain {

  trait Standard extends Terrain
  trait Custom extends Terrain
  trait LosBlock

  case object Forest extends LosBlock with Standard
  case object Cloud extends LosBlock with Standard
  case object Obstruction extends LosBlock with Standard
  case object FireCloud extends LosBlock with Standard
  case object AcidCloud extends LosBlock with Custom
  case object FireForest extends LosBlock with Custom
  case object FireRubble extends LosBlock with Custom
  case object Fence extends Standard
  case object Rough extends Standard
  case object Rubble extends Standard
  case object Trench extends Standard
  case object Wall extends Standard
  case object Water extends Standard
  case object Acid extends Standard
  case object Other extends Custom

  val list: List[Terrain] = Obstruction :: Fence :: Wall :: Trench :: Rough ::
    Rubble :: FireRubble :: Forest :: FireForest :: Water :: Acid :: Cloud ::
    AcidCloud :: FireCloud :: Other :: Nil

  val adt2param: Map[Terrain, String] = Map(
    Forest -> "forest",
    Cloud -> "cloud",
    Obstruction -> "obstr",
    Fence -> "fence",
    Rough -> "rough",
    Rubble -> "rubble",
    Trench -> "trench",
    Wall -> "wall",
    Water -> "water",
    Acid -> "acid",
    FireCloud -> "bcloud",
    AcidCloud -> "ccloud",
    FireForest -> "bforest",
    FireRubble -> "frubble",
    Other -> "other",
  )

  val param2adt: Map[String, Terrain] = adt2param map { _.swap }

  val adt2name: Map[Terrain, String] = Map(
    Forest -> "Forest",
    Cloud -> "Dense Fog",
    Obstruction -> "Obstruction",
    Fence -> "Obstacle, Concealment",
    Rough -> "Rough Terrain",
    Rubble -> "Rubble",
    Trench -> "Trench",
    Wall -> "Obstacle, Cover",
    Water -> "Shallow Water",
    Acid -> "Acid Bath",
    FireCloud -> "Burning Earth",
    AcidCloud -> "Acid Cloud",
    FireForest -> "Burning Forest",
    FireRubble -> "Burning Rubble",
    Other -> "Custom",
  )
}

