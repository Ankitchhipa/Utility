package com.utilify.boost.cleaner.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.advanced.phone.junk.cache.cleaner.booster.antimalware.FeedbackActivity
import com.itl.commonres.R
import com.itl.commonres.utils.CommonMethods
import com.itl.commonres.utils.Constants
import com.utilify.boost.cleaner.databinding.ActivityUsefullScreenBinding

class UsefullScreenActivity : AppCompatActivity() {

    private var commonMethods: CommonMethods? = null
    private var screenName = "UsefulLinksScreenActivity"

    private lateinit var binding: ActivityUsefullScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUsefullScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initObjects()
        clickListeners()

    }

    private fun initObjects() {
        commonMethods = CommonMethods(this)
    }

    private fun clickListeners() {
        binding.apply {
            mToolbarUsefull.tvTitle.text = "Useful Links"
            mToolbarUsefull.ivBack.setOnClickListener {
                finish()
            }

            feedback.setOnClickListener {
                CommonMethods.logCustomFireBaseEvents(
                    screenName,
                    Constants.CLICK_FEEDBACK
                )
                startActivity(
                    Intent(
                        this@UsefullScreenActivity,
                        FeedbackActivity::class.java
                    )
                )
            }

            privacyPolicy.setOnClickListener {
                CommonMethods.logCustomFireBaseEvents(
                    screenName,
                    Constants.CLICK_PRIVACY_POLICY
                )
                //commonMethods?.redirectToBrowser(Constants.privacyPolicyUrl)
                openWebViewActivity(getString(com.utilify.boost.cleaner.R.string.privacy_policy),Constants.privacyPolicyUrl)
            }

            termsOfUses.setOnClickListener {
                CommonMethods.logCustomFireBaseEvents(
                    screenName,
                    Constants.CLICK_TERMS_OF_USE
                )
                //commonMethods?.redirectToBrowser(Constants.termsOfUseUrl)
                openWebViewActivity(getString(com.utilify.boost.cleaner.R.string.terms_of_uses),Constants.termsOfUseUrl)
            }

            helpCenter.setOnClickListener {
                CommonMethods.logCustomFireBaseEvents(
                    screenName,
                    Constants.CLICK_HELP_CENTER
                )
                //commonMethods?.redirectToBrowser(Constants.helpUrl)
                openWebViewActivity(getString(com.utilify.boost.cleaner.R.string.help_center),Constants.helpUrl)
            }

            tip.setOnClickListener {
                startActivity(
                    Intent(
                        this@UsefullScreenActivity,
                        TutorialActivity::class.java
                    ).putExtra(Constants.FROM_USEFUL_SCREEN, true)
                )
            }
        }
    }

    private fun openWebViewActivity(title: String?, url: String?) {
        val intent = Intent(this, com.cam.scanner.scantopdf.android.activities.WebViewActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("url", url)
        startActivity(intent)
    }
}
