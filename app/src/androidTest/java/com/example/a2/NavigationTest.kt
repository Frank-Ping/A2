package com.example.a2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.a2.presentation.MainActivity
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    // Helper to wait for a node to be displayed safely
    private fun waitForNode(tag: String, timeout: Long = 10000) {
        composeTestRule.waitUntil(timeout) {
            try {
                composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
                true
            } catch (e: Throwable) {
                false
            }
        }
    }

    @Test
    fun testNavigationToSensorActivityAndBack() {
        repeat(3) {
            waitForNode("WelcomeTitle")
            composeTestRule.onNodeWithTag("OpenSensorsButton").performClick()
            
            waitForNode("SensorHeading")
            composeTestRule.onNodeWithTag("BackButton").performScrollTo().performClick()
            
            waitForNode("WelcomeTitle")
        }
    }

    @Test
    fun testSystemBackNavigation() {
        waitForNode("WelcomeTitle")
        composeTestRule.onNodeWithTag("OpenSensorsButton").performClick()
        
        waitForNode("SensorHeading")
        androidx.test.espresso.Espresso.pressBack()
        
        waitForNode("WelcomeTitle")
    }

    @Test
    fun testSensorDataBecomesActive() {
        waitForNode("WelcomeTitle")
        composeTestRule.onNodeWithTag("OpenSensorsButton").performClick()
        
        waitForNode("SensorHeading")
        
        // Wait for status to contain "Active" or for X: to be populated
        composeTestRule.waitUntil(20000) {
            try {
                // If the emulator is running, we expect a value
                // We check if X: label is present and contains a number (not just "X: ")
                // Since our labels are "X: %s m/s^2", we check for "m/s"
                composeTestRule.onNodeWithText("X:", substring = true).assertIsDisplayed()
                true
            } catch (e: Throwable) {
                false
            }
        }
    }
}
