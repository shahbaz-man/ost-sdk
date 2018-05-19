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

import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import java.io.ByteArrayOutputStream

object ApacheNetworkProvider: NetworkProvider {

    private val client = HttpClientBuilder.create().build()

    override fun doPost(url: String, body: String, contentType: String): String {
        val request = HttpPost(url)
        // set the request entity
        request.entity = StringEntity(body, ContentType.getByMimeType(contentType))
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