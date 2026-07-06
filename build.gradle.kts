plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.2.21" apply false
    id("com.google.devtools.ksp") version "2.2.21-2.0.5" apply false
    // Pinned em 2.58 (não 2.59+): a partir do Dagger 2.59 o Hilt Gradle Plugin passou a
    // exigir AGP 9 (e Gradle 9.1+), uma migração maior fora do escopo desta atualização.
    id("com.google.dagger.hilt.android") version "2.58" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21" apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
    id("com.google.firebase.crashlytics") version "3.0.7" apply false
}
