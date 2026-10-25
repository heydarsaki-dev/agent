# R8 rules for release shrinking. Keep the minimum: everything reachable from the
# manifest is kept automatically; only reflective entry points need help here.

# BouncyCastle is a JCE provider: its algorithms are looked up by name through
# reflection when the provider registers, so R8 cannot see those references and
# would strip classes the SSH stack needs at runtime.
-keep class org.bouncycastle.jce.provider.BouncyCastleProvider { *; }
-keep class org.bouncycastle.jcajce.provider.** { *; }
-keep class org.bouncycastle.crypto.** { *; }
-keep class org.bouncycastle.util.** { *; }
# R8 keeps META-INF/services/** service-loader files itself, so BouncyCastleProvider
# stays discoverable without an explicit resource rule.

# sshj probes optional transports/ciphers by reflection.
-keep class net.schmizz.sshj.** { *; }
-keep class com.hierynomus.sshj.** { *; }

# OkHttp/MockWebServer reference okhttp3 internal platforms reflectively.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.**
-dontwarn net.schmizz.**
# sshj's eddsa engine references a sun.security class that exists on the desktop JVM but
# not on Android; the code path is never taken here.
-dontwarn sun.security.x509.X509Key
