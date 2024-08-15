package com.iimm.miliao.bean.message;

import com.j256.ormlite.field.DatabaseField;
import com.iimm.miliao.timer.ItemTimer;
import com.iimm.miliao.timer.ItemTimerListener;

public abstract class XmppMessage implements ItemTimerListener {
    public ItemTimer itemTimer;
    public static final long resendTimePeriod = 5 * 1000; //重发时间间隔
    public static final int resendCount = 3; //重发次数(加上第一次发送的，一共发送4次)
    @DatabaseField(canBeNull = false)
    protected String packetId;// 消息包的Id
    /* 网络传输字段 */
    @DatabaseField(canBeNull = false)
    protected int type;// 消息的类型
    @DatabaseField(canBeNull = false)
    protected double timeSend; // 发送时间，秒级别的(但是带小数点，小数点后的值为毫秒)，为点击发送按钮，开始发送的时间
    @DatabaseField
    protected boolean isMySend = true;// 是否是由我自己发送，代替toUserId，toUserId废弃不用,默认值true，代表是我发送的

    public boolean isMySend() {
        return isMySend;
    }

    public void setMySend(boolean isMySend) {
        this.isMySend = isMySend;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    // Todo 2018-11-28 by zq
    // Todo 服务器在压测时，一秒钟会发送很多条消息，且timeSend为double类型，但本地之前为long类型，在存入数据库的时候会将double类型的timeSend会强转为long类型
    // Todo 这时，在查询数据库的时候如果为同一秒发送的消息顺序排列会不准确
    // Todo 在将TimeSend改为double类型之后，因为getTimeSend()这个方法有太多地方调用了，所以我们return 的时候强转下，而不去动其他类
    // Todo 改为double只是用于数据库查询 ex:' builder.orderBy("timeSend", false);' 其它地方不会有影响
    public long getTimeSend() {
        return (long) timeSend;
    }

    public void setTimeSend(long timeSend) {
        this.timeSend = timeSend;
    }

    public double getDoubleTimeSend() {
        return timeSend;
    }

    public void setDoubleTimeSend(double timeSend) {
        this.timeSend = timeSend;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }
}
