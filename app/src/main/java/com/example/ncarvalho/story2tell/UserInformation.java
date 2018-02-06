package com.example.ncarvalho.story2tell;

/**
 * Created by ncarvalho on 4/01/18.
 */

public class UserInformation {
    public String username;
    public String password;


    public UserInformation(){

    }
    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }

    public UserInformation(String username, String password){
        this.username = username;
        this.password = password;
    }

    public UserInformation(String username,
                           String password,
                           Integer shy,
                           Integer talkative,
                           Integer polite
                           ){

        this.username = username;
        this.password = password;


    }


}
