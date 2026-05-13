rootProject.name = "KMPDarcyDemo"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenLocal()  // 添加这行以启用本地 .m2 仓库
//        maven("https://maven.aliyun.com/repository/public")
//        maven("https://maven.aliyun.com/repository/jcenter")
//        maven("https://maven.aliyun.com/repository/google")
//        maven("https://maven.aliyun.com/repository/gradle-plugin")
        google()
//        google {
//            mavenContent {
//                includeGroupAndSubgroups("androidx")
//                includeGroupAndSubgroups("com.android")
//                includeGroupAndSubgroups("com.google")
//            }
//        }
        mavenCentral()
        gradlePluginPortal()
        // 添加本地 Maven 仓库
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()  // 添加这行以启用本地 .m2 仓库
//        maven("https://maven.aliyun.com/repository/public")
//        maven("https://maven.aliyun.com/repository/jcenter")
//        maven("https://maven.aliyun.com/repository/google")
//        maven("https://maven.aliyun.com/repository/gradle-plugin")
        google()
//        google {
//            mavenContent {
//                includeGroupAndSubgroups("androidx")
//                includeGroupAndSubgroups("com.android")
//                includeGroupAndSubgroups("com.google")
//            }
//        }
        mavenCentral()

        // 添加本地 Maven 仓库
        mavenLocal()
    }
}

include(":composeApp")