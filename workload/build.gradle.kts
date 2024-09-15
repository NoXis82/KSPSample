plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":processor"))
    ksp(project(":processor"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
//чтобы IDE знала о сгенерированных файлах
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}