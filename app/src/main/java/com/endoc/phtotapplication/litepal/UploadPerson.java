package com.endoc.phtotapplication.litepal;

import org.litepal.crud.LitePalSupport;

/**
 * 识别成功上传的图片
 */
public class UploadPerson extends LitePalSupport {
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
