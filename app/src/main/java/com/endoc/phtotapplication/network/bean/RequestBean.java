package com.endoc.phtotapplication.network.bean;

public class RequestBean {


    /**
     * authCode : 192.168.200.100
     * reqType : Return
     * personId :
     * Returnid : {"personid":"155","change":"Add"}
     */

    private String authCode;
    private String reqType;
    private String personId;
    private ReturnidBean Returnid;

    public RequestBean(String authCode, String reqType) {
        this.authCode = authCode;
        this.reqType = reqType;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public ReturnidBean getReturnid() {
        return Returnid;
    }

    public void setReturnid(ReturnidBean Returnid) {
        this.Returnid = Returnid;
    }

    public static class ReturnidBean {
        /**
         * personid : 155
         * change : Add
         */

        private String personid;
        private String change;

        public String getPersonid() {
            return personid;
        }

        public void setPersonid(String personid) {
            this.personid = personid;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }
    }
}
