/*
 *  NOTICE FOR FILE:
 *  ApacheNetworkProvider.kt
 *
 *  Copyright (c) 2015-2018. Created by and property of Asdev Software Development, on 17/05/18 7:09 PM. All rights reserved.
 *  Unauthorized copying via any medium is strictly prohibited.
 *
 *  Authored by Shahbaz Momi <shahbaz@asdev.ca> as part of
 *  edu-backend under the module ost-sdk
 */

package com.asdev.ost.sdk.network

import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.core5.http.NameValuePair
import org.apache.hc.core5.http.message.BasicNameValuePair
import java.io.ByteArrayOutputStream

object ApacheNetworkProvider: NetworkProvider {

    private val client = HttpClientBuilder.create().disableAutomaticRetries().build()

    override fun doPost(url: String, params: Map<String, String>): String {
        val request = HttpPost(url)

        val pairs = mutableListOf<NameValuePair>()
        for(item in params) {
            pairs.add(BasicNameValuePair(item.key, item.value))
        }

        val entity = UrlEncodedFormEntity(pairs, Charsets.UTF_8)

        request.entity = entity

        // execute
        val response = client.execute(request)
        // read the response as a string of UTF8
        val bos = ByteArrayOutputStream()
        response.entity.writeTo(bos)
        // close the response
        response.close()
        val output = String(bos.toByteArray(), Charsets.UTF_8)
        bos.close()
        return output
    }

    override fun doGet(url: String): String {
        val request = HttpGet(url)
        // execute
        val response = client.execute(request)
        // read the response as a string of UTF8
        val bos = ByteArrayOutputStream()
        response.entity.writeTo(bos)
        // close the response
        response.close()
        val output = String(bos.toByteArray(), Charsets.UTF_8)
        bos.close()
        return output
    }

}