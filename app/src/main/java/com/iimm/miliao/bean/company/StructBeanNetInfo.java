package com.iimm.miliao.bean.company;

import java.io.Serializable;
import java.util.List;

public class StructBeanNetInfo implements Serializable {

    /**
     * companyName : 扶风
     * createTime : 1519964750
     * createUserId : 10008297
     * deleteTime : 0
     * deleteUserId : 0
     * departments : [{"companyId":"5a98d24e4adfdc63ca4530c1","createTime":1519964750,"createUserId":10008297,"departName":"人事部","empNum":8,"employees":[{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98d24e4adfdc63ca4530c6","isCustomer":0,"isPause":0,"nickname":"曾琪","operationType":0,"position":"员工","role":3,"userId":10008297},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453153","isCustomer":0,"isPause":0,"nickname":"韩潇","operationType":0,"position":"员工","role":0,"userId":10010228},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453154","isCustomer":0,"isPause":0,"nickname":"刘轩","operationType":0,"position":"员工","role":0,"userId":10008291},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453155","isCustomer":0,"isPause":0,"nickname":"ZH4","operationType":0,"position":"员工","role":0,"userId":10007042},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453156","isCustomer":0,"isPause":0,"nickname":"小兔","operationType":0,"position":"员工","role":0,"userId":10004541},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453157","isCustomer":0,"isPause":0,"nickname":"路路通","operationType":0,"position":"员工","role":0,"userId":10004476},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453158","isCustomer":0,"isPause":0,"nickname":"小敏","operationType":0,"position":"员工","role":0,"userId":10008945},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453159","isCustomer":0,"isPause":0,"nickname":"10000","operationType":0,"position":"员工","role":0,"userId":10000}],"id":"5a98d24e4adfdc63ca4530c3","parentId":"5a98d24e4adfdc63ca4530c2","type":0}]
     * empNum : 8
     * id : 5a98d24e4adfdc63ca4530c1
     * noticeContent : 暂无公告
     * noticeTime : 1519975095
     * rootDpartId : ["5a98d24e4adfdc63ca4530c2"]
     * type : 0
     */

    private String companyName;
    private int createTime;
    private int createUserId;
    private int deleteTime;
    private int deleteUserId;
    private int empNum;
    private String id;
    private String noticeContent;
    private int noticeTime;
    private int type;
    private List<DepartmentsBean> departments;
    private List<String> rootDpartId;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(int createUserId) {
        this.createUserId = createUserId;
    }

    public int getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(int deleteTime) {
        this.deleteTime = deleteTime;
    }

    public int getDeleteUserId() {
        return deleteUserId;
    }

    public void setDeleteUserId(int deleteUserId) {
        this.deleteUserId = deleteUserId;
    }

    public int getEmpNum() {
        return empNum;
    }

    public void setEmpNum(int empNum) {
        this.empNum = empNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNoticeContent() {
        return noticeContent;
    }

    public void setNoticeContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }

    public int getNoticeTime() {
        return noticeTime;
    }

