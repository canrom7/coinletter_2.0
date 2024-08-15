package com.payeasenet.wepay.ui.fragment

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ehking.sdk.wepay.ui.pullloadmorerecyclerview.PullLoadMoreRecyclerView
import com.payeasenet.wepay.net.bean.ResponseBean
import com.payeasenet.wepay.ui.activity.TransactionDetailActivity
import com.payeasenet.wepay.ui.view.adapter.TransactionAdapter
import com.payeasenet.wepay.ui.viewModel.IncomeViewModel
import com.iimm.miliao.R
import kotlinx.android.synthetic.main.income_fragment.*
import java.util.*


private var page = 1
private var adapter: TransactionAdapter? = null
private var mData = ArrayList<ResponseBean.Transaction>()

class IncomeFragment : Fragment() {

    companion object {
        fun newInstance( type:Int) : IncomeFragment{
            val args = Bundle()

            args.putInt("type", type)
            val fragment = IncomeFragment()
            fragment.arguments =args
            return fragment

        }
    }

    private lateinit var viewModel: IncomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.income_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(IncomeViewModel::class.java)
        mData.clear()
        page=1
        viewModel.getData(this, page)
        recycler_view.setLinearLayout()
        adapter =TransactionAdapter(activity!!.applicationContext, mData)
        adapter?.setOnItemClickListener { view, position ->if(position< mData.size){
            startActivity(
                Intent(activity!!.applicationContext, TransactionDetailActivity::class.java
                ).putExtra("transaction", mData[position]))
            }
        }
        recycler_view.setAdapter(adapter)
        recycler_view.setOnPullLoadMoreListener(object :PullLoadMoreRecyclerView.PullLoadMoreListener {
            override fun onRefresh() {
                page = 1
                mData.clear()
                viewModel.getData(this@IncomeFragment, page)
                recycler_view.setPullLoadMoreCompleted()
            }

            override fun onLoadMore() {

                  page += 1
                 viewModel.getData(this@IncomeFragment, page)
            }
        })
    }

    fun setOpenLoadMore(open: Boolean) {

       recycler_view.isHasMore = open
    }

    fun setData(data: ArrayList<ResponseBean.Transaction>) {
        if(data.size>0) {
            mData.addAll(data)
            adapter?.data = mData
            adapter?.notifyDataSetChanged()
        }
    }
    fun finishComplete(){
        recycler_view.setPullLoadMoreCompleted()
    }
    override fun onDestroyView() {
        viewModel.disposeView()
        super.onDestroyView()
    }

}
