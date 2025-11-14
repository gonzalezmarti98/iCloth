package com.martigonzalez.project_icloth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.martigonzalez.project_icloth.databinding.ActivityClosetBinding

class ClosetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClosetBinding

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        binding = ActivityClosetBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}