# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class io.whyscape.lundo.data.db.** { *; }
-keep class io.whyscape.lundo.data.remote.dto.** { *; }
-keep class io.whyscape.lundo.domain.model.** { *; }
-keep @androidx.annotation.Keep class ** { *; }

# Dagger 2
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
#-keep class * extends dagger.internal.Binding
#-keep class * extends dagger.internal.ModuleAdapter
#-keep class * extends dagger.internal.StaticInjection

-keep class **$$ModuleAdapter { *; }
-keep class **$$InjectAdapter { *; }
-keep class **$$StaticInjection { *; }
-keep class **$$ComponentAdapter { *; }
-keep class **$$Factory { *; }
-keep class **$$MembersInjector { *; }

-keepclassmembers class * {
    @javax.inject.Inject *;
    @dagger.Provides *;
}

-keep @dagger.Component class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.multibindings.Multibinds class * { *; }
-keep @dagger.multibindings.IntoMap class * { *; }
-keep @dagger.multibindings.IntoSet class * { *; }

-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.slf4j.impl.StaticLoggerBinder

-keep class net.sqlcipher.** { *; }
-keep class net.zetetic.** { *; }