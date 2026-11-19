// Repository resolution has two modes (same convention as Scriptlet / MochiMori):
// - Normal machines: google() / mavenCentral() / gradlePluginPortal().
// - The author's build VM: dependencies go through the local curl-backed
//   mirror when OTONA_LOCAL_MIRROR=1 (see build.sh).
pluginManagement {
    repositories {
        if (System.getenv("OTONA_LOCAL_MIRROR") == "1") {
            maven { url = uri("http://127.0.0.1:18080/google"); isAllowInsecureProtocol = true }
            maven { url = uri("http://127.0.0.1:18080/central"); isAllowInsecureProtocol = true }
            maven { url = uri("http://127.0.0.1:18080/plugins"); isAllowInsecureProtocol = true }
        } else {
            google()
            mavenCentral()
            gradlePluginPortal()
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (System.getenv("OTONA_LOCAL_MIRROR") == "1") {
            maven { url = uri("http://127.0.0.1:18080/google"); isAllowInsecureProtocol = true }
            maven { url = uri("http://127.0.0.1:18080/central"); isAllowInsecureProtocol = true }
        } else {
            google()
            mavenCentral()
        }
    }
}
rootProject.name = "OtonaLevel100"
include(":app")
