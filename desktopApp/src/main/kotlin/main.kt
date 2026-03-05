import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.davanok.dvnkquizz.di.JvmAppGraph
import dev.zacsweers.metro.createGraph
import java.awt.Dimension

fun main() {
    val app = createGraph<JvmAppGraph>().app
    application {
        Window(
            title = "DVNKQuizz",
            state = rememberWindowState(width = 800.dp, height = 600.dp),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(350, 600)
            app()
        }
    }
}

