package com.example.project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.project.databinding.ActivityMinigameBinding

class MinigameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMinigameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_minigame)

    }
}
