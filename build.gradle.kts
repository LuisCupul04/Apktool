import java.io.ByteArrayOutputStream

// TU VERSIÓN PERSONALIZADA - CAMBIA ESTO A LO QUE NECESITES
val customVersion = "42fccafcaa"
val baseVersion = "2.5.3"

// Strings embedded into the build.
var gitRevision by extra("")
var apktoolVersion by extra("")

defaultTasks("build", "shadowJar", "proguard")

// Functions (las mantenemos por si las necesitas después)
val gitDescribe: String? by lazy {
    val stdout = ByteArrayOutputStream()
    try {
        rootProject.exec {
            commandLine("git", "describe", "--tags")
            standardOutput = stdout
        }
        stdout.toString().trim().replace("-g", "-")
    } catch (e: Exception) {
        null
    }
}

val gitBranch: String? by lazy {
    val stdout = ByteArrayOutputStream()
    try {
        rootProject.exec {
            commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim()
    } catch (e: Exception) {
        null
    }
}

// LÓGICA SIMPLIFICADA DE VERSIONES - USA TU VERSIÓN PERSONALIZADA
if ("release" !in gradle.startParameter.taskNames) {
    gitRevision = "42fccafc"  // El hash de tu commit
    apktoolVersion = customVersion  // "2.5.3-42fccafc-dirty"
    project.logger.lifecycle("Building LuisCupul04 fork: $apktoolVersion")
} else {
    gitRevision = ""
    apktoolVersion = baseVersion  // "2.5.3" para releases
    project.logger.lifecycle("Building RELEASE: $apktoolVersion")
}

plugins {
    `java-library`
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
            content {
                includeGroup("com.github.iBotPeaches.smali")  // Original
                // CORREGIDO: Quité el punto extra
                includeGroup("com.github.luiscupul04.smali")  // Tu fork
            }
        }
        google()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    val mavenProjects = arrayOf(
        "brut.j.common", "brut.j.util", "brut.j.dir", "brut.j.xml", "brut.j.yaml",
        "apktool-lib", "apktool-cli"
    )

    if (project.name in mavenProjects) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
        
        project.extensions.configure<org.gradle.api.publish.PublishingExtension> {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/LuisCupul04/Apktool")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user")?.toString() ?: ""
                        password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key")?.toString() ?: ""
                    }
                }
            }
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                    // IMPORTANTE: Cambié groupId de "com.extenre" a algo más personal
                    groupId = "com.github.luiscupul04"
                    artifactId = project.name
                    version = apktoolVersion

                    pom {
                        name.set("Apktool")
                        description.set("A tool for reverse engineering Android apk files.")
                        url.set("https://github.com/LuisCupul04/Apktool")

                        licenses {
                            license {
                                name.set("The Apache License 2.0")
                                url.set("https://opensource.org/licenses/Apache-2.0")
                            }
                        }
                        developers {
                            developer {
                                id.set("LuisCupul04")
                                name.set("LuisCupul04")
                            }
                            developer {
                                id.set("iBotPeaches")
                                name.set("Connor Tumbleson")
                                email.set("connor.tumbleson@gmail.com")
                            }
                            developer {
                                id.set("brutall")
                                name.set("Ryszard Wiśniewski")
                                email.set("brut.alll@gmail.com")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/LuisCupul04/Apktool.git")
                            developerConnection.set("scm:git:ssh://github.com/LuisCupul04/Apktool.git")
                            url.set("https://github.com/LuisCupul04/Apktool")
                        }
                    }
                }
            }
        }

        project.extensions.configure<org.gradle.plugins.signing.SigningExtension> {
            useGpgCmd()
            sign(project.extensions.getByType<org.gradle.api.publish.PublishingExtension>().publications["mavenJava"])
        }
    }
}

task("release") {
    // Used for official releases.
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:-options")
    options.compilerArgs.add("--release 8")

    options.encoding = "UTF-8"
}