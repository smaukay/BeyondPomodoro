package com.smaukay.beyondpomodoro

import android.support.test.uiautomator.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers

fun clickDisplayedNotification(title: String) {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.openNotification()
    device.wait(Until.hasObject(By.text(title)), 1000)
    val notificationTitle: UiObject2 = device.findObject(By.text(title))
    ViewMatchers.assertThat(title, Matchers.equalTo(notificationTitle.text))
    notificationTitle.click()
}

fun focusOnApp() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.pressRecentApps()
    val myApp: UiObject = device.findObject(UiSelector().description("BeyondPomodoro"))
    myApp.clickAndWaitForNewWindow()
}