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
import com.payeasenet.wepay.ui.viewModel.AllViewModel
import com.iimm.miliao.R
import kotlinx.android.synthetic.main.all_fragment.*
import java.util.*


private var page = 1
private var adapter: TransactionAdapter? = null
private var mData = ArrayList<ResponseBean.Transaction>()

class AllFragment : Fragment() {

    companion object {
        fun newInstance( type:Int) : AllFragment{
            val args = Bundle()

            args.putInt("type", type)
            val fragment = AllFragment()
            fragment.arguments =args
            return fragment

        }
    }

    private lateinit var viewModel: AllViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.all_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AllViewModel::class.java)
        mData.clear()
        page=1
        viewModel.getData(this, page)

        all_recycler_view.setLinearLayout()
        adapter =TransactionAdapter(activity!!.applicationContext, mData)
        adapter?.setOnItemClickListener { view, position ->if(position< mData.size){
              startActivity(Intent(activity!!.applicationContext,TransactionDetailActivity::class.java
              ).putExtra("transaction", mData[position]))
            }
        }
        all_recycler_view.pullRefreshEnable = true
        all_recycler_view.isHasMore = true
        all_recycler_view.setAdapter(adapter)
        all_recycler_view.setOnPullLoadMoreListener(object :PullLoadMoreRecyclerView.PullLoadMoreListener {
            override fun onRefresh() {
                page = 1
                mData.clear()
                viewModel.getData(this@AllFragment, page)
                all_recycler_view.setPullLoadMoreCompleted()
            }

            override fun onLoadMore() {
                    page += 1
                    viewModel.getData(this@AllFragment, page)

            }
        })
    }

    fun setOpenLoadMore(open: Boolean) {

        all_recycler_view.isHasMore = open
    }
   fun finishComplete(){
        all_recycler_view.setPullLoadMoreCompleted()
    }

    fun setData(data: ArrayList<ResponseBean.Transaction>) {
        if(data.size>0) {
            mData.addAll(data)
            adapter?.data = mData
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        viewModel.disposeView()
        super.onDestroyView()
    }

}
