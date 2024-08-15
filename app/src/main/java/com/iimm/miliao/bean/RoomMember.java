package com.iimm.miliao.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.iimm.miliao.db.dao.RoomMemberDaoImpl;
import com.iimm.miliao.util.Constants;

/**
 * Created by zq on 2017/6/27 0027.
 */
@DatabaseTable(daoClass = RoomMemberDaoImpl.class)
public class RoomMember implements Parcelable {
    @DatabaseField(generatedId = true)
    private int _id;
    // 房间id
    @DatabaseField
    private String roomId;
    // 用户id
    @DatabaseField
    private String userId;
    // 用户昵称 A
    @DatabaseField
    private String userName;
    // 群主对该群内成员的备注名 仅群主可见
    @DatabaseField
    private String cardName;

    @DatabaseField
    private int vipLevel;
    /**
     * 1创建者，2管理员，3成员,
     * 4, 隐身人，
     * 5，监控人，
     * 隐身人和监控人：即群主设置某成员为这2个角色，则群员数量减1,其他人完全看不到他；隐身人和监控人的区别是，前者不可以说话，后者能说话。
     */
    // 职位
    @DatabaseField
    private int role;
    // 加入时间
    @DatabaseField
    private int createTime;

    @DatabaseField
    private long lastOnLineTime;  //上次在时间

    @DatabaseField
    private long talkTime;

    public long getTalkTime() {
        return talkTime;
    }

    public void setTalkTime(long talkTime) {
        this.talkTime = talkTime;
    }

    public long getLastOnLineTime() {
        return lastOnLineTime;
    }

    public void setLastOnLineTime(long lastOnLineTime) {
        this.lastOnLineTime = lastOnLineTime;
    }

    public RoomMember() {

    }

    public static boolean shouldSendRead(Integer role) {
        if (role == null) {
            return true;
        }
        return role != 4 && role != 5;
    }

    /**
     * 返回是群主或者管理员，
     * 用于显示管理员头像相框，
     */
    public boolean isGroupOwnerOrManager() {
        return getRole() == Constants.ROLE_OWNER || getRole() == Constants.ROLE_MANAGER;
    }

    /**
     * 全员禁言是否对此人生效，
     * {@link com.iimm.miliao.bean.message.MucRoomMember#isAllBannedEffective}
     * {@link com.iimm.miliao.bean.RoomMember#isAllBannedEffective}
     */
    public boolean isAllBannedEffective() {
        return getRole() == Constants.ROLE_MEMBER;
    }

    /**
     * 是否是隐身人，不能发言，
     */
    public boolean isInvisible() {
        return getRole() == Constants.ROLE_INVISIBLE;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this._id);
        dest.writeString(this.roomId);
        dest.writeString(this.userId);
        dest.writeString(this.userName);
        dest.writeString(this.cardName);
        dest.writeInt(this.vipLevel);
        dest.writeInt(this.role);
        dest.writeInt(this.createTime);
        dest.writeLong(this.lastOnLineTime);
        dest.writeLong(this.talkTime);
    }

    protected RoomMember(Parcel in) {
        this._id = in.readInt();
        this.roomId = in.readString();
        this.userId = in.readString();
        this.userName = in.readString();
        this.cardName = in.readString();
        this.vipLevel = in.readInt();
        this.role = in.readInt();
        this.createTime = in.readInt();
        this.lastOnLineTime = in.readLong();
        this.talkTime = in.readLong();
    }

    public static final Parcelable.Creator<RoomMember> CREATOR = new Parcelable.Creator<RoomMember>() {
        @Override
        public RoomMember createFromParcel(Parcel source) {
            return new RoomMember(source);
        }

        @Override
        public RoomMember[] newArray(int size) {
            return new RoomMember[size];
        }
    };
}
