package com.example.ncarvalho.story2tell;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ncarvalho on 8/01/18.
 */

public class UserQualities {

    public String username;
    public String date;
    Map<String, Long> qualitiesMap = new HashMap<String, Long>();

    public UserQualities(){

    }

    public String getDate(){
        return date;
    }

    public String getUsername(){
        return username;
    }


    public UserQualities(String username,
                         String date){

        this.username = username;
        qualitiesMap.put(Constants.sshy, Constants.ishy);
        qualitiesMap.put(Constants.spolite, Constants.ipolite);
        qualitiesMap.put(Constants.stalkative, Constants.italkative);
        this.date = date;

    }

    public UserQualities(Object obj){

        HashMap map = (HashMap) ((Map.Entry) obj).getValue();

        for (Object entry : map.entrySet())
        {
            if(((Map.Entry) entry).getKey().equals("username")){
                this.username = (String) ((Map.Entry) entry).getValue();
            }
            if(((Map.Entry) entry).getKey().equals("date")){
                this.date = (String) ((Map.Entry) entry).getValue();
            }
            if(((Map.Entry) entry).getKey().equals("qualitiesMap")){

                HashMap qualities = (HashMap) ((Map.Entry) entry).getValue();

                for (Object qualityEntry : qualities.entrySet()){
                    qualitiesMap.put((String) ((Map.Entry) qualityEntry).getKey(),
                            (Long) ((Map.Entry) qualityEntry).getValue());
                }

            }

        }


    }
}
