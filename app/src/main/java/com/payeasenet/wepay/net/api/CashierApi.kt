package com.payeasenet.wepay.net.api


import com.payeasenet.wepay.net.bean.BaseObjectResult
import com.payeasenet.wepay.net.bean.ResponseBean
import io.reactivex.Flowable
import retrofit2.http.*

/**
 * 类 `${CLASS_NAME}`
 *
 *
 * 描述：
 *
 * 创建日期：2017年07月13日
 *
 * @author zhaoyong.chen@ehking.com
 * @version 1.0
 */
interface WepayApi {

    /**
     * 开通钱包
     */
    @FormUrlEncoded
    @POST("webox/walletCreate")
    fun walletCreate(@FieldMap map: Map<String, String>): Flowable<ResponseBean.WalletCreate>

    /**
     * 充值预下单
     */
    @FormUrlEncoded
    @POST("webox/recharge")
    fun walletRecharge(@FieldMap map: Map<String, String>): Flowable<ResponseBean.Token>

    /**
     * 充值下单查询
     */
    @FormUrlEncoded
    @POST("webox/rechargeQuery")
    fun walletRechargeQuery(@FieldMap map: Map<String, String>): Flowable<ResponseBean.RechargeQuery>

    /**
     * 余额查询
     */
    @FormUrlEncoded
    @POST("webox/walletQuery")
    fun walletQuery(@FieldMap map: Map<String, String>): Flowable<ResponseBean.WalletQuery>

    /**
     * 提现预下单
     */
    @FormUrlEncoded
    @POST("webox/withholding")
    fun withholding(@FieldMap map: Map<String, String>): Flowable<ResponseBean.Token>

    /**
     * 提现查询
     */
    @FormUrlEncoded
    @POST("webox/withholdingQuery")
    fun walletWithholdingQuery(@FieldMap map: HashMap<String, String>): Flowable<ResponseBean.RechargeQuery>

    /**
     * 生成token
     */
    @FormUrlEncoded
    @POST("webox/clientTokenCreate")
    fun clientTokenCreate(@FieldMap map: HashMap<String, String>): Flowable<ResponseBean.Token>


    /**
     * 转账预下单
     */
    @FormUrlEncoded
    @POST("webox/transferCreateOrder")
    fun  transferCreateOrder(@FieldMap map: Map<String, String>): Flowable<ResponseBean.Token>

    /**
     * 转账查询
     */
    @FormUrlEncoded
    @POST("webox/transferQueryOrder")
    fun transferQueryOrder(@FieldMap map: HashMap<String, String>): Flowable<ResponseBean.RechargeQuery>

    /**
     * 转账拒收
     */
    @FormUrlEncoded
    @POST("webox/transferRefuseOrder")
    fun transferRefuseOrder(@FieldMap map: HashMap<String, String>): Flowable<Any>

    /**
     * 转账确认
     */
    @FormUrlEncoded
    @POST("webox/transferConfirmOrder")
    fun transferConfirmOrder(@FieldMap map: HashMap<String, String>): Flowable<Any>

   /**
     * 转账查询
     */
    @FormUrlEncoded
    @POST("webox/redPacketGrab")
    fun redPacketGrab(@FieldMap map: HashMap<String, String>): Flowable<Any>

    /**
     * 红包查询
     */
    @FormUrlEncoded
    @POST("webox/redPacketQuery")
    fun redPacketQuery(@FieldMap map: HashMap<String, String>): Flowable<ResponseBean.RechargeQuery>


    /**
     * 红包预下单
     */
    @FormUrlEncoded
    @POST("webox/redPacket")
    fun redPacket(@FieldMap map: HashMap<String, String>): Flowable<ResponseBean.Token>
    /**
     * 交易记录查询
     */
    @FormUrlEncoded
    @POST("webox/tradeRecordQuery")
    fun tradeRecordQuery(@FieldMap map: HashMap<String, String>): Flowable<ResponseBean.Records>


    /**
     * 交易记录查询
     */
    @FormUrlEncoded
    @POST("webox/tradeStatis")
    fun  tradeStatis(@FieldMap map: HashMap<String, String>): Flowable<ResponseBean.TradeStatis>

















    /**
     * 余额查询
     *
     */
    @GET("webox/wallet/query")
    fun walletQueryCHAT(@QueryMap map: Map<String, String>): Flowable<BaseObjectResult<ResponseBean.WalletQueryCHAT>>

    /**
     * 充值预下单
     *
     */
    @FormUrlEncoded
    @POST("webox/recharge/create")
    fun walletRechargeCHAT(@FieldMap map: Map<String, String>): Flowable<BaseObjectResult<ResponseBean.TokenCHAT>>

    /**
     * 充值下单查询
     *
     */
    @FormUrlEncoded
    @POST("webox/recharge/query")
    fun walletRechargeQueryCHAT(@FieldMap map: Map<String, String>): Flowable<BaseObjectResult<ResponseBean.RechargeQuery>>


    /**
     * 提现预下单
     *
     */
    @FormUrlEncoded
    @POST("webox/withholding/create")
    fun withholdingCHAT(@FieldMap map: Map<String, String>): Flowable<BaseObjectResult<ResponseBean.TokenCHAT>>

    /**
     * 提现查询
     */
    @FormUrlEncoded
    @POST("webox/withholding/query")
    fun walletWithholdingQueryCHAT(@FieldMap map: HashMap<String, String>): Flowable<BaseObjectResult<ResponseBean.RechargeQueryCHAT>>

    /**
     * 交易记录查询(账单明细)
     *
     */
    @FormUrlEncoded
    @POST("webox/account/trade/query")
    fun tradeRecordQueryCHAT(@FieldMap map: HashMap<String, String>): Flowable<BaseObjectResult<ResponseBean.RecordsCHAT>>

    /**
     * 红包统计
     *
     */
    @FormUrlEncoded
    @POST("webox/account/trade/stats")
    fun  tradeStatisCHAT(@FieldMap map: HashMap<String, String>): Flowable<BaseObjectResult<ResponseBean.TradeStatis>>

    /**
     * 生成token
     *
     */
    @FormUrlEncoded
    @POST("webox/account/token/create")
    fun clientTokenCreateCHAT(@FieldMap map: HashMap<String, String>): Flowable<BaseObjectResult<ResponseBean.TokenCHAT>>

    /**
     * 红包明细
     *
     */
    @FormUrlEncoded
    @POST("webox/account/trade/query")
    fun redEnvelopeQueryCHAT(@FieldMap map: HashMap<String, String>): Flowable<BaseObjectResult<ResponseBean.RecordsCHAT>>

    /**
     * 开通钱包
     *
     */
    @FormUrlEncoded
    @POST("webox/wallet/create")
    fun walletCreateCHAT(@FieldMap map: Map<String, String>): Flowable<BaseObjectResult<ResponseBean.WalletCreate>>

    /**
     * 红包查询
     *
     */
    @FormUrlEncoded
    @POST("webox/redpacket/query")
    fun redPacketQueryCHAT(@FieldMap map: HashMap<String, String>): Flowable<BaseObjectResult<ResponseBean.RechargeQuery>>


    /**
     * 红包预下单
     *
     */
    @FormUrlEncoded
    @POST("webox/redpacket/create")
    fun redPacketCHAT(@FieldMap map: HashMap<String, String>): Flowable<BaseObjectResult<ResponseBean.TokenCHAT>>

}
