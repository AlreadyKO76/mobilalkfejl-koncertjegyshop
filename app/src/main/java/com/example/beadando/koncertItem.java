package com.example.beadando;

public class koncertItem {
    private String name;
    private String price;
    private String helyszin;
    private String date;
    private final int imgResource;

    public koncertItem(String date, String helyszin, String name, String price, int imgResource) {
        this.date = date;
        this.helyszin = helyszin;
        this.name = name;
        this.price = price;
        this.imgResource = imgResource;
    }

    public String getDate() {
        return date;
    }

    public String getHelyszin() {
        return helyszin;
    }

    public int getImgResource() {
        return imgResource;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }
}
