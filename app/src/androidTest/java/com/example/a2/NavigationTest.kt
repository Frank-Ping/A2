package com.example.a2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
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
            
            // For TransformingLazyColumn, we might need to swipe up manually if performScrollTo fails
            // or use a more persistent approach.
            var found = false
            var attempts = 0
            while (!found && attempts < 5) {
                try {
                    composeTestRule.onNodeWithTag("BackButton").assertIsDisplayed()
                    found = true
                } catch (e: Throwable) {
                    composeTestRule.onNodeWithTag("SensorList").performTouchInput {
                        swipeUp(durationMillis = 500)
                    }
                    attempts++
                }
            }

            composeTestRule.onNodeWithTag("BackButton").performClick()
            
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
    fun testAllSensorsAreVisibleByScrolling() {
        waitForNode("WelcomeTitle")
        composeTestRule.onNodeWithTag("OpenSensorsButton").performClick()
        
        waitForNode("SensorHeading")
        
        // Verify Accelerometer
        composeTestRule.onNodeWithText("Accelerometer", substring = true).assertIsDisplayed()
        
        // Scroll and verify Gyroscope
        swipeToNode("Gyroscope")
        composeTestRule.onNodeWithText("Gyroscope", substring = true).assertIsDisplayed()
        
        // Scroll and verify Magnetic Field
        swipeToNode("Magnetic Field")
        composeTestRule.onNodeWithText("Magnetic Field", substring = true).assertIsDisplayed()
        
        // Scroll and verify Back button
        swipeToNode("Back")
        composeTestRule.onNodeWithTag("BackButton").assertIsDisplayed()
        
        // Return to MainActivity
        composeTestRule.onNodeWithTag("BackButton").performClick()
        waitForNode("WelcomeTitle")
    }

    // Helper to swipe up until a node with text is displayed
    private fun swipeToNode(text: String, attempts: Int = 10) {
        var count = 0
        var found = false
        while (!found && count < attempts) {
            try {
                composeTestRule.onNodeWithText(text, substring = true).assertIsDisplayed()
                found = true
            } catch (e: Throwable) {
                composeTestRule.onNodeWithTag("SensorList").performTouchInput {
                    swipeUp(durationMillis = 500)
                }
                count++
            }
        }
    }
}
