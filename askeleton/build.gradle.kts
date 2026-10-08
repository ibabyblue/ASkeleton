plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

group = "io.github.ibabyblue"
version = "0.2.0"

android {
    namespace = "com.ibabyblue.askeleton"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.foundation)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifactId = "askeleton"
                pom {
                    name = "ASkeleton"
                    description = "Synchronized slot-level skeleton loading states for Jetpack Compose and Android Views."
                    url = "https://github.com/ibabyblue/ASkeleton"
                    licenses {
                        license {
                            name = "MIT License"
                            url = "https://opensource.org/licenses/MIT"
                            distribution = "repo"
                        }
                    }
                    developers {
                        developer {
                            id = "ibabyblue"
                            name = "ibabyblue"
                            email = "ibabyblue_z@icloud.com"
                        }
                    }
                    scm {
                        connection = "scm:git:git://github.com/ibabyblue/ASkeleton.git"
                        developerConnection = "scm:git:ssh://github.com/ibabyblue/ASkeleton.git"
                        url = "https://github.com/ibabyblue/ASkeleton"
                    }
                }
            }
        }
    }
}
