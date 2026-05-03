package com.utilify.boost.cleaner.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.itl.commonres.utils.CommonMethods
import com.itl.commonres.utils.Constants
import com.utilify.boost.cleaner.R
import com.utilify.boost.cleaner.TutorialDataModel
import com.utilify.boost.cleaner.adapter.TutorialPagerAdapter
import com.utilify.boost.cleaner.databinding.ActivityTutorialBinding


class TutorialActivity : AppCompatActivity() {

    lateinit var binding: ActivityTutorialBinding
    var fromUseFulScreen = false
    private var screenName = "TutorialActivity"

    private var dataModel: ArrayList<TutorialDataModel> = arrayListOf(
        /*TutorialDataModel(
            R.string.t1_content,
            R.drawable.t_1
        ),*/
        TutorialDataModel(
            R.string.t2_content,
            R.drawable.t_2
        ),
        TutorialDataModel(
            R.string.t3_content,
            R.drawable.t_3
        ),
        TutorialDataModel(
            R.string.t4_content,
            R.drawable.t_4
        ),
        TutorialDataModel(
            R.string.t5_content,
            R.drawable.t_5
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CommonMethods.logCustomFireBaseEvents(screenName, Constants.Launch_Tutorial_Screen)

        getDataFromIntent()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            dataModel.removeAt(0)
        }


        val adapter = TutorialPagerAdapter(this, dataModel)
        binding.viewPager.setAdapter(adapter)

        binding.dot.setViewPager(binding.viewPager)
        setProgress()

        binding.skip.setOnClickListener {
            if(fromUseFulScreen) {
                finish()
            }else {
                startActivity(Intent(this, UniScanDashboardActivity::class.java))
                finishAffinity()
            }
        }

        binding.next.setOnClickListener {
            if (binding.viewPager.currentItem == dataModel.size - 1) {
                if(fromUseFulScreen) {
                    finish()
                }else {
                    startActivity(Intent(this, UniScanDashboardActivity::class.java))
                    finishAffinity()
                }
            } else {
                binding.viewPager.setCurrentItem(binding.viewPager.currentItem + 1, true)
                setProgress()
            }
        }

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                setProgress()
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })
    }

    private fun getDataFromIntent() {
        val i = intent
        fromUseFulScreen = i.getBooleanExtra(Constants.FROM_USEFUL_SCREEN,false)
    }

    fun setProgress() {
        binding.prograssBar.progress =
            (binding.viewPager.currentItem + 1) * 100 / dataModel.size
    }
}