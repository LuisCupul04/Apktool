import java.io.ByteArrayOutputStream

val version = "3.0.1"
val suffix = ".RE"

// Strings embedded into the build.
var gitRevision by extra("")
var apktoolVersion by extra("")

defaultTasks("build", "shadowJar", "proguard")

// Obtener descripción de Git usando providers.exec (Gradle 8+)
val gitDescribe: String? by lazy {
    try {
        val output = ByteArrayOutputStream()
        project.providers.exec {
            commandLine("git", "describe", "--tags")
            standardOutput = output
        }.result.get()
        output.toString().trim().replace("-g", "-")
    } catch (e: Exception) {
        null
    }
}

// Obtener rama de Git
val gitBranch: String? by lazy {
    try {
        val output = ByteArrayOutputStream()
        project.providers.exec {
            commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
            standardOutput = output
        }.result.get()
        output.toString().trim()
    } catch (e: Exception) {
        null
    }
}

if ("release" !in gradle.startParameter.taskNames) {
    gitRevision = ""
    apktoolVersion = if (suffix.isNotEmpty()) "${version}${suffix}" else version
    project.logger.lifecycle("Building RELEASE ($gitBranch): $apktoolVersion")
}

plugins {
    `java-library`
    `maven-publish`
    signing
}

allprojects {
    repositories {
        mavenCentral()
        google()

        // TU REPOSITORIO SMALI-RE
        maven {
            url = uri("https://maven.pkg.github.com/LuisCupul04/smali-RE")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user").toString()
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key").toString()
            }
        }

        // JitPack (solo para iBotPeaches, NO para com.extenre)
        maven {
            url = uri("https://jitpack.io")
            content {
                includeGroup("com.github.iBotPeaches.smali")
                // ¡COMENTA O ELIMINA: includeGroup("com.extenre.smali")!
            }
        }
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

        java {
            withJavadocJar()
            withSourcesJar()
        }

        publishing {
            repositories {
                maven {
                    url = uri("https://maven.pkg.github.com/LuisCupul04/Apktool")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user").toString()
                        password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key").toString()
                    }
                }
            }
            publications {
                register("mavenJava", MavenPublication::class) {
                    from(components["java"])
                    groupId = "com.extenre"
                    artifactId = project.name
                    version = apktoolVersion

                    pom {
                        name = "Apktool"
                        description = "A tool for reverse engineering Android apk files."
                        url = "https://apktool.org"

                        licenses {
                            license {
                                name = "The Apache License 2.0"
                                url = "https://opensource.org/licenses/Apache-2.0"
                            }
                        }
                        developers {
                            developer {
                                id = "LuisCupul04"
                                name = "LuisCupul04"
                                email = "LuisCupul04@outlook.com"
                            }
                            developer {
                                id = "brutall"
                                name = "Ryszard Wiśniewski"
                                email = "brut.alll@gmail.com"
                            }
                        }
                        scm {
                            connection = "scm:git:git://github.com/LuisCupul04/Apktool.git"
                            developerConnection = "scm:git:git@github.com:LuisCupul04/Apktool.git"
                            url = "https://github.com/LuisCupul04/Apktool"
                        }
                    }
                }
            }
        }

        tasks.withType<Javadoc>() {
            (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
        }

        signing {
            sign(publishing.publications["mavenJava"])
        }
    }
}

tasks.register("release") {
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

// Opcional: deshabilitar metadatos de módulo de Gradle si no se desean
// tasks.withType<GenerateModuleMetadata> {
//     enabled = false
// }