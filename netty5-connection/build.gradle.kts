plugins {
    id("java")
}

dependencies {
    compileOnly(libs.utility.lombok)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.gson)
    implementation(libs.netty5.all)
    annotationProcessor(libs.utility.lombok)
}