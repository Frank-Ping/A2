package com.example.a2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.a2.presentation.MainActivity
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testNavigationToSensorActivityAndBack() {
        repeat(3) {
            // 1. Verify MainActivity is displayed
            composeTestRule.onNodeWithTag("WelcomeTitle").assertIsDisplayed()

            // 2. Click OpenSensorsButton
            composeTestRule.onNodeWithTag("OpenSensorsButton").performClick()
            composeTestRule.waitForIdle()

            // 3. Verify SensorActivity is displayed
            composeTestRule.onNodeWithTag("SensorHeading").assertIsDisplayed()

            // 4. Scroll to and click BackButton
            composeTestRule.onNodeWithTag("BackButton").performScrollTo().performClick()
            composeTestRule.waitForIdle()

            // 5. Verify MainActivity is displayed again
            composeTestRule.onNodeWithTag("WelcomeTitle").assertIsDisplayed()
        }
    }

    @Test
    fun testSystemBackNavigation() {
        // 1. Open SensorActivity
        composeTestRule.onNodeWithTag("OpenSensorsButton").performClick()
        composeTestRule.onNodeWithTag("SensorHeading").assertIsDisplayed()

        // 2. Trigger System Back
        androidx.test.espresso.Espresso.pressBack()
        composeTestRule.waitForIdle()

        // 3. Verify return to MainActivity
        composeTestRule.onNodeWithTag("WelcomeTitle").assertIsDisplayed()
    }
}
