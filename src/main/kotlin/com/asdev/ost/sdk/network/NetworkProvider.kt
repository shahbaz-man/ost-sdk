/*
 *  NOTICE FOR FILE:
 *  NetworkProvider.kt
 *  
 *  Copyright (c) 2015-2018. Created by and property of Asdev Software Development, on 17/05/18 7:07 PM. All rights reserved.
 *  Unauthorized copying via any medium is strictly prohibited.
 *
 *  Authored by Shahbaz Momi <shahbaz@asdev.ca> as part of
 *  edu-backend under the module ost-com.asdev.ost.sdk
 */

package com.asdev.ost.sdk.network

interface NetworkProvider {

    /**
     * Low-level method that sends a POST request with the given parameters, and returns
     * the response as a string.
     */
    fun doPost(url: String, body: String, contentType: String): String

    /**
     * Low-level method that sends a GET request with the given parameters, and returns
     * the response as a string.
     */
    fun doGet(url: String): String

}