// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ktandroid) apply false
    alias(libs.plugins.ktcompose) apply false
    alias(libs.plugins.ktserialization) apply false
    alias(libs.plugins.hiltgradle) apply false
    alias(libs.plugins.kspgradle) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
}
