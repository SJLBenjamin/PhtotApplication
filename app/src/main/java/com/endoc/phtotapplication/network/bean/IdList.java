package com.endoc.phtotapplication.network.bean;

public class IdList {
        private String id;
        private String change;
        public void setId(String id) {
            this.id = id;
        }
        public String getId() {
            return id;
        }

        public void setChange(String change) {
            this.change = change;
        }
        public String getChange() {
            return change;
        }

    @Override
    public String toString() {
        return "IdList{" +
                "id='" + id + '\'' +
                ", change='" + change + '\'' +
                '}';
    }
}
