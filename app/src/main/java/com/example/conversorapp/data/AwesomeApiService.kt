package com.example.conversorapp.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface AwesomeApiService {
    @GET("json/last/{moedas}")
    fun getCotacao(@Path("moedas") moedas: String): Call<Map<String, CotacaoResponse>>
}