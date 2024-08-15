package com.payeasenet.wepay.net.exception

/**
 * @Description: [一句话描述该类的功能]
 * @Author: [wenbin.zhou@ehking.com]
 * @CreateDate: [2017/04/14 18:49]
 * @Version: [1.0]
 */
class ResultException(var code: String, msg: String) : RuntimeException(msg)
