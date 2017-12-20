package com.rydeit.model.ola.requests;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import java.util.List;

/**
 * Created by Aditya.Khambampati on 12/27/2015.
 */
public class OErrorResponse {
    /**
     "status": "FAILURE",
     "code": "NO_CABS_AVAILABLE",
     "message": "Sorry, we're working real hard to add more cars. Please try us again soon."
     **/

    @Expose
    @SerializedName("status")
    private String estatus;

    @Expose
    @SerializedName("code")
    private String ecode;
    @Expose
    @SerializedName("message")
    private String emesage;


    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getEcode() {
        return ecode;
    }

    public void setEcode(String ecode) {
        this.ecode = ecode;
    }

    public String getEmesage() {
        return emesage;
    }

    public void setEmesage(String emesage) {
        this.emesage = emesage;
    }






}
