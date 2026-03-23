import androidx.compose.ui.window.ComposeUIViewController
import com.davanok.dvnkquizz.ui.di.IosAppGraph
import dev.zacsweers.metro.createGraph
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.UIViewController
import platform.UIKit.setStatusBarStyle

fun MainViewController(): UIViewController {
    val app = createGraph<IosAppGraph>().app
    return ComposeUIViewController {
        app(onThemeChanged = ::onThemeChanged)
    }
}

private fun onThemeChanged(isDark: Boolean) {
    UIApplication.sharedApplication.setStatusBarStyle(
        if (isDark) UIStatusBarStyleDarkContent else UIStatusBarStyleLightContent
    )
}