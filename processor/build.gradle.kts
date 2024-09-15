val kspVersion: String by project

plugins {
    kotlin("jvm")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}

//tasks.withType<KotlinCompile>().all {
//    compilerOptions.jvmTarget.set(JvmTarget.JVM_11) //= JavaVersion.VERSION_11.toString()
//
//}