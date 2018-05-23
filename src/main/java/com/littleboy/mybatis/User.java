package com.littleboy.mybatis;

public class User {
    Long id ;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "i = "+id;
    }
}
