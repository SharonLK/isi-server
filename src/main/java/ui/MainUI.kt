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

        stage.width = 1280.0
        stage.height = 720.0

        stage.show()
        stage.isMaximized = true
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
                    fontSize = Dimension(4.0, Dimension.LinearUnits.em)
                    fontWeight = FontWeight.BOLD
                    textFill = c("#282828")
                }
            }

            label("http://127.0.0.1:8080/function/simple-echo") {
                style {
                    fontSize = Dimension(1.5, Dimension.LinearUnits.em)
                    fontStyle = FontPosture.ITALIC
                    textFill = c("#58564C")
                }
            }

            separator(Orientation.HORIZONTAL)

            textarea("def handler(st):\n\tprint(\"Hello World\")\n\n") {
                maxHeightProperty().set(Double.MAX_VALUE)
                maxWidthProperty().set(Double.MAX_VALUE)
                vgrow = Priority.ALWAYS
                font = Font.font("monospace", 16.0)
            }

            separator(Orientation.HORIZONTAL)

            hbox {
                spacingProperty().set(20.0)

                button("Download") {
                    prefHeightProperty().set(60.0)
                    prefWidthProperty().set(150.0)
                    style {
                        fontSize = Dimension(1.5, Dimension.LinearUnits.em)
                        backgroundColor += c("#79B0C0")
                        textFill = c("#FFFFFF")
                        fontWeight = FontWeight.BOLD
                    }
                }

                button("Re-submit") {
                    prefHeightProperty().set(60.0)
                    prefWidthProperty().set(150.0)
                    style {
                        fontSize = Dimension(1.5, Dimension.LinearUnits.em)
                        backgroundColor += c("#7EAB75")
                        textFill = c("#FFFFFF")
                        fontWeight = FontWeight.BOLD
                    }
                }

                pane {
                    hgrow = Priority.ALWAYS
                }

                button("Remove") {
                    prefHeightProperty().set(60.0)
                    prefWidthProperty().set(150.0)
                    style {
                        fontSize = Dimension(1.5, Dimension.LinearUnits.em)
                        backgroundColor += c("#AE4338")
                        textFill = c("#FFFFFF")
                        fontWeight = FontWeight.BOLD
                    }
                }
            }
        }
    }
}
