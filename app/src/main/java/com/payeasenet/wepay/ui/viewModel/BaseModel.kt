package com.payeasenet.wepay.ui.viewModel

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * MrLiu253@163.com
 * @time 2020-01-10
 */
open class BaseModel{
    var  mCompositeDisposable: CompositeDisposable? = null;

    fun addSubscribe(paramDisposable: Disposable) {
        if (this.mCompositeDisposable == null) {
            this.mCompositeDisposable = CompositeDisposable()
        }
        this.mCompositeDisposable!!.add(paramDisposable);
    }

    fun unSubscribe(){
        if (this.mCompositeDisposable != null) {
            this.mCompositeDisposable!!.dispose();
        }
    }

}