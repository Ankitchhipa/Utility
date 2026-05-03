package com.utilify.boost.cleaner.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.advanced.phone.junk.cache.cleaner.booster.antimalware.utility.SharedPrefUtil
import com.itl.commonres.utils.CommonMethods
import com.utilify.boost.cleaner.BuildConfig
import com.utilify.boost.cleaner.R
import com.utilify.boost.cleaner.databinding.ActivityAboutUsScreenBinding


class AboutUsScreenActivity : AppCompatActivity() {

    lateinit var binding: ActivityAboutUsScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.version.text = "App Version : ${BuildConfig.VERSION_NAME}"
        binding.mToolbar.tvTitle.text = getString(R.string.about_us)
        binding.mToolbar.ivBack.setOnClickListener {
            finish()
        }

        binding.link.setOnClickListener {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://mobibuz.com/")).apply {
                startActivity(this)
            }
        }

        CommonMethods(this).setAppName(binding.tvAppname, this)
        val curretDBVersion = SharedPrefUtil(baseContext).getInt(SharedPrefUtil.CURRENTDB)

        if(curretDBVersion != 0) {
            binding.databaseVersion.text = getString(R.string.anti_malware_database_version,curretDBVersion.toString())
            binding.databaseVersion.visibility = View.VISIBLE
        }
    }
}