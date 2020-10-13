package com.endoc.phtotapplication.network.bean;

public class VerifyRequestBean {

    public VerifyRequestBean(String person, String device, String retime, String repic) {
        this.person = person;
        this.device = device;
        this.retime = retime;
        this.repic = repic;
    }

    /**
     * person :
     * device :
     * retime :
     * repic :
     */

    private String person;
    private String device;
    private String retime;
    private String repic;

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
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
