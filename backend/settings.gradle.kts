rootProject.name = "backend"

plugins {
    id("com.gradle.develocity") version "4.3.2"
}

develocity {
    server.set("https://scans.gradle.com")
    buildScan {
        publishing.onlyIf { true }
        termsOfUseAgree.set("yes")
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
    }
}
