package com.utilify.boost.cleaner.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.cam.scanner.scantopdf.android.R
import com.cam.scanner.scantopdf.android.activities.WebViewActivity
import com.itl.commonres.utils.CommonMethods
import com.utilify.boost.cleaner.databinding.ActivitySettingsMainBinding


class SettingsMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsMainBinding
    private var commonMethods: CommonMethods? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initObjects()
        clickListener()

    }

    override fun onStart() {
        super.onStart()
        CommonMethods.mLastClickTime = 0L
    }

    private fun initObjects() {
        commonMethods = CommonMethods(this)

        binding.mToolbar.tvTitle.text =
            getString(R.string.settings)

    }

    private fun clickListener() {
        binding.switchStorage.setOnCheckedChangeListener { view, _ ->
            if (view.isPressed) {
                commonMethods?.askUserToRequestAllFilesAccess(this)
            }
        }
        binding.mToolbar.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun openWebViewActivity(title: String?, url: String?) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        binding.switchStorage.isChecked = isStoragePermissionGranted()
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        } else {
            true
        }
    }

}