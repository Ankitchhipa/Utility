package com.utilify.boost.cleaner.adapter

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.itl.commonres.utils.CommonMethods
import com.itl.commonres.utils.Constants
import com.utilify.boost.cleaner.BannerEnum
import com.utilify.boost.cleaner.R
import com.utilify.boost.cleaner.databinding.RecommendedAppItemBinding

class RecommendeAppAdapter(private val list: List<Int>, private val context: Context) :
    RecyclerView.Adapter<RecommendeAppAdapter.VH>() {

    class VH(val binding: RecommendedAppItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            RecommendedAppItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.banner.setImageResource(
            when (list[position]) {
                BannerEnum.AnytimeAstro.value -> {
                    R.drawable.anytime_astro_banner
                }

                BannerEnum.TarotLife.value -> {
                    R.drawable.tarotlife_banner
                }

                else -> {
                    R.drawable.tarotlife_banner
                }
            }
        )
        holder.binding.banner.setOnClickListener {
            val packageName = if (list[position] == BannerEnum.AnytimeAstro.value) {
                CommonMethods.logCustomFireBaseEvents(
                    "UniScanDashboardActivity",
                    Constants.CLICK_ANYTIME_ASTRO_AD
                )

                "com.dashaspeaks.astrology.online.chat"
            } else {

                CommonMethods.logCustomFireBaseEvents(
                    "UniScanDashboardActivity",
                    Constants.CLICK_TAROT_LIFE_AD
                )
                "com.tarotlife.tarot.card.reading.numerology"
            }
            openAppOrPlayStore(context, packageName)
        }
    }

    private fun openAppOrPlayStore(context: Context, packageName: String) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            // App is installed, open it
            context.startActivity(intent)
        } else {
            // App is not installed, open Play Store
            try {
                val playStoreIntent = Intent(Intent.ACTION_VIEW)
                playStoreIntent.data = Uri.parse("market://details?id=$packageName")
                context.startActivity(playStoreIntent)
            } catch (e: ActivityNotFoundException) {
                // Play Store app is not available, open Play Store in browser
                val playStoreWebIntent = Intent(Intent.ACTION_VIEW)
                playStoreWebIntent.data =
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                context.startActivity(playStoreWebIntent)
            }
        }
    }

}