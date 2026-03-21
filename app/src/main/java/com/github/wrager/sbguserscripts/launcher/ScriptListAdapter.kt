package com.github.wrager.sbguserscripts.launcher

import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.wrager.sbguserscripts.R
import com.github.wrager.sbguserscripts.script.model.ScriptIdentifier
import com.google.android.material.color.MaterialColors

class ScriptListAdapter(
    private val onToggleChanged: (ScriptIdentifier, Boolean) -> Unit,
    private val onDownloadRequested: (ScriptIdentifier) -> Unit,
    private val onUpdateRequested: (ScriptIdentifier) -> Unit,
    private val onOverflowClick: (View, ScriptUiItem) -> Unit,
) : ListAdapter<ScriptUiItem, ScriptListAdapter.ScriptViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScriptViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_script, parent, false)
        return ScriptViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScriptViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScriptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.scriptName)
        private val detailsText: TextView = itemView.findViewById(R.id.scriptDetails)
        private val downloadStatusText: TextView = itemView.findViewById(R.id.downloadStatusText)
        private val toggle: SwitchCompat = itemView.findViewById(R.id.scriptToggle)
        private val actionButton: ImageButton = itemView.findViewById(R.id.actionButton)
        private val loadingProgress: ProgressBar = itemView.findViewById(R.id.loadingProgress)
        private val defaultStatusTextColor = downloadStatusText.currentTextColor

        fun bind(item: ScriptUiItem) {
            nameText.text = item.name

            bindDetails(item)
            bindDownloadStatus(item)
            bindLoadingProgress(item)
            bindControls(item)
        }

        /**
         * Собирает строку деталей из версии, статуса актуальности и предупреждения о
         * несовместимости. Всё отображается в одном TextView, чтобы не менять высоту карточки.
         */
        private fun bindDetails(item: ScriptUiItem) {
            val versionText = formatVersion(item.version, item.releaseTag)
            val conflictText = if (item.conflictNames.isNotEmpty()) {
                itemView.context.getString(
                    R.string.conflict_warning,
                    item.conflictNames.joinToString(", "),
                )
            } else {
                null
            }
            val upToDateText = if (item.isUpToDate) {
                itemView.context.getString(R.string.status_up_to_date)
            } else {
                null
            }

            val hasContent = versionText.isNotEmpty() || conflictText != null || upToDateText != null
            if (!hasContent) {
                detailsText.visibility = View.GONE
                return
            }

            val builder = SpannableStringBuilder(versionText)
            if (upToDateText != null) {
                if (builder.isNotEmpty()) builder.append(DETAILS_SEPARATOR)
                builder.append(upToDateText)
            }
            if (conflictText != null) {
                if (builder.isNotEmpty()) builder.append(DETAILS_SEPARATOR)
                val conflictStart = builder.length
                builder.append(conflictText)
                builder.setSpan(
                    ForegroundColorSpan(CONFLICT_WARNING_COLOR),
                    conflictStart,
                    builder.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
            detailsText.text = builder
            detailsText.visibility = View.VISIBLE
        }

        /**
         * Формирует строку версии для карточки скрипта.
         * Если releaseTag задан и отличается от @version (например, CUI в репо EUI),
         * показывает оба: "v26.1.7 (v6.14.0)".
         */
        private fun formatVersion(version: String?, releaseTag: String?): String {
            if (version == null) return ""
            val versionText = "v$version"
            if (releaseTag == null) return versionText
            val tagVersion = releaseTag.removePrefix("v")
            if (tagVersion == version) return versionText
            return "$versionText ($releaseTag)"
        }

        private fun bindDownloadStatus(item: ScriptUiItem) {
            downloadStatusText.paintFlags = downloadStatusText.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            downloadStatusText.setTextColor(defaultStatusTextColor)
            downloadStatusText.setOnClickListener(null)
            downloadStatusText.isClickable = false
            when {
                item.downloadProgress != null -> {
                    downloadStatusText.text = itemView.context.getString(
                        R.string.downloading_progress,
                        item.downloadProgress,
                    )
                    downloadStatusText.visibility = View.VISIBLE
                }
                item.isCheckingUpdate -> {
                    downloadStatusText.text =
                        itemView.context.getString(R.string.checking_updates)
                    downloadStatusText.visibility = View.VISIBLE
                }
                item.hasUpdateAvailable -> {
                    val primaryColor = MaterialColors.getColor(
                        itemView.context, com.google.android.material.R.attr.colorPrimary, 0,
                    )
                    downloadStatusText.text = itemView.context.getString(R.string.update)
                    downloadStatusText.setTextColor(primaryColor)
                    downloadStatusText.paintFlags =
                        downloadStatusText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    downloadStatusText.setOnClickListener { onUpdateRequested(item.identifier) }
                    downloadStatusText.isClickable = true
                    downloadStatusText.visibility = View.VISIBLE
                }
                else -> {
                    downloadStatusText.visibility = View.GONE
                }
            }
        }

        private fun bindLoadingProgress(item: ScriptUiItem) {
            when {
                item.downloadProgress != null -> {
                    loadingProgress.isIndeterminate = false
                    loadingProgress.progress = item.downloadProgress
                    loadingProgress.visibility = View.VISIBLE
                }
                item.isCheckingUpdate -> {
                    loadingProgress.isIndeterminate = true
                    loadingProgress.visibility = View.VISIBLE
                }
                else -> {
                    loadingProgress.visibility = View.INVISIBLE
                }
            }
        }

        private fun bindControls(item: ScriptUiItem) {
            val isDownloading = item.downloadProgress != null
            val isInteractive = item.isDownloaded && !isDownloading

            toggle.visibility = View.VISIBLE
            toggle.setOnCheckedChangeListener(null)
            toggle.isChecked = item.enabled
            if (isInteractive) {
                toggle.alpha = 1.0f
                toggle.setOnTouchListener(null)
                toggle.setOnCheckedChangeListener { _, isChecked ->
                    onToggleChanged(item.identifier, isChecked)
                }
            } else {
                toggle.alpha = 0.4f
                toggle.setOnTouchListener { _, _ -> true }
            }

            when {
                isDownloading -> {
                    actionButton.visibility = View.INVISIBLE
                    actionButton.isClickable = false
                }
                item.isDownloaded -> {
                    actionButton.setImageResource(R.drawable.ic_more_vert)
                    actionButton.contentDescription = itemView.context.getString(R.string.script_menu)
                    actionButton.isClickable = true
                    actionButton.setOnClickListener { view -> onOverflowClick(view, item) }
                    actionButton.visibility = View.VISIBLE
                }
                else -> {
                    actionButton.setImageResource(R.drawable.ic_download)
                    actionButton.contentDescription = itemView.context.getString(R.string.download_script)
                    actionButton.isClickable = true
                    actionButton.setOnClickListener { onDownloadRequested(item.identifier) }
                    actionButton.visibility = View.VISIBLE
                }
            }
        }

    }

    companion object {
        private const val CONFLICT_WARNING_COLOR = 0xFFFFA000.toInt()
        private const val DETAILS_SEPARATOR = " · "

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ScriptUiItem>() {
            override fun areItemsTheSame(oldItem: ScriptUiItem, newItem: ScriptUiItem): Boolean =
                oldItem.identifier == newItem.identifier

            override fun areContentsTheSame(oldItem: ScriptUiItem, newItem: ScriptUiItem): Boolean =
                oldItem == newItem
        }
    }
}
