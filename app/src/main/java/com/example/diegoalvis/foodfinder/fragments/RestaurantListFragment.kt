package com.example.diegoalvis.foodfinder.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diegoalvis.foodfinder.R
import com.example.diegoalvis.foodfinder.adapters.RestaurantAdapter
import com.example.diegoalvis.foodfinder.utils.applyUISchedulers
import com.example.diegoalvis.foodfinder.vm.SharedViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.list_restaurant_fragment.view.*


class RestaurantListFragment : Fragment() {

    companion object {
        const val TAG = "list_restaurant_fragment"
        fun newInstance() = RestaurantListFragment()
    }

    private lateinit var viewModel: SharedViewModel
    private val adapter = RestaurantAdapter(this::onRepoClick)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.list_restaurant_fragment, container, false)

        view.list.adapter = adapter
        view.list.layoutManager = LinearLayoutManager(activity!!)

        var mPreviousTotal = 0
        var mLoading = true
        view.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = recyclerView.layoutManager?.itemCount
                if (mLoading) {
                    if (totalItemCount != null) {
                        if (totalItemCount > mPreviousTotal) {
                            mLoading = false
                            mPreviousTotal = totalItemCount
                        }
                    }
                }

                val visibleThreshold = 8
                if (totalItemCount != null) {
                    val itemPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (!mLoading && (itemPosition + visibleThreshold) >= totalItemCount) {
                        mLoading = true
                        onLoadMore()
                    }
                }
            }

            @SuppressLint("CheckResult")
            fun onLoadMore() {
                viewModel
                    .getMoreRepos()
                    .applyUISchedulers()
                    .subscribe({
                        val startIndex = adapter.data.size
                        adapter.data.addAll(it.data)
                        adapter.notifyItemRangeInserted(startIndex, adapter.data.size)
                    }, {
                        it.printStackTrace()
                        activity?.findViewById<View>(android.R.id.content)?.let {
                            Snackbar.make(it, "Host unreachable", Snackbar.LENGTH_LONG).show()
                        }
                    })
            }
        })

        viewModel = ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
        viewModel.restaurants.observe(this, Observer {
            mLoading = false
            mPreviousTotal = 0
            adapter.data = it.distinct().toMutableList()
        })

        return view
    }

    private fun onRepoClick(pos: Int) {
        viewModel.select(adapter.data[pos])
    }
}