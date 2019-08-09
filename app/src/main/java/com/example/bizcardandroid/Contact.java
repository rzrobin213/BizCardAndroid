package com.example.bizcardandroid;

public class Contact {
    private String name, company, imgURL;
    private int order, id;

    Contact(String name, String company, String imgURL, int order, int id) {
        this.name = name;
        this.company = company;
        this.imgURL = imgURL;
        this.order = order;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public String getImgURL() {
        return imgURL;
    }

    public int getOrder() { return order; }

    public int getId() {
        return id;
    }
}
