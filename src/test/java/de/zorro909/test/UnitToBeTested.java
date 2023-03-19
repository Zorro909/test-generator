package de.zorro909.test;

public class UnitToBeTested {

    public String ping(){
        return "Hallo";
    }

    public String ping(String content){
        if(content == null){
            throw new IllegalArgumentException();
        }

        return ping() + " " + content;
    }

}
