package com.example.webservicesapp;

public class Weather {

    private String city;
    private double degree;
    private String wind;
    private String minTemp;
    private String maxTemp;

    public Weather(String city, double degree, String wind, String minTemp, String maxTemp) {
        this.city = city;
        this.degree = degree;
        this.wind = wind;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }
    Weather(){}

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getDegree() {
        return degree;
    }

    public void setDegree(double degree) {
        this.degree = degree;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(String minTemp) {
        this.minTemp = minTemp;
    }

    public String getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(String maxTemp) {
        this.maxTemp = maxTemp;
    }
}