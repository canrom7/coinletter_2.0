package com.payeasenet.wepay.net.bean

import java.io.Serializable

/**
 * 类 `ResponseBean`
 *
 *
 * 描述：
 *
 * 创建日期：2019年11月19日
 *
 * @author zhaoyong.chen@ehking.com
 * @version 1.0
 */
object ResponseBean{
    data class Records(var records: ArrayList<Transaction>)
    data class Token(var token: String)
    data class Statistics(var averageAmount: String, var count: String, var sumAmount: String)
    data class TradeStatis(var increaseTradeStatisVO: Statistics)
    data class WalletCreate(var walletId: String)
    data class RechargeQuery(var amount: String, var orderStatus: String)
    data class WalletQuery(var idCardNoDesc: String?, var authTimes: String?, var idCardRzStatus: String?, var balance: String, var mobileDesc: String?, var nameDesc: String?, var operatorRzStatus: String)
    data class Transaction(var serialNumber: String, var tradeSubType: String, var redPacketReceiveAmount: String, var redPacketCount: String, var redPacketReceiveCount: String, var requestId: String, var direction: String, var paymentType: String, var tradeType: String, var completeDateTime: String, var amount: String, var status: String, var createDateTime: String) : Serializable

    data class WalletQueryCHAT(var nickName: String, var idCardNoDesc: String?, var authTimes: String?, var idCardRzStatus: String?, var balance: String, var mobileDesc: String?, var nameDesc: String?, var operatorRzStatus: String)
    data class TokenCHAT(var token: String, var requestId: String, var merchantId: String, var walletId: String)
    data class RechargeQueryCHAT(var amount: String, var orderStatus: String)
    data class RecordsCHAT(var records: ArrayList<Transaction>, var walletId: String)
}
