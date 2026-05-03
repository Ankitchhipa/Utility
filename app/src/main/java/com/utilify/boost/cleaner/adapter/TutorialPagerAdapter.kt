package com.utilify.boost.cleaner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.utilify.boost.cleaner.R
import com.utilify.boost.cleaner.TutorialDataModel
import com.utilify.boost.cleaner.databinding.TutorialItemBinding
import java.util.Objects

class TutorialPagerAdapter(private val context: Context, private val list: ArrayList<TutorialDataModel>) :
    PagerAdapter() {

    private val mLayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return list.size;
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view == o
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = mLayoutInflater.inflate(R.layout.tutorial_item, container, false)
        val binding = TutorialItemBinding.bind(itemView)

        binding.img.setImageResource(list[position].image)
        binding.content.text = context.getString(list[position].content)

        Objects.requireNonNull(container).addView(binding.root);
        return itemView;
    }

    override fun destroyItem(container: ViewGroup, position: Int, o: Any) {
        container.removeView(o as View);
    }
}