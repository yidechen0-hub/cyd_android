plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    id("com.google.protobuf")
//    alias(libs.plugins.protobuf)
    id("com.google.protobuf") version "0.9.4"
}

android {
    namespace = "com.cyd.cyd_android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cyd.cyd_android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    sourceSets {
        getByName("main") {
            java {
                srcDirs("src/main/java/com/cyd/cyd_android/serialization")
            }

        }
    }


}




// 必须配置 protobuf 块（插件核心配置）
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.4"
    }
    generateProtoTasks {
        // 0.9.x 版本插件使用 all() 方法
        all().forEach { task ->
            task.plugins {
                create("java") {
                    outputSubDir = "java"
                }
            }
        }
    }
//    generatedFilesBaseDir = "$project.buildDir/generated/source/proto"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // 使用与protoc匹配的版本
    implementation("com.google.protobuf:protobuf-java:3.24.4")
    implementation("com.google.code.gson:gson:2.10.1")

}
