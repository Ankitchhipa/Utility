package com.utilify.boost.cleaner.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.itl.commonres.R
import com.itl.commonres.utils.CommonMethods
import com.itl.commonres.utils.Constants
import com.utilify.boost.cleaner.databinding.ActivityReferFriendBinding

class ReferFriendActivity : AppCompatActivity() {

    lateinit var binding: ActivityReferFriendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReferFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.referFriendToolbar.ivBack.setOnClickListener { finish() }

        binding.referFriendToolbar.tvTitle.text = "Refer a Friend"

        binding.share.setOnClickListener {
            Log.e("Playstore Url==", "onCreate: ${Constants.playStoreUrl}")
            CommonMethods.logCustomFireBaseEvents(
                "ReferFriendActivity",
                Constants.CLICK_SHARE
            )
            val shareContent =
                getString(R.string.share_content, Constants.playStoreUrl)
            shareLink(shareContent)
        }

    }

    private fun shareLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
        }
        startActivity(Intent.createChooser(intent, "Share link via"))
    }
}