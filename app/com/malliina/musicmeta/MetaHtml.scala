package com.malliina.musicmeta

import com.malliina.html.{Bootstrap, Tags}
import com.malliina.play.tags.TagPage
import controllers.routes
import controllers.routes.MetaAssets.versioned
import play.api.mvc.Call

import scalatags.Text.{GenericAttr, TypedTag}
import scalatags.Text.all._

object MetaHtml extends MetaHtml

class MetaHtml extends Bootstrap(Tags) {
  import tags._

  implicit val callAttr = new GenericAttr[Call]
  val empty: Modifier = ()

  def index = baseIndex("home")(
    headerRow("Home"),
    fullRow(
      leadPara("Hello")
    )
  )

  def logs(feedback: Option[UserFeedback]) = baseIndex("logs")(
    headerRow("Logs"),
    fullRow(
      feedback.fold(empty)(feedbackDiv),
      p(id := "status", `class` := Lead)("Initializing...")
    ),
    fullRow(
      table(`class` := tables.stripedHoverResponsive)(
        thead(tr(Seq("Time", "Message", "Logger", "Level").map(h => th(h)))),
        tbody(id := "log-table-body")
      )
    ),
    jsScript(versioned("js/rx.js"))
  )

  def eject(feedback: Option[UserFeedback]) =
    basePage("Goodbye!")(
      divContainer(
        halfRow(
          feedback.fold(empty)(feedbackDiv),
          p("Try to ", a(href := routes.MetaOAuth.logs())("sign in"), " again.")
        )
      )
    )

  def baseIndex(tabName: String)(content: Modifier*) = {
    def navItem(thisTabName: String, tabId: String, url: Call, iconicName: String) = {
      val itemClass = if (tabId == tabName) "nav-item active" else "nav-item"
      li(`class` := itemClass)(a(href := url, `class` := "nav-link")(iconic(iconicName), s" $thisTabName"))
    }

    basePage("MusicPimp")(
      navbar.basic(
        routes.MetaOAuth.index(),
        "musicmeta",
          modifier(
            ulClass(s"${navbar.Nav} $MrAuto")(
              navItem("Home", "home", routes.MetaOAuth.index(), "home"),
              navItem("Logs", "logs", routes.MetaOAuth.logs(), "list")
            ),
            ulClass(s"${navbar.Nav} ${navbar.Right}")(
              li(`class` := "nav-item")(a(href := routes.MetaOAuth.logout(), `class` := "nav-link")("Logout"))
            )
        )
      ),
      divContainer(content)
    )
  }

  def basePage(title: String)(content: Modifier*) = TagPage(
    html(lang := En)(
      head(
        titleTag(title),
        deviceWidthViewport,
        cssLinkHashed("https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css", "sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm"),
        cssLink("https://use.fontawesome.com/releases/v5.0.6/css/all.css"),
        cssLink(versioned("css/main.css")),
        jsHashed("https://code.jquery.com/jquery-3.2.1.slim.min.js", "sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN"),
        jsHashed("https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js", "sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q"),
        jsHashed("https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js", "sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl")
      ),
      body(
        content,
        footer(`class` := "footer")(
          divClass(Container)(
            spanClass(s"${text.muted} float-right")("Developed by ", a(href := "https://github.com/malliina")("Michael Skogberg"), ".")
          )
        )
      )
    )
  )

  def feedbackDiv(feedback: UserFeedback) = {
    val message = feedback.message
    if (feedback.isError) alertDanger(message)
    else alertSuccess(message)
  }
}