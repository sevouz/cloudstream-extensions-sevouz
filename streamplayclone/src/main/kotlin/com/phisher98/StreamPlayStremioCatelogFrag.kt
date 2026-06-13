package com.phisher98

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.DialogFragment
import com.lagradost.api.Log
import com.lagradost.cloudstream3.CommonActivity.showToast

class StreamPlayStremioCatelogFrag(
    plugin: StreamPlayPlugin,
    private val sharedPref: SharedPreferences,
    private val onDismissCallback: (() -> Unit)? = null
) : DialogFragment() {

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val displayMetrics = resources.displayMetrics
            val maxDialogWidth = (500 * displayMetrics.density).toInt()
            val width = if (displayMetrics.widthPixels > 0 && displayMetrics.widthPixels > maxDialogWidth) maxDialogWidth
            else (displayMetrics.widthPixels * 0.9f).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
    }

    private val PREF_KEY_LINKS = "streamplay_stremio_saved_links"
    private val res = plugin.resources ?: throw Exception("Unable to access plugin resources")

    private fun getDrawable(name: String): Drawable {
        val id = res.getIdentifier(name, "drawable", BuildConfig.LIBRARY_PACKAGE_NAME)
        return res.getDrawable(id, null) ?: throw Exception("Drawable $name not found")
    }

    private fun <T : View> View.findView(name: String): T {
        val id = res.getIdentifier(name, "id", BuildConfig.LIBRARY_PACKAGE_NAME)
        if (id == 0) throw Exception("View ID $name not found.")
        return this.findViewById(id)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun View.makeTvCompatible() {
        val outlineId = res.getIdentifier("outline", "drawable", BuildConfig.LIBRARY_PACKAGE_NAME)
        this.background = res.getDrawable(outlineId, null)
    }

    private fun getLayout(name: String, inflater: LayoutInflater, container: ViewGroup?): View {
        val id = res.getIdentifier(name, "layout", BuildConfig.LIBRARY_PACKAGE_NAME)
        val layout = res.getLayout(id)
        return inflater.inflate(layout, container, false)
    }

    @SuppressLint("DiscouragedApi")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = getLayout("stremio_bottom_sheet_layout", inflater, container)
        val drawableId = res.getIdentifier("dialog_background", "drawable", BuildConfig.LIBRARY_PACKAGE_NAME)
        if (drawableId != 0) view.background = res.getDrawable(drawableId, null)

        val addlinks: ImageView = view.findView("addlinks")
        val showlinks: ImageView = view.findView("showlinks")
        val saveIcon: ImageView = view.findView("saveIcon")

        addlinks.setImageDrawable(getDrawable("settings_icon"))
        showlinks.setImageDrawable(getDrawable("settings_icon"))
        saveIcon.setImageDrawable(getDrawable("save_icon"))
        addlinks.makeTvCompatible()
        showlinks.makeTvCompatible()
        saveIcon.makeTvCompatible()

        addlinks.setOnClickListener {
            val dialogView = getLayout("streamio_addon_addlinks", inflater, container)
            val etName: EditText
            val etLink: EditText
            try {
                etName = dialogView.findView("etName")
                etLink = dialogView.findView("etLink")
            } catch (t: Throwable) {
                Toast.makeText(requireContext(), "Dialog fields not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dlg = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create()

            dlg.setOnShowListener {
                dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val name = etName.text.toString().trim()
                    val link = etLink.text.toString().trim()
                    if (link.isEmpty()) { showToast("Please enter a link"); return@setOnClickListener }
                    val valid = try {
                        val scheme = link.toUri().scheme?.lowercase()
                        scheme == "http" || scheme == "https"
                    } catch (_: Exception) { false }
                    if (!valid) { showToast("Enter a valid URL (http/https)"); return@setOnClickListener }
                    try {
                        val list = loadLinks().toMutableList()
                        list.add(0, LinkItem(name = name.ifBlank { link }, link = link, type = "StremioC"))
                        saveLinks(list)
                        Toast.makeText(requireContext(), "Link saved", Toast.LENGTH_SHORT).show()
                        dlg.dismiss()
                    } catch (e: Throwable) {
                        Log.e("StreamPlayStremioCatelogFrag", "Failed to save link $e")
                        showToast("Failed to save link")
                    }
                }
            }
            dlg.show()
        }

        showlinks.setOnClickListener {
            val dialogView = getLayout("stremio_dialog_list_links", inflater, container)
            val dlg = AlertDialog.Builder(requireContext()).setView(dialogView).setPositiveButton("Close", null).create()
            val rv: RecyclerView = dialogView.findView("rvLinks")
            val tvNoLinks: TextView = dialogView.findView("tvNoLinks")
            val list = loadLinks().toMutableList()
            if (list.isEmpty()) {
                tvNoLinks.visibility = View.VISIBLE
                rv.visibility = View.GONE
            } else {
                tvNoLinks.visibility = View.GONE
                rv.visibility = View.VISIBLE
                rv.layoutManager = LinearLayoutManager(requireContext())
                rv.adapter = LinksAdapter(list) { itemToDelete ->
                    val updatedList = loadLinks().toMutableList()
                    if (updatedList.removeAll { it.id == itemToDelete.id }) {
                        saveLinks(updatedList)
                        (rv.adapter as? LinksAdapter)?.remove(itemToDelete)
                        showToast("Deleted")
                        if (updatedList.isEmpty()) { tvNoLinks.visibility = View.VISIBLE; rv.visibility = View.GONE }
                    }
                }
            }
            dlg.show()
        }

        saveIcon.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Save & Reload")
                .setMessage("Changes have been saved. Do you want to restart the app to apply them?")
                .setPositiveButton("Yes") { _, _ -> dismiss(); restartApp() }
                .setNegativeButton("No", null)
                .show()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {}

    private fun restartApp() {
        val context = requireContext().applicationContext
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.component?.let {
            context.startActivity(Intent.makeRestartActivityTask(it))
            Runtime.getRuntime().exit(0)
        }
    }

    data class LinkItem(val id: Long = System.currentTimeMillis(), val name: String, val link: String, val type: String)

    private fun loadLinks(): MutableList<LinkItem> {
        val json = sharedPref.getString(PREF_KEY_LINKS, null) ?: return mutableListOf()
        return try {
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                LinkItem(obj.optLong("id", System.currentTimeMillis()), obj.optString("name", ""), obj.optString("link", ""), obj.optString("type", "StremioC"))
            }.toMutableList()
        } catch (_: Exception) { mutableListOf() }
    }

    private fun saveLinks(list: List<LinkItem>) {
        val arr = org.json.JSONArray()
        list.forEach { item ->
            arr.put(org.json.JSONObject().apply {
                put("id", item.id); put("name", item.name); put("link", item.link); put("type", item.type)
            })
        }
        sharedPref.edit { putString(PREF_KEY_LINKS, arr.toString()) }
    }

    inner class LinksAdapter(private val items: MutableList<LinkItem>, private val onDelete: (LinkItem) -> Unit) : RecyclerView.Adapter<LinksAdapter.VH>() {
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findView("tvName")
            val tvLink: TextView = v.findView("tvLink")
            val tvType: TextView = v.findView("tvType")
            val btnDelete: ImageButton = v.findView("btnDelete")
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val layoutId = res.getIdentifier("stremio_item_saved_link", "layout", BuildConfig.LIBRARY_PACKAGE_NAME)
            val v = layoutInflater.inflate(res.getLayout(layoutId), parent, false)
            return VH(v)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvName.text = item.name; holder.tvLink.text = item.link; holder.tvType.text = item.type
            holder.btnDelete.setOnClickListener { onDelete(item) }
        }
        override fun getItemCount() = items.size
        fun remove(item: LinkItem) {
            val idx = items.indexOfFirst { it.id == item.id }
            if (idx >= 0) { items.removeAt(idx); notifyItemRemoved(idx) }
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }
}
