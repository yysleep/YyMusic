package com.example.administrator.yymusic.model;

/**
 * Created by Administrator on 2017/11/9.
 */

public class LocationInfo {
    //获取详细地址信息
    private String address;
    //获取国家
    private String country;
    //获取省份
    private String province;
    //获取城市
    private String city;
    //获取区县
    private String district;
    //获取街道信息
    private String street;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}
