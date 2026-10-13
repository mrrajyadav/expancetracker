package com.baburamtechnologies.expensetracker.core.auth

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentActivityProvider @Inject constructor() : Application.ActivityLifecycleCallbacks {

    private var currentActivity: WeakReference<Activity>? = null

    fun get(): Activity? = currentActivity?.get()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = WeakReference(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity?.get() == activity) {
            currentActivity = null
        }
    }
}
