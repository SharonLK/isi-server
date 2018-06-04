package ui

import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import tornadofx.*

fun main(args: Array<String>) {
    launch<Screen>(args)
}

class Screen : App(HelloWorld::class, InternalWindow.Styles::class) {
    override fun start(stage: Stage) {
        super.start(stage)
    }
}

class HelloWorld : View() {
    override val root = hbox {
        spacingProperty().set(20.0)

        listview<String> {
            items.add("Ping")
            items.add("Simple Echo")
            items.add("Cat")
        }

        vbox {
            spacingProperty().set(10.0)
            paddingProperty().set(Insets(10.0, 20.0, 10.0, 0.0))
            hgrow = Priority.ALWAYS

            label("Simple Echo") {
                style {
                    fontSize = Dimension(3.0, Dimension.LinearUnits.em)
                    fontWeight = FontWeight.BOLD
                }
            }
            label("http://127.0.0.1:8080/function/simple-echo") {
                style {
                    fontStyle = FontPosture.ITALIC
                }
            }
            separator(Orientation.HORIZONTAL) {
                paddingProperty().set(Insets(0.0, 10.0, 0.0, 10.0))
            }
            textarea("def handler(st):\n\tprint(\"Hello World\")\n\n") {
                maxHeightProperty().set(Double.MAX_VALUE)
                maxWidthProperty().set(Double.MAX_VALUE)
                vgrow = Priority.ALWAYS
                font = Font.font("Verdana")
            }
            separator(Orientation.HORIZONTAL) {
                paddingProperty().set(Insets(0.0, 10.0, 0.0, 10.0))
            }

            hbox {
                spacingProperty().set(20.0)

                button("Download") {
                    prefHeightProperty().set(40.0)
                    prefWidthProperty().set(100.0)
                    style {
                        backgroundColor += c("#79B0C0")
                        textFill = c("#FFFFFF")
                        fontWeight = FontWeight.BOLD
                    }
                }
                button("Remove") {
                    prefHeightProperty().set(40.0)
                    prefWidthProperty().set(100.0)
                    style {
                        backgroundColor += c("#AE4338")
                        textFill = c("#FFFFFF")
                        fontWeight = FontWeight.BOLD
                    }
                }
            }
        }
    }
}
