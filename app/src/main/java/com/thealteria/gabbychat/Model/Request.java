package com.thealteria.gabbychat.Model;

public class Request {

    private String name, image, requestType;

    public Request() {
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestType() {
        return requestType;
    }

    public Request(String image, String requestType, String name) {
        this.image = image;
        this.requestType = requestType;
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
