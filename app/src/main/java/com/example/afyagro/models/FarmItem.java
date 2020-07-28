package com.example.afyagro.models;

public class FarmItem {
    private String type;
    private String name;
    private String description;
    private String publisherName;
    private String publisherId;
    private String photoPath;
    private String price;

    public FarmItem(){}

    public FarmItem(String type, String name, String description, String publisherName, String publisherId, String photoPath, String price) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.publisherName = publisherName;
        this.publisherId = publisherId;
        this.photoPath = photoPath;
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
