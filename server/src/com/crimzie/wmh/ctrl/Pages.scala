package com.crimzie.wmh
package ctrl

import com.crimzie.wmh.model.Terrain
import scalatags.Text
import scalatags.Text.all._

object Pages {

  private val terrBoxes: Terrain => ConcreteHtmlTag[String] = t => {
    val cb = input(
      `type` := "checkbox",
      name := "t",
      value := Terrain.adt2param(t),
      checked,
    )
    p(cb, cb, Terrain.adt2name(t))
  }

  val indexPage: String =
    html(
      head(
        link(href := "style.css", rel := "stylesheet"),
        meta(content := "width=device-width, initial-scale=1", name := "viewport"),
      ),
      body(
        div(`class` := "header__block"),
        tag("section")(
          //div(
          //  h3("List chicken"),
          //  ul(li(a(href := "/chicken")("Initiate"))),
          //  `class` := "list",
          //),
          div(
            h3("Random terrain generator"),
            "Pick available terrain elements (at least 7 is recommended):", br,
            form(
              terrBoxes(Terrain.LosBlock.Forest),
              terrBoxes(Terrain.LosBlock.Obstruction),
              terrBoxes(Terrain.LosBlock.Cloud),
              terrBoxes(Terrain.Other.Water),
              terrBoxes(Terrain.Other.Trench),
              terrBoxes(Terrain.Other.Rubble),
              terrBoxes(Terrain.Other.Rough),
              terrBoxes(Terrain.Other.Wall),
              terrBoxes(Terrain.Other.Fence),
              ul(
                li(input(
                  `type` := "submit",
                  formaction := "/terrain",
                  value := "Random scenario",
                )),
                li(input(
                  `type` := "submit",
                  formaction := "/terrain/1",
                  value := "Scenario 1: King Of The Hill",
                )),
                li(input(
                  `type` := "submit",
                  formaction := "/terrain/2",
                  value := "Scenario 2: Bunkers",
                )),
                li(input(
                  `type` := "submit",
                  formaction := "/terrain/3",
                  value := "Scenario 3: Spread The Net",
                )),
                li(input(
                  `type` := "submit",
                  formaction := "/terrain/4",
                  value := "Scenario 4: Invasion",
                )),
                li(input(
                  `type` := "submit",
                  formaction := "/terrain/5",
                  value := "Scenario 5: Anarchy",
                )),
                li(input(
                  `type` := "submit",
                  formaction := "/terrain/6",
                  value := "Scenario 6: Recon II",
                )),
              ),
            ),
            `class` := "random",
          ),
          `class` := "wrapper",
        ),
        div(`class` := "footer__block"),
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
          "ir through the form below, all four lists, scenario and terra" +
          "in setup for your game will be revealed."),
        playerForm(id),
      )).render

  val chickenPage: (String, model.Chicken) => String = (id, chi) =>
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
