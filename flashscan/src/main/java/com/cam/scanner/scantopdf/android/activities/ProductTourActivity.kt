package com.cam.scanner.scantopdf.android.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cam.scanner.scantopdf.android.AppController
import com.cam.scanner.scantopdf.android.R
import com.cam.scanner.scantopdf.android.databinding.ActivityProductTourBinding
import com.cam.scanner.scantopdf.android.db.DBHandler
import com.cam.scanner.scantopdf.android.rest.RegisterToApi
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult
import com.cam.scanner.scantopdf.android.util.Constants
import com.cam.scanner.scantopdf.android.util.FlashScanUtil
import com.cam.scanner.scantopdf.android.util.PrefManager
import kotlin.system.exitProcess

class ProductTourActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG: String = ProductTourActivity::class.java.simpleName
    private lateinit var binding: ActivityProductTourBinding

    private lateinit var layouts: ArrayList<Int> /*= ArrayList()*/
    private var prefManager: PrefManager? = null
    private var fromNav = 0
    private var util: FlashScanUtil? = null
    private var dbHandler: DBHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductTourBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntents()
        initObjects()

        if (Constants.IS_OWN_API_IMPLEMENT) {
            registerToApi()
        }

        clickListeners()
        setUpLayouts()
        setUpViewPager()
    }

    private fun initObjects() {
        prefManager = PrefManager(this)
        util = FlashScanUtil(this)
        dbHandler = AppController.getINSTANCE().dbHandler
        prefManager?.isSplashDone = true
    }

    private fun getIntents() {
        intent?.let {
            fromNav = it.getIntExtra(Constants.FROM_NAV, 0)
        }
    }

    private fun registerToApi() {
        if (fromNav == Constants.FROM_SPLASH && util!!.isConnectingToInternet) {
            RegisterToApi(this, object : OnApiResult {
                override fun onApiResponse() {
                    Log.i(TAG, "onApiResponse")
                }

                override fun onApiFailure() {
                    Log.i(TAG, "onApiFailure")
                }
            }).doRegister()
        }
    }

    private fun clickListeners() {
        binding.btnNext.setOnClickListener(this)
        binding.btnSkip.setOnClickListener(this)
    }

    private fun setUpViewPager() {
        val myViewPagerAdapter = MyViewPagerAdapter(this,layouts)
        binding.viewPager.adapter = myViewPagerAdapter
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                addBottomDots(position)

                if (position == layouts.size - 1) {
                    binding.btnSkip.visibility = View.GONE
                    binding.btnNext.text = getString(R.string.got_it)
                } else {
                    binding.btnSkip.visibility = View.VISIBLE
                    binding.btnNext.text = getString(R.string.str_next)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    private fun setUpLayouts() {
        layouts = arrayListOf(
            R.layout.app_tour_slide1,
            R.layout.app_tour_slide2,
            R.layout.app_tour_slide3,
            R.layout.app_tour_slide4,
            R.layout.app_tour_slide5
        )
        addBottomDots(0)
    }

    private fun addBottomDots(currentPage: Int) {
        val dots = arrayOfNulls<TextView>(layouts.size)

        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)

        binding.llDots.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]!!.text = Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_LEGACY)
            dots[i]!!.textSize = 35f
            dots[i]!!.setTextColor(colorsInactive[currentPage])
            binding.llDots.addView(dots[i])
        }

        if (dots.isNotEmpty()) dots[currentPage]!!.setTextColor(colorsActive[currentPage])
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_skip -> launchHomeScreen()
            R.id.btn_next -> {
                val current = getItem(+1)
                if (current < layouts.size) {
                    binding.viewPager.currentItem = current
                } else {
                    launchHomeScreen()
                }
            }
        }
    }

    private fun launchHomeScreen() {
        prefManager?.setFirstTimeLaunch(true)
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                R.anim.slide_from_right,
                R.anim.slide_to_left
            )
        } else {*/
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
//        }
        finish()
    }

    private fun getItem(i: Int): Int {
        return binding.viewPager.currentItem + i
    }

    private class MyViewPagerAdapter(val context: Context, val layouts: ArrayList<Int>) : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = LayoutInflater.from(context)
                .inflate(layouts[position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }

    override fun onBackPressed() {
        if (prefManager?.isFirstTimeLaunched == false) {
            finish()
//            showExitAppDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun showExitAppDialog() {
        val positiveClickListener = DialogInterface.OnClickListener { dialog, _ ->
            dialog.cancel()
            finishAffinity()
            exitProcess(0)
        }
        val negativeClickListener = DialogInterface.OnClickListener { dialog, _ ->
            dialog.cancel()
        }
        util?.showAlertDialog(
            "",
            getString(R.string.exit_msg),
            getString(R.string.yes),
            positiveClickListener,
            getString(R.string.no),
            negativeClickListener
        )
    }
}
