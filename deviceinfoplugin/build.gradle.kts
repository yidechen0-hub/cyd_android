plugins {
    id("com.android.application")
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.cyd.deviceinfoplugin"
    compileSdk = 36

    defaultConfig {
        // 1. 移除 applicationId（仅主应用需要）
        // 删掉：applicationId = "com.cyd.deviceinfoplugin"

        minSdk = 24
        targetSdk = 36



        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // 若需要被其他模块引用但不强制依赖，可添加此配置（可选）
//        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation(project(":pluginframework")) // 仅依赖插件框架
    // 其他基础依赖保留，但不添加对主应用的依赖
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}