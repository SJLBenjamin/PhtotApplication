package com.endoc.phtotapplication.litepal;

import org.litepal.crud.LitePalSupport;

public class Person  extends LitePalSupport {
    private String personID;//人员id
    private String name;//人员名字
    private String retime;//时间
    private String repic;//图片路径
    private String membertype;//人员类型
    public String getMembertype() {
        return membertype;
    }

    public void setMembertype(String membertype) {
        this.membertype = membertype;
    }


    public String getPersonID() {
        return personID;
    }

    public void setPersonID(String personID) {
        this.personID = personID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRetime() {
        return retime;
    }

    public void setRetime(String retime) {
        this.retime = retime;
    }

    public String getRepic() {
        return repic;
    }

    public void setRepic(String repic) {
        this.repic = repic;
    }
}
