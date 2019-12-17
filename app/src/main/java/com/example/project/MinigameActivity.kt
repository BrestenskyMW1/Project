package com.example.project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.MotionEvent.actionToString
import android.view.View
import android.widget.Toast
import androidx.core.view.MotionEventCompat
import androidx.databinding.DataBindingUtil
import com.example.project.databinding.ActivityMinigameBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.Console
import kotlin.random.Random

class MinigameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMinigameBinding
    private val actionList = mutableListOf<Int>()
    private val gestureList = mutableListOf<String>()
    private val instructionList = mutableListOf<String>()
    private var gameOn:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_minigame)
        val botNav : BottomNavigationView = findViewById(R.id.navigation)
        botNav.selectedItemId = R.id.navigation_game

        //Navigation Bar
        botNav.setOnNavigationItemSelectedListener(object : BottomNavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.getItemId()) {
                    R.id.navigation_game -> {
                    }
                    R.id.navigation_home -> {
                        val a = Intent(this@MinigameActivity, MainActivity::class.java)
                        startActivity(a)
                    }
                    R.id.navigation_map -> {
                        val b = Intent(this@MinigameActivity, MapsActivity::class.java)
                        startActivity(b)
                    }
                }
                return false
            }
        })
        botNav.selectedItemId = R.id.navigation_game

        binding.resetButton.isEnabled = false
        binding.resetButton.setOnClickListener { resetGame() }

        binding.startButton.setOnClickListener { startGame(3) }
    }

    // This example shows an Activity, but you would use the same approach if
    // you were subclassing a View.
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action: Int = MotionEventCompat.getActionMasked(event)
        val (xPos: Int, yPos: Int) = MotionEventCompat.getActionMasked(event).let { action ->
            //Log.d("The-Minigame", "The action is ${action}, ${actionToString(action)}")
            actionList.add(action)
            checkForTouchEnd()
            // Get the index of the pointer associated with the action.
            MotionEventCompat.getActionIndex(event).let { index ->
                // The coordinates of the current screen contact, relative to
                // the responding View or Activity.
                MotionEventCompat.getX(event, index).toInt() to MotionEventCompat.getY(
                    event,
                    index
                ).toInt()
            }
        }
        //Log.d("The-Minigame", "The actions are $actionList")
        /*
        if (event.pointerCount > 1) {
            Log.d("The-Minigame", "Multitouch event")

        } else {
            // Single touch event
            Log.d("The-Minigame", "Single touch event")
        }
         */
        return true
    }

    //Essentially game logic
    private fun checkForTouchEnd() {
        if (!gameOn) {
            return
        }
        //Action has ended, 1 = POINTER_UP
        if (actionList.last() == 1 && gameOn) {
            //Grab action next to last
            //Log.d("The-Minigame", "Last Action: $lastAction")

            //Add Gesture to gestureList
            when (actionList[actionList.lastIndex-1]) {
                0 -> gestureList.add("Tap")
                2 -> gestureList.add("Swipe")
                6 -> gestureList.add("MultiTouch")
                else -> gestureList.add("Other")
            }
            //Log.d("The-Minigame", "Gestures: $gestureList")

            /*Action Log Test
            when(lastAction) {
                0 -> Log.d("The-Minigame", "Tap")
                2 -> Log.d("The-Minigame", "Swipe")
                6 -> Log.d("The-Minigame", "Multi")
                else -> Log.d("The-Minigame", "ACTION ERROR")
            }
            */

            //Clear list for next gesture add
            actionList.clear()

            //Begin correctList check when lists are same size
            if (gestureList.size == instructionList.size) {
                for (g in 0 until gestureList.size) {
                    //Check for equal strings
                    if (gestureList[g] != instructionList[g]) {
                        //Log.d("The-Minigame", "Round Lost")
                        gameOver()
                        break
                    }
                }
                //Log.d("The-Minigame", "Round Won")
                gestureList.clear()
                nextRound()
            }
        }
        else {
            return
        }
    }

    private fun startGame(numActions: Int) {
        gameOn = true
        binding.resetButton.isEnabled = false
        binding.gameTitle.text = "Let the Games Begin!"
        binding.orderText.text  = "\nLook out for Toast!\nWait for it to go away.\n\nThen Gesture Here!"
        val moves = listOf("Tap", "Swipe", "MultiTouch")
        var instructions = ""
        //Create initial list
        for (a in 0 until numActions) {
            instructionList.add(moves[Random.nextInt(0,3)])
        }
        //Add actions to displayable string
        for (x in 0 until instructionList.size) {
            instructions += "${instructionList[x]}, "
        }
        //Log.d("The-Minigame", "Starting list: $instructions")
        //Set display text to instructions
        Toast.makeText(this, instructions, Toast.LENGTH_SHORT).show()
        binding.startButton.isEnabled = false
    }

    private fun nextRound() {
        if (!gameOn) {
            return
        }
        val moves = listOf("Tap", "Swipe", "MultiTouch")
        var instructions = ""
        instructionList.add(moves[Random.nextInt(0,3)])
        for (x in 0 until instructionList.size) {
            instructions += "${instructionList[x]}, "
        }
        //Log.d("The-Minigame", "Next list: $instructions")
        //Set display text to instructions
        Toast.makeText(this, instructions, Toast.LENGTH_SHORT).show()
    }

    private fun gameOver() {
        gameOn = false

        binding.gameTitle.text = "Game Over"
        binding.orderText.text = "\nThe Asked Pattern was:\n$instructionList\n\nYour Pattern was: \n$gestureList"

        instructionList.clear()
        gestureList.clear()
        actionList.clear()

        binding.resetButton.isEnabled = true
    }

    private fun resetGame() {
        binding.startButton.isEnabled = true
        binding.resetButton.isEnabled = false

    }

}
