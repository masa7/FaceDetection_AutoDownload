package com.example.facedetection.fileuploader

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MyAPI {

    @Multipart
    @POST("Api.php?apicall=upload")
    fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("desc") desc: RequestBody
    ): Call<UploadResponse>

    companion object {
        operator fun invoke(): MyAPI {
            return Retrofit.Builder()
                //.baseUrl("http://10.0.2.2/FileUploader/")
                //.baseUrl("http://xxx.xxx.x.xxx/FileUploader/")  //*check ipconfig for localhost
                //.baseUrl("http://172.20.10.4/FileUploader/")  //*check ipconfig for localhost
                .baseUrl("http://192.168.3.19/FileUploader/")  //*Sony SO-01M
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MyAPI::class.java)
        }
    }
}