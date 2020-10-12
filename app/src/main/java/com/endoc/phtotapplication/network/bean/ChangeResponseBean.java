package com.endoc.phtotapplication.network.bean;

import java.util.List;

public class ChangeResponseBean {

        private String code;
        private String message;
        private List<IdList> idList;
        public void setCode(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        public String getMessage() {
            return message;
        }

        public void setIdList(List<IdList> idList) {
            this.idList = idList;
        }
        public List<IdList> getIdList() {
            return idList;
        }

    @Override
    public String toString() {
        return "ChangeResponseBean{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", idList=" + idList +
                '}';
    }
}
