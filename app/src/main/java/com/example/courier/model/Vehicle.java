package com.example.courier.model;

public class Vehicle {
    private String vname;
    private String vno;
    private String date;
    private String exdate;
    private String imgUrl;


    public Vehicle(String vname, String vno, String date, String exdate, String imgUrl) {
        this.vname = vname;
        this.vno = vno;
        this.date = date;
        this.exdate = exdate;
        this.imgUrl = imgUrl;

    }

    public Vehicle() {
    }

    public String getVname() {
        return vname;
    }

    public void setVname(String vname) {
        this.vname = vname;
    }

    public String getVno() {
        return vno;
    }

    public void setVno(String vno) {
        this.vno = vno;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExdate() {
        return exdate;
    }

    public void setExdate(String exdate) {
        this.exdate = exdate;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }


}
