package `in`.planckstudio.tweetcreator.chroma

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.fragment.app.DialogFragment
import `in`.planckstudio.tweetcreator.chroma.internal.ChromaView
import kotlin.properties.Delegates

class ChromaDialog constructor() : DialogFragment() {

    companion object {
        private const val ArgInitialColor = "arg_initial_color"
        private const val ArgColorModeName = "arg_color_mode_name"

        @JvmStatic
        private fun newInstance(@ColorInt initialColor: Int, colorMode: ColorMode): ChromaDialog {
            val fragment = ChromaDialog()
            fragment.arguments = makeArgs(initialColor, colorMode)
            return fragment
        }

        @JvmStatic
        private fun makeArgs(@ColorInt initialColor: Int, colorMode: ColorMode): Bundle {
            val args = Bundle()
            args.putInt(ArgInitialColor, initialColor)
            args.putString(ArgColorModeName, colorMode.name)
            return args
        }
    }

    class Builder {
        @ColorInt
        private var initialColor: Int = ChromaView.DefaultColor
        private var colorMode: ColorMode = ChromaView.DefaultModel
        private var listener: ColorSelectListener? = null

        fun initialColor(@ColorInt initialColor: Int): Builder {
            this.initialColor = initialColor
            return this
        }

        fun colorMode(colorMode: ColorMode): Builder {
            this.colorMode = colorMode
            return this
        }

        fun onColorSelected(listener: ColorSelectListener): Builder {
            this.listener = listener
            return this
        }

        fun create(): ChromaDialog {
            val fragment = newInstance(initialColor, colorMode)
            fragment.listener = listener
            return fragment
        }
    }

    private var listener: ColorSelectListener? = null
    private var chromaView: ChromaView by Delegates.notNull<ChromaView>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        chromaView = if (savedInstanceState == null) {
            ChromaView(
                requireArguments().getInt(ArgInitialColor),
                ColorMode.fromName(requireArguments().getString(ArgColorModeName)),
                context
            )
        } else {
            ChromaView(
                savedInstanceState.getInt(ArgInitialColor, ChromaView.DefaultColor),
                ColorMode.fromName(savedInstanceState.getString(ArgColorModeName)),
                context
            )
        }

        chromaView.enableButtonBar(object : ChromaView.ButtonBarListener {
            override fun onNegativeButtonClick() = dismiss()
            override fun onPositiveButtonClick(color: Int) {
                listener?.onColorSelected(color)
                dismiss()
            }
        })

        return AlertDialog.Builder(context).setView(chromaView).create().apply {
            setOnShowListener {
                val width: Int;
                val height: Int
                if (orientation(context) == ORIENTATION_LANDSCAPE) {
                    height = 210
                    width = 80 percentOf screenDimensions(context).widthPixels
                } else {
                    height = WindowManager.LayoutParams.WRAP_CONTENT
                    width = 310
                }
                window!!.setLayout(width, height)
            }
        }
    }

//  override fun onSaveInstanceState(outState: Bundle?) {
//    outState?.putAll(makeArgs(chromaView.currentColor, chromaView.colorMode))
//    super.onSaveInstanceState(outState)
//  }

    override fun onDestroyView() {
        super.onDestroyView()
        listener = null
    }
}
