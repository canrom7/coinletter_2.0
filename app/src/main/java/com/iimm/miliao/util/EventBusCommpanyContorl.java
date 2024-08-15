package com.iimm.miliao.util;

import com.iimm.miliao.bean.company.StructBeanNetInfo;

public class EventBusCommpanyContorl {
    public static int DEPARTMENT_ADD = 0;//创建部门
    public static int COMPANY_EXIT = 1;//退出团队
    public static int COMPANY_DISSOLVE = 2;//解散团队
    public static int DEPARTMENT_UPDATENAME = 3;//修改部门名称
    public static int DEPARTMENT_DELETER = 4;//删除部门
    public static int COMPANYNAME_UPDATE = 5;//修改团队名称
    public static int POSITION_NAME_UPDATE = 6;//修改职位名称
    public static int DEPARTMENT_CHANG = 7;//跟换部门
    public static int DELETE_EMPLOYEES = 8;//删除员工
    public static int ADD_EMPLOYEES = 9;//添加员工

    StructBeanNetInfo data;//团队数据
    private int contorlType;
    private int mDepartPostion;
    private int mCompanyPostion;
    private int mUserPostion;
    private boolean isRefresh = false;//判断是否是新数据
    private boolean isSend = true;//判断是否是发送

    public int getmUserPostion() {
        return mUserPostion;
    }

    public void setmUserPostion(int mUserPostion) {
        this.mUserPostion = mUserPostion;
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public String getDepartMentname() {
        return data.getDepartments().get(mDepartPostion).getDepartName();
    }

    public EventBusCommpanyContorl(StructBeanNetInfo data, int contorlType) {
        this.data = data;
        this.contorlType = contorlType;
    }

    public static EventBusCommpanyContorl getInstance(StructBeanNetInfo data, int type) {
        return new EventBusCommpanyContorl(data, type);
    }

    public StructBeanNetInfo getData() {
        return data;
    }


    public int getContorlType() {
        return contorlType;
    }

    public int getmDepartPostion() {
        return mDepartPostion;
    }

    public void setmDepartPostion(int mDepartPostion) {
        this.mDepartPostion = mDepartPostion;
    }

    public int getmCompanyPostion() {
        return mCompanyPostion;
    }

    public void setmCompanyPostion(int mCompanyPostion) {
        this.mCompanyPostion = mCompanyPostion;
    }
}