    public void setNoticeTime(int noticeTime) {
        this.noticeTime = noticeTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<DepartmentsBean> getDepartments() {
        return departments;
    }

    public void setDepartments(List<DepartmentsBean> departments) {
        this.departments = departments;
    }

    public List<String> getRootDpartId() {
        return rootDpartId;
    }

    public void setRootDpartId(List<String> rootDpartId) {
        this.rootDpartId = rootDpartId;
    }

    public static class DepartmentsBean implements Serializable {
        /**
         * companyId : 5a98d24e4adfdc63ca4530c1
         * createTime : 1519964750
         * createUserId : 10008297
         * departName : 人事部
         * empNum : 8
         * employees : [{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98d24e4adfdc63ca4530c6","isCustomer":0,"isPause":0,"nickname":"曾琪","operationType":0,"position":"员工","role":3,"userId":10008297},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453153","isCustomer":0,"isPause":0,"nickname":"韩潇","operationType":0,"position":"员工","role":0,"userId":10010228},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453154","isCustomer":0,"isPause":0,"nickname":"刘轩","operationType":0,"position":"员工","role":0,"userId":10008291},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453155","isCustomer":0,"isPause":0,"nickname":"ZH4","operationType":0,"position":"员工","role":0,"userId":10007042},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453156","isCustomer":0,"isPause":0,"nickname":"小兔","operationType":0,"position":"员工","role":0,"userId":10004541},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453157","isCustomer":0,"isPause":0,"nickname":"路路通","operationType":0,"position":"员工","role":0,"userId":10004476},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453158","isCustomer":0,"isPause":0,"nickname":"小敏","operationType":0,"position":"员工","role":0,"userId":10008945},{"chatNum":0,"companyId":"5a98d24e4adfdc63ca4530c1","departmentId":"5a98d24e4adfdc63ca4530c3","id":"5a98fab74adfdc63ca453159","isCustomer":0,"isPause":0,"nickname":"10000","operationType":0,"position":"员工","role":0,"userId":10000}]
         * id : 5a98d24e4adfdc63ca4530c3
         * parentId : 5a98d24e4adfdc63ca4530c2
         * type : 0
         */

        private String companyId;
        private int createTime;
        private int createUserId;
        private String departName;
        private int empNum;
        private String id;
        private String parentId;
        private String roomJid;
        private String roomId;
        private int type;
        private boolean selector;
        private List<EmployeesBean> employees;

        public String getRoomJid() {
            return roomJid;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public void setRoomJid(String roomJid) {
            this.roomJid = roomJid;
        }

        public boolean isSelector() {
            return selector;
        }

        public void setSelector(boolean selector) {
            this.selector = selector;
        }

        public String getCompanyId() {
            return companyId;
        }

        public void setCompanyId(String companyId) {
            this.companyId = companyId;
        }

        public int getCreateTime() {
            return createTime;
        }

        public void setCreateTime(int createTime) {
            this.createTime = createTime;
        }

        public int getCreateUserId() {
            return createUserId;
        }

        public void setCreateUserId(int createUserId) {
            this.createUserId = createUserId;
        }

        public String getDepartName() {
            return departName;
        }

        public void setDepartName(String departName) {
            this.departName = departName;
        }

        public int getEmpNum() {
            return empNum;
        }

        public void setEmpNum(int empNum) {
            this.empNum = empNum;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public List<EmployeesBean> getEmployees() {
            return employees;
        }

        public void setEmployees(List<EmployeesBean> employees) {
            this.employees = employees;
        }

        public static class EmployeesBean implements Serializable {
            /**
             * chatNum : 0//当前会话的人数
             * companyId : 5a98d24e4adfdc63ca4530c1
             * departmentId : 5a98d24e4adfdc63ca4530c3
             * id : 5a98d24e4adfdc63ca4530c6
             * isCustomer : 0  是否为客服    0:不是  1:是
             * isPause : 0  0：暂停,1:正常
             * nickname : 曾琪
             * operationType : 0  1.建立会话操作 2.结束回话操作
             * position : 员工  职位（头衔），如：经理、总监等
             * role : 3 员工角色：0：普通员工     1：部门管理者    2：管理员    3：公司创建者(超管)
             * userId : 10008297
             */

            private int chatNum;
            private String companyId;
            private String departmentId;
            private String id;
            private int isCustomer;
            private int isPause;
            private String nickname;
            private int operationType;
            private String position;
            private int role;
            private int userId;

            public int getChatNum() {
                return chatNum;
            }

            public void setChatNum(int chatNum) {
                this.chatNum = chatNum;
            }

            public String getCompanyId() {
                return companyId;
            }

            public void setCompanyId(String companyId) {
                this.companyId = companyId;
            }

            public String getDepartmentId() {
                return departmentId;
            }

            public void setDepartmentId(String departmentId) {
                this.departmentId = departmentId;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public int getIsCustomer() {
                return isCustomer;
            }

            public void setIsCustomer(int isCustomer) {
                this.isCustomer = isCustomer;
            }

            public int getIsPause() {
                return isPause;
            }

            public void setIsPause(int isPause) {
                this.isPause = isPause;
            }

            public String getNickname() {
                return nickname;
            }

            public void setNickname(String nickname) {
                this.nickname = nickname;
            }

            public int getOperationType() {
                return operationType;
            }

            public void setOperationType(int operationType) {
                this.operationType = operationType;
            }

            public String getPosition() {
                return position;
            }

            public void setPosition(String position) {
                this.position = position;
            }

            public int getRole() {
                return role;
            }

            public void setRole(int role) {
                this.role = role;
            }

            public int getUserId() {
                return userId;
            }

            public void setUserId(int userId) {
                this.userId = userId;
            }
        }
    }
}
