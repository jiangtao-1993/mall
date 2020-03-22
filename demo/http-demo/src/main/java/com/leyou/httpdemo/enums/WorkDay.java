package com.leyou.httpdemo.enums;


public enum WorkDay {



    SUNDAY(10,"刘德华"),
    MONDAY(20,"黑马"),
    FRIDAY(30,"20K");

    int age;
    String name;

    WorkDay(int age,String name){
        this.age=age;
        this.name = name;
    }


    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
