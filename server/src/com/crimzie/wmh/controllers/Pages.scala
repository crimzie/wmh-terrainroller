package com.crimzie.wmh
package controllers

import com.crimzie.wmh.model.Chicken
import scalatags.Text
import scalatags.Text.all._

object Pages {
  val indexPage: String =
    html(
      head(),
      body(
        p(a(href := "/chicken")("List chicken")),
        p(a(href := "/terrain")("Terrain randomizer")),
      )).render
  private val playerForm: String => Text.TypedTag[String] = u =>
    form(
      "Your name:", br,
      input(`type` := "text", name := "name", autofocus, required), br,
      "List 1:", br,
      textarea(`type` := "text", name := "list1", required), br,
      "List 2:", br,
      textarea(`type` := "text", name := "list2", required), br,
      input(`type` := "submit", value := "Submit"),
      action := s"/chicken/$u",
      method := "post",
    )
  val newChickenPage: String =
    html(
      head(),
      body(
        p("You are initiating a list chicken. Fill in the following form and " +
          "then send to the player you wish to challenge the link that will b" +
          "e provided. All lists, scenario, and randomized terrain will be re" +
          "vealed to both of you once your opponent submits his lists."),
        playerForm("new"),
      )).render
  val chickenUrlPage: String => String = id =>
    html(
      head(),
      body(
        "Your initiated list chicken challenge can be viewed via this ",
        a(href := s"/chicken/$id")("link"),
        ". Send it to your opponent so that he can submit his lists.",
      )).render
  val challengePage: (String, String) => String = (c, id) =>
    html(
      head(),
      body(
        p(s"$c challenges you to a list chicken! Once you submit your list pa" +
          "ir through the form below, and all four lists, scenario, and terra" +
          "in setup will be revealed to both of you."),
        playerForm(id),
      )).render
  val chickenPage: (String, Chicken) => String = (id, chi) =>
    html(
      head(),
      body(
        h3(chi.playerA.name),
        h4("List A:"),
        code(chi.playerA.list1, whiteSpace.`pre-wrap`), br,
        h4("List B:"),
        code(chi.playerA.list2, whiteSpace.`pre-wrap`), br,
        h3(chi.playerB.name),
        h4("List A:"),
        code(chi.playerB.list1, whiteSpace.`pre-wrap`), br,
        h4("List B:"),
        code(chi.playerB.list2, whiteSpace.`pre-wrap`), br,
        h3("Scenario and terrain:"),
        img(src := s"/terrain/$id")
      )).render
}
