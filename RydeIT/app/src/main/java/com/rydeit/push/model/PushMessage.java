package com.rydeit.push.model;

import com.parse.ParseObject;

/**
 * Created by Aditya.Khambampati on 10/30/2015.
 *
 * Sample push Json can be as follows
 * {
 "type": "PRM",
 "ptxt": "primary text",
 "img" : "NA",
 "expiry" : "48",
 "stxt": "secondary text"
 }


 {
 "type": "CMD",
 "ptxt": "LOGOUT",
 "img" : "NA",
 "expiry" : "NA",
 "stxt": "NA"
 }


 */
public class PushMessage  {


    public PushMessage()
    {

    }

    public PushMessage(String title, String message , long hrs, String aimg ,String kind , String alink){
        ptxt = title;
        stxt= message;
        expiry = hrs;
        img = aimg;
        type = kind;
        link = alink;


    }

    public String type; // can be command CMD or Promotion PRM
    public String  ptxt; // primary promotion text
    public String img ; // link for promo image or video
    public String link; // clicking it should take user to approprate place.
    public long expiry ;  // hrs for expiry
    public String stxt ; // secondary text
}
