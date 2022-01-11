package com.example.imagetopdfkotlin.Fragment

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.example.imagetopdfkotlin.Activity.PolicyActivity
import com.example.imagetopdfkotlin.BuildConfig
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.databinding.FragmentSettingBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    var settingBinding: FragmentSettingBinding? = null
    var view1: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingBinding =
            DataBindingUtil.inflate(
                inflater, R.layout.fragment_setting, container, false
            )
        view1 = settingBinding?.root
        settingBinding?.mShare?.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Calendar")
                var shareMessage = "\nLet me recommend you this application\n\n"
                shareMessage =
                    """
        ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
        
        
        """.trimIndent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: Exception) {
                //e.toString();
            }
        }

        settingBinding?.mRate?.setOnClickListener {
            showRateDialog()
        }

        settingBinding?.mFeedback?.setOnClickListener {
            sendFeedback()
        }

        settingBinding?.mPolicy?.setOnClickListener {
            val intent = Intent(activity, PolicyActivity::class.java)
            startActivity(intent)
        }
        return view1
    }

    fun sendFeedback() {
        val emailIntent = Intent(
            Intent.ACTION_SENDTO,
            Uri.parse("mailto:" + "bitbotdevelopers@gmail.com")
        )
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email via..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                requireActivity().applicationContext,
                "There are no email clients installed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showRateDialog() {
        val dialog = Dialog(requireActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialige_rate_us)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.setCanceledOnTouchOutside(true)
        val animationView = dialog.findViewById(R.id.animationView) as LottieAnimationView
        animationView.animate()
        val yesBtn = dialog.findViewById(R.id.yes) as TextView
        val noBtn = dialog.findViewById(R.id.no) as TextView
        yesBtn.setOnClickListener {
            val appPackageName =
                requireActivity().packageName

            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appPackageName")
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
            dialog.dismiss()
        }
        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}