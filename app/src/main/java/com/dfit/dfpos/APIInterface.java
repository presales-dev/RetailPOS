package com.dfit.dfpos;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

interface APIInterface {
    @Headers({"Content-Type: application/x-www-form-urlencoded",
            "X-Auth-Token: e7dotfHSpEcxCEUCgWto9BInyx8fIAvc"})
    //@Multipart
    @FormUrlEncoded //we use formUrlEncoded
    @POST("serial.do?method=validateSerial")
    Call<LoginResponse> createUser(@Field("data") String data); //field keyname is data, the datatype is String
    //Call<LoginResponse> createUser(@Part MultipartBody.Part form_data);
    //Call<LoginResponse> createUser(@Body LoginResponse login);
}