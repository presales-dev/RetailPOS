package com.dfit.dfpos;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import okhttp3.MultipartBody;

public class LoginResponse {
    @SerializedName("serialNumber")
    public String Serial;
    @SerializedName("mobileNumber")
    public String Phone;
    @SerializedName("countryCode")
    public String Country;
    @SerializedName("succeed")
    public String Status;
    @SerializedName("message")
    public String Message;
    @SerializedName("timestamp")
    public String Timestamp;
    @SerializedName("ResponseCode")
    public String ResponseCode;
    @SerializedName("data")
    public String data;

    public MultipartBody.Part form_data;

    public LoginResponse(String serialIn, String phoneIn, String countryIn) {
        this.Serial = serialIn;
        this.Phone = phoneIn;
        this.Country = countryIn;

        //we form the JSON string here and later used by the API Interface
        this.data = "{\"serialNumber\":\""+serialIn+"\",\"mobileNumber\":\"463256448464646\",\"countryCode\":\"ID\"}";
        Log.e("retailPOS",this.data);
        //this.form_data = MultipartBody.Part.createFormData("data",this.data);
    }

    public String getResponseCode() {
        return ResponseCode;
    }

    public String getSerial() {
        return Serial;
    }

    public String getPhone() {
        return Phone;
    }

    public String getCountry() {
        return Country;
    }

    public String getStatus() {
        return Status;
    }

    public String getMessage() {
        return Message;
    }

    public String getTimestamp(){
        return Timestamp;
    }

}