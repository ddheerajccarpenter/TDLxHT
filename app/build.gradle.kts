import java.io.File
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
  id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.habittask.wvnk"
    minSdk = 24
    targetSdk = 35
    versionCode = 38
    versionName = "2.5.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    buildConfigField("String", "SUPABASE_URL", "\"${System.getenv("SUPABASE_URL") ?: project.findProperty("SUPABASE_URL") ?: "https://mtlckvkyimoqbrlvirch.supabase.co"}\"")
    buildConfigField("String", "SUPABASE_ANON_KEY", "\"${System.getenv("SUPABASE_ANON_KEY") ?: project.findProperty("SUPABASE_ANON_KEY") ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im10bGNrdmt5aW1vcWJybHZpcmNoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc5Njc0MTEsImV4cCI6MjEwMzU0MzQxMX0.gGxCER-GAzSVS6IMzKoy5p8NwuDbOZ2QiBzkEeG_-J4"}\"")
    buildConfigField("String", "APP_WEB_URL", "\"${System.getenv("APP_WEB_URL") ?: project.findProperty("APP_WEB_URL") ?: "https://tdlxht.vercel.app/"}\"")
    buildConfigField("String", "NAPKIN_API_KEY", "\"${System.getenv("NAPKIN_API_KEY") ?: project.findProperty("NAPKIN_API_KEY") ?: "sk-deff27b1093c44fc3a79b5d48cf3d3f470e2eaeca4baf9eb631f227455c88759"}\"")
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/tdlxht-release.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD") ?: "tdlxhtpassword"
      keyAlias = "tdlxhtkey"
      keyPassword = System.getenv("KEY_PASSWORD") ?: "tdlxhtpassword"
      enableV1Signing = true
      enableV2Signing = true
      enableV3Signing = true
      enableV4Signing = false
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // Keep default debug signing so users upgrading from existing debug builds can install without conflicts
      signingConfig = signingConfigs.getByName("debug")
    }
  }
  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin with safe fallback so removing .env does not break build
secrets {
  defaultPropertiesFileName = "secrets.defaults.properties"
  propertiesFileName = ".env"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
  ignoreList.add("SUPABASE_URL")
  ignoreList.add("SUPABASE_ANON_KEY")
  ignoreList.add("NAPKIN_API_KEY")
  ignoreList.add("GEMINI_API_KEY")
  ignoreList.add("APP_WEB_URL")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  // Supabase
  implementation(platform(libs.supabase.bom))
  implementation(libs.supabase.auth)
  implementation(libs.supabase.postgrest)
  implementation(libs.supabase.realtime)
  implementation(libs.supabase.storage)
  implementation(libs.ktor.android)
  implementation(libs.ktor.serialization)
  implementation(libs.kotlinx.serialization.json)
  // DataStore for settings persistence
  implementation(libs.androidx.datastore.preferences)
  // Core library desugaring
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
  // Lottie Compose for Iconly & Flaticon animated icons
  implementation("com.airbnb.android:lottie-compose:6.6.2")
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

tasks.register("syncReleaseApk") {
  group = "build"
  description = "Deletes older APKs and copies the release APK as TDLxHT.<version>.apk and TDlxHT.apk to root, web, and dist directories."
  dependsOn("assembleRelease")
  notCompatibleWithConfigurationCache("Performs file tree scan and multi-directory synchronization")
  doLast {
    val ver = android.defaultConfig.versionName ?: "2.3.3"
    val code = android.defaultConfig.versionCode ?: 20
    val targetFileName = "TDLxHT.$ver.apk"
    val genericFileName = "TDlxHT.apk"
    val releaseApk = file("${layout.buildDirectory.get()}/outputs/apk/release/app-release.apk")
    if (releaseApk.exists()) {
      val rootDir = rootProject.rootDir
      val allowedApkNames = setOf(targetFileName, genericFileName)
      rootDir.listFiles { f -> f.isFile && f.name.endsWith(".apk") && f.name !in allowedApkNames }?.forEach { it.delete() }
      file("$rootDir/web").listFiles { f -> f.isFile && f.name.endsWith(".apk") && f.name !in allowedApkNames }?.forEach { it.delete() }
      file("$rootDir/dist").listFiles { f -> f.isFile && f.name.endsWith(".apk") && f.name !in allowedApkNames }?.forEach { it.delete() }

      val rootTarget = File(rootDir, targetFileName)
      releaseApk.copyTo(rootTarget, overwrite = true)
      releaseApk.copyTo(File(rootDir, genericFileName), overwrite = true)

      val webDir = File(rootDir, "web")
      file("$rootDir/web").listFiles { f -> f.isFile && f.name.endsWith(".apk") }?.forEach { it.delete() }

      val distDir = File(rootDir, "dist")
      file("$rootDir/dist").listFiles { f -> f.isFile && f.name.endsWith(".apk") }?.forEach { it.delete() }

      val releaseOutputDir = File(layout.buildDirectory.get().asFile, "outputs/apk/release")
      releaseApk.copyTo(File(releaseOutputDir, targetFileName), overwrite = true)
      releaseApk.copyTo(File(releaseOutputDir, genericFileName), overwrite = true)

      // Automatically generate version.json for dynamic web auto-fetching pointing directly to GitHub Release assets
      val versionJsonContent = """
        {
          "version": "$ver",
          "versionCode": $code,
          "filename": "$targetFileName",
          "apkUrl": "https://github.com/ddheerajccarpenter/TDLxHT/releases/download/v$ver/$targetFileName",
          "fallbackUrl": "https://github.com/ddheerajccarpenter/TDLxHT/releases/latest/download/$genericFileName",
          "githubReleaseUrl": "https://github.com/ddheerajccarpenter/TDLxHT/releases/tag/v$ver",
          "releaseDate": "${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}"
        }
      """.trimIndent()

      File(rootDir, "version.json").writeText(versionJsonContent)
      if (webDir.exists()) {
        File(webDir, "version.json").writeText(versionJsonContent)
      }
      if (distDir.exists()) {
        File(distDir, "version.json").writeText(versionJsonContent)
      }

      println("SUCCESS: Enforced $targetFileName & $genericFileName and updated version.json pointing to GitHub Release.")
    } else {
      println("WARNING: Release APK not found at ${releaseApk.absolutePath}")
    }
  }
}

