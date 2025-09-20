import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "com.soundwave.compose.lib"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

mavenPublishing {
    coordinates("io.github.ultimatehandsomeboy666", "soundwavelib-compose", "1.0.1")

    pom {
        name.set("SoundWaveCompose")
        description.set("A Composable SoundView that dances with volume changing")
        url.set("https://github.com/ultimateHandsomeBoy666/SoundView-Compose")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("ultimateHandsomeBoy666")
                name.set("bullfrog")
                email.set("jiujiuli@qq.com")
            }
        }
        scm {
            connection.set("scm:git:github.com/ultimateHandsomeBoy666/SoundView-Compose.git")
            developerConnection.set("scm:git:ssh://github.com/ultimateHandsomeBoy666/SoundView-Compose.git")
            url.set("https://github.com/ultimateHandsomeBoy666/SoundView-Compose/tree/main")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}


dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}