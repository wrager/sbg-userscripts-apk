package com.github.wrager.sbguserscripts.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.wrager.sbguserscripts.GameActivity
import com.github.wrager.sbguserscripts.R
import com.github.wrager.sbguserscripts.script.preset.ConflictDetector
import com.github.wrager.sbguserscripts.script.preset.StaticConflictRules
import com.github.wrager.sbguserscripts.script.storage.ScriptFileStorageImpl
import com.github.wrager.sbguserscripts.script.storage.ScriptStorageImpl
import com.github.wrager.sbguserscripts.script.updater.DefaultHttpFetcher
import com.github.wrager.sbguserscripts.script.updater.ScriptDownloader
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.io.File

class LauncherActivity : AppCompatActivity() {

    private val viewModel: LauncherViewModel by viewModels {
        val preferences = getSharedPreferences("scripts", MODE_PRIVATE)
        val fileStorage = ScriptFileStorageImpl(File(filesDir, "scripts"))
        val scriptStorage = ScriptStorageImpl(preferences, fileStorage)
        val conflictDetector = ConflictDetector(StaticConflictRules())
        val downloader = ScriptDownloader(DefaultHttpFetcher(), scriptStorage)
        val appPreferences = getSharedPreferences("app", MODE_PRIVATE)
        LauncherViewModel.Factory(scriptStorage, conflictDetector, downloader, appPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        val scriptList = findViewById<RecyclerView>(R.id.scriptList)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val emptyText = findViewById<TextView>(R.id.emptyText)
        val launchButton = findViewById<MaterialButton>(R.id.launchButton)

        val adapter = ScriptListAdapter { identifier, enabled ->
            viewModel.toggleScript(identifier, enabled)
        }

        scriptList.layoutManager = LinearLayoutManager(this)
        scriptList.adapter = adapter

        launchButton.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    scriptList.visibility = if (state.isLoading) View.GONE else View.VISIBLE
                    launchButton.isEnabled = !state.isLoading

                    if (!state.isLoading) {
                        adapter.submitList(state.scripts)
                        emptyText.visibility =
                            if (state.scripts.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }
}
