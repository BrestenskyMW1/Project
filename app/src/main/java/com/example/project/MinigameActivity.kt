package com.example.project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.actionToString
import androidx.core.view.MotionEventCompat
import androidx.databinding.DataBindingUtil
import com.example.project.databinding.ActivityMinigameBinding

class MinigameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMinigameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_minigame)


    }

    // This example shows an Activity, but you would use the same approach if
    // you were subclassing a View.
    override fun onTouchEvent(event: MotionEvent): Boolean {

        val action: Int = MotionEventCompat.getActionMasked(event)

        val (xPos: Int, yPos: Int) = MotionEventCompat.getActionMasked(event).let { action ->
            Log.d("The-Minigame", "The action is ${actionToString(action)}")
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

        if (event.pointerCount > 1) {
            Log.d("The-Minigame", "Multitouch event")

        } else {
            // Single touch event
            Log.d("The-Minigame", "Single touch event")
        }

        // Given an action int, returns a string description
        fun actionToString(action: Int): String {
            return when (action) {
                MotionEvent.ACTION_DOWN -> "Down"
                MotionEvent.ACTION_MOVE -> "Move"
                MotionEvent.ACTION_POINTER_DOWN -> "Pointer Down"
                MotionEvent.ACTION_UP -> "Up"
                MotionEvent.ACTION_POINTER_UP -> "Pointer Up"
                MotionEvent.ACTION_OUTSIDE -> "Outside"
                MotionEvent.ACTION_CANCEL -> "Cancel"
                else -> ""
            }
        }
        return true
    }

}
