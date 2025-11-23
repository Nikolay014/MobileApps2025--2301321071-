package com.example.fittrack

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainFlowTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun addRunWorkout_showsInDashboardList() {
        // Натискаме бутона "Добави бягане"
        onView(withId(R.id.btnAddRun))
            .perform(click())

        // Попълваме заглавие
        onView(withId(R.id.etTitle))
            .perform(typeText("Test"), closeSoftKeyboard())

        // Продължителност
        onView(withId(R.id.etDuration))
            .perform(typeText("30"), closeSoftKeyboard())

        // Дистанция
        onView(withId(R.id.etDistance))
            .perform(typeText("5.0"), closeSoftKeyboard())

        // Бележки (по желание)
        onView(withId(R.id.etNotes))
            .perform(typeText("UI test"), closeSoftKeyboard())

        // Запазваме
        onView(withId(R.id.btnSaveWorkout))
            .perform(click())

        // Връщаме се на Dashboard и проверяваме дали в списъка пише "Тестово бягане"
        onView(withText(containsString("Test")))
            .check(matches(isDisplayed()))
    }
}
