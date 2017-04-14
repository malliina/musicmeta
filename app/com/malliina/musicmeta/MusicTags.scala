package com.malliina.musicmeta

import com.malliina.play.tags.{Bootstrap, PlayTags, TagPage, Tags}
import controllers.routes
import controllers.routes.Assets.at
import play.api.mvc.Call

import scalatags.Text.TypedTag
import scalatags.Text.all._

object MusicTags extends MusicTags

trait MusicTags extends Tags with PlayTags with Bootstrap {

  def index = baseIndex("home")(
    headerRow()("Home"),
    fullRow(
      leadPara("Hello")
    )
  )

  def logs(feedback: Option[UserFeedback]) = baseIndex("logs")(
    headerRow()("Logs"),
    fullRow(
      feedback.fold(empty)(feedbackDiv),
      p(id := "status", `class` := Lead)("Initializing...")
    ),
    fullRow(
      headeredTable(TableStripedHoverResponsive, Seq("Time", "Message", "Logger", "Level"))(
        tbody(id := "log-table-body")
      )
    ),
    jsScript(at("js/rx.js"))
  )

  def eject(feedback: Option[UserFeedback]) =
    basePage("Goodbye!")(
      divContainer(
        halfRow(
          feedback.fold(empty)(feedbackDiv),
          p("Try to ", aHref(routes.MetaOAuth.logs())("sign in"), " again.")
        )
      )
    )

  def baseIndex(tabName: String)(content: Modifier*) = {
    def navItem(thisTabName: String, tabId: String, url: Call, glyphiconName: String) = {
      val maybeActive = if (tabId == tabName) Option(`class` := "active") else None
      li(maybeActive)(a(href := url)(glyphIcon(glyphiconName), s" $thisTabName"))
    }

    basePage("MusicPimp")(
      divClass(s"$Navbar $NavbarDefault")(
        divContainer(
          divClass(NavbarHeader)(
            hamburgerButton,
            aHref(routes.MetaOAuth.index(), `class` := NavbarBrand)("musicmeta")
          ),
          divClass(s"$NavbarCollapse $Collapse")(
            ulClass(s"$Nav $NavbarNav")(
              navItem("Home", "home", routes.MetaOAuth.index(), "home"),
              navItem("Logs", "logs", routes.MetaOAuth.logs(), "list")
            ),
            ulClass(s"$Nav $NavbarNav $NavbarRight")(
              liHref(routes.MetaOAuth.logout())("Logout")
            )
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
        cssLink("//netdna.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css"),
        cssLink("//netdna.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap-theme.min.css"),
        cssLink(at("css/footer.css")),
        cssLink(at("css/custom.css")),
        jsScript("//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"),
        jsScript("//netdna.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js")
      ),
      body(
        div(id := "wrap")(
          content,
          div(id := "push")
        ),
        div(id := "footer")(
          divContainer(
            pClass("muted credit pull-right")("Developed by ", aHref("https://www.mskogberg.info")("Michael Skogberg"), ".")
          )
        )
      )
    )
  )

  def feedbackDiv(feedback: UserFeedback): TypedTag[String] = {
    val message = feedback.message
    if (feedback.isError) alertDanger(message)
    else alertSuccess(message)
  }
}
