# --- Optional crypto providers ---
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# --- GraalVM native image support ---
-dontwarn org.graalvm.nativeimage.**
-dontwarn com.oracle.svm.core.annotate.**

# --- OkHttp optional platforms ---
-dontwarn okhttp3.internal.platform.**
-dontwarn okhttp3.internal.graal.**

# --- Cryptography library optional BC bridge ---
-dontwarn dev.whyoleg.cryptography.providers.jdk.internal.**

# --- DBus optional reflection usage ---
-dontwarn org.freedesktop.dbus.**

# --- JNA reflection ---
-dontwarn com.sun.jna.**

# --- Ktor optional internals ---
-dontwarn io.ktor.**

# --- SLF4J dynamic access ---
-dontwarn org.slf4j.**

# --- Typesafe config reflection ---
-dontwarn com.typesafe.config.**