package com.ogoons.downloadablefonts

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import kotlin.coroutines.CoroutineContext

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var btnLoad: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        view.findViewById<Button>(R.id.btn_open).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            try {
                startActivityForResult(Intent.createChooser(intent, "Open"), FILE_OPEN_REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }

        btnLoad = view.findViewById(R.id.btn_load)
        btnLoad?.isEnabled = false
        btnLoad?.setOnClickListener {
            val textView = view.findViewById<TextView>(R.id.textview_first)
            val fontPath = getDataPath(requireContext())
            val typeface = Typeface.createFromFile(fontPath)
            textView.typeface = typeface
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == Activity.RESULT_OK) {
            if (requestCode == FILE_OPEN_REQUEST_CODE) {
                val fileUri = data.data
                if (fileUri != null) {
                    importFontFile(fileUri)
                }
            }
        }
    }

    private fun importFontFile(fileUri: Uri) = launch {
        val parcelFileDescriptor = requireContext().contentResolver.openFileDescriptor(fileUri, "r")
        if (parcelFileDescriptor != null) {
            val filePath = getDataPath(requireContext())
            val bufferedInputStream =
                BufferedInputStream(FileInputStream(parcelFileDescriptor.fileDescriptor))
            val bufferedOutputStream = BufferedOutputStream(FileOutputStream(File(filePath)))
            val buf = ByteArray(1024)
            var len: Int
            while (bufferedInputStream.read(buf).also { len = it } >= 0) {
                bufferedOutputStream.write(buf, 0, len)
            }
            bufferedInputStream.close()
            bufferedOutputStream.flush()
            bufferedOutputStream.close()
            withContext(Dispatchers.Main) {
                btnLoad?.isEnabled = true
            }
        }
    }

    companion object {

        const val FILE_OPEN_REQUEST_CODE = 100

        private const val DATA_FILE_NAME = "temp-font.ttf"

        fun getDataPath(context: Context): String =
            "${context.filesDir.absolutePath}${File.separator}$DATA_FILE_NAME"
    }
}