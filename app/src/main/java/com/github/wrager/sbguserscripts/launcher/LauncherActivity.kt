package com.github.wrager.sbguserscripts.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.wrager.sbguserscripts.GameActivity
import com.github.wrager.sbguserscripts.R
import com.github.wrager.sbguserscripts.script.model.ScriptIdentifier
import com.github.wrager.sbguserscripts.script.preset.ConflictDetector
import com.github.wrager.sbguserscripts.script.preset.StaticConflictRules
import com.github.wrager.sbguserscripts.script.storage.ScriptFileStorageImpl
import com.github.wrager.sbguserscripts.script.storage.ScriptStorageImpl
import com.github.wrager.sbguserscripts.script.updater.DefaultHttpFetcher
import com.github.wrager.sbguserscripts.script.updater.ScriptDownloader
import com.github.wrager.sbguserscripts.script.updater.ScriptUpdateChecker
import com.github.wrager.sbguserscripts.settings.SettingsActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.File

class LauncherActivity : AppCompatActivity() {

    private val viewModel: LauncherViewModel by viewModels {
        val preferences = getSharedPreferences("scripts", MODE_PRIVATE)
        val fileStorage = ScriptFileStorageImpl(File(filesDir, "scripts"))
        val scriptStorage = ScriptStorageImpl(preferences, fileStorage)
        val conflictDetector = ConflictDetector(StaticConflictRules())
        val httpFetcher = DefaultHttpFetcher()
        val downloader = ScriptDownloader(httpFetcher, scriptStorage)
        val updateChecker = ScriptUpdateChecker(httpFetcher, scriptStorage)
        val appPreferences = getSharedPreferences("app", MODE_PRIVATE)
        LauncherViewModel.Factory(
            scriptStorage,
            conflictDetector,
            downloader,
            updateChecker,
            appPreferences,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        setupToolbar()
        setupScriptList()
        setupButtons()
        observeViewModel()
    }

    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.toolbar).setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_update_all -> {
                    viewModel.updateAllScripts()
                    Toast.makeText(this, R.string.checking_updates, Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupScriptList() {
        val scriptList = findViewById<RecyclerView>(R.id.scriptList)
        scriptList.layoutManager = LinearLayoutManager(this)
        scriptList.adapter = ScriptListAdapter(
            onToggleChanged = { identifier, enabled ->
                viewModel.toggleScript(identifier, enabled)
            },
            onDeleteClick = { identifier ->
                showDeleteConfirmation(identifier)
            },
        )
    }

    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.launchButton).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
        findViewById<FloatingActionButton>(R.id.addScriptButton).setOnClickListener {
            showAddScriptDialog()
        }
    }

    private fun observeViewModel() {
        val scriptList = findViewById<RecyclerView>(R.id.scriptList)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val emptyText = findViewById<TextView>(R.id.emptyText)
        val launchButton = findViewById<MaterialButton>(R.id.launchButton)
        val adapter = scriptList.adapter as ScriptListAdapter

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event -> handleEvent(event) }
            }
        }
    }

    private fun handleEvent(event: LauncherEvent) {
        val message = when (event) {
            is LauncherEvent.ScriptAdded ->
                getString(R.string.script_added, event.scriptName)
            is LauncherEvent.ScriptAddFailed ->
                getString(R.string.script_add_failed, event.errorMessage)
            is LauncherEvent.ScriptDeleted ->
                getString(R.string.script_deleted, event.scriptName)
            is LauncherEvent.UpdatesCompleted ->
                if (event.updatedCount > 0) {
                    getString(R.string.updates_applied, event.updatedCount)
                } else {
                    getString(R.string.no_updates)
                }
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAddScriptDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_script, null)
        val urlInput = dialogView.findViewById<TextInputEditText>(R.id.scriptUrlInput)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_script)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _, _ ->
                val url = urlInput.text?.toString()?.trim()
                if (!url.isNullOrEmpty()) {
                    viewModel.addScript(url)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showDeleteConfirmation(identifier: ScriptIdentifier) {
        val scriptName = viewModel.uiState.value.scripts
            .find { it.identifier == identifier }?.name ?: return

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_script)
            .setMessage(getString(R.string.delete_script_confirmation, scriptName))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteScript(identifier)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
