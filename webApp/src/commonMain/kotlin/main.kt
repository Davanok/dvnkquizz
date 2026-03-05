import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.davanok.dvnkquizz.di.WebAppGraph
import dev.zacsweers.metro.createGraph

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val app = createGraph<WebAppGraph>().app
    ComposeViewport { app() }
}
