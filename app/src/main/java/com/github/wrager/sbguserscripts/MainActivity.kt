package com.github.wrager.sbguserscripts

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// Совместимость при обновлении с v0.1: Android может попытаться восстановить
// сохранённый task backstack с этой активити. Перенаправляем на GameActivity.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, GameActivity::class.java))
        finish()
    }
}
