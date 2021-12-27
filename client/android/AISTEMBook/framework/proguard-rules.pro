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
-keepattributes EnclosingMethod
-keepattributes InnerClasses

#keep callback method
-keepclassmembers class * implements com.aispeech.AIEngine$aiengine_callback{public int run(byte[] , int , byte[] , int);}

-keepnames class com.aispeech.AIEngine$*{
	public <fields>;
	public <methods>;
}

#keep native method
-keepclassmembers class com.aispeech.AIEngine{
	public static native <methods>;
}
-keep public class com.aispeech.common.AIMacUtils{public *;}