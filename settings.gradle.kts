rootProject.name = "community_management"

plugins {
	id("com.gradle.develocity") version "4.3.2"
}

develocity {
	server.set("https://scans.gradle.com")
	buildScan {
		publishing.onlyIf { !System.getenv("CI").isNullOrBlank() }
		termsOfUseAgree.set("yes")
		termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
	}
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
	}
}

include("backend")