plugins {
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
repositories { mavenCentral() }
java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }
application { mainClass.set("aso.nfsapp.app.Main") }
tasks.withType<JavaCompile> { options.release.set(17) }
dependencies { testImplementation("org.junit.jupiter:junit-jupiter:5.10.3") }
tasks.test { useJUnitPlatform() }
tasks.shadowJar {
    archiveBaseName.set("nfs-app")
    archiveVersion.set("1.0.0")
    archiveClassifier.set("")
    manifest {
        attributes(mapOf("Main-Class" to "aso.nfsapp.app.Main"))
    }
}
