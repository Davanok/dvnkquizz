# ==== Bouncy Castle ====
-dontwarn org.bouncycastle.**

# ==== Conscrypt ====
-dontwarn org.conscrypt.**

# ==== OpenJSSE ====
-dontwarn org.openjsse.**

# ==== GraalVM native-image ====
-dontwarn org.graalvm.nativeimage.**
-dontwarn com.oracle.svm.core.annotate.**

# ==== OkHttp optional platforms ====
-dontwarn okhttp3.internal.graal.**
-dontwarn okhttp3.internal.platform.**

# ==== cryptography-kotlin ====
-dontwarn dev.whyoleg.cryptography.**

# ==== Ktor optional JVM internals ====
-dontwarn io.ktor.**

# ==== JNA reflection ====
-dontwarn com.sun.jna.**

# ==== SLF4J ====
-dontwarn org.slf4j.**

# ==== DBus ====
-dontwarn org.freedesktop.dbus.**