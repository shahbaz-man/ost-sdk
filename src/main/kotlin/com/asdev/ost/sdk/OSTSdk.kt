/*
 *  NOTICE FOR FILE:
 *  OSTSdk.kt
 *
 *  Copyright (c) 2015-2018. Created by and property of Asdev Software Development, on 17/05/18 7:09 PM. All rights reserved.
 *  Unauthorized copying via any medium is strictly prohibited.
 *
 *  Authored by Shahbaz Momi <shahbaz@asdev.ca> as part of
 *  edu-backend under the module ost-sdk
 */

package com.asdev.ost.sdk

import com.asdev.ost.sdk.models.OSTAddress
import com.asdev.ost.sdk.models.OSTUser
import com.asdev.ost.sdk.network.NetworkProvider
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import org.apache.commons.codec.binary.Hex
import java.io.StringReader
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal const val BASE_API_URL = "https://sandboxapi.ost.com/v1"
internal const val CONTENT_TYPE_FORM = "application/x-www-form-urlencoded"

/**
 * The class serving as an entry point for the OSTSdk.
 */
object OSTSdk {

    private var apiKey: String? = null
    private var apiSecret: String? = null
    private var secretKey: SecretKeySpec? = null
    private var networkProvider: NetworkProvider? = null
    private val jsonParser = JsonParser()

    /**
     * Initializes this SDK with the supplied keys. Must be called before
     * any API operations are called, otherwise an initialization exception will be thrown.
     */
    fun init(apiKey: String, apiSecret: String, networkProvider: NetworkProvider) {
        this.apiKey = apiKey
        this.apiSecret = apiSecret
        this.networkProvider = networkProvider
        secretKey = SecretKeySpec(apiSecret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
    }

    /**
     * Internal method used to ensure that the keys are properly initialized and the network provider
     * is submitted.
     */
    private fun ensureReady() {
        if(apiKey == null || apiSecret == null) {
            throw ExceptionInInitializerError("Api keys were incorrectly submitted. Please call OSTSdk.init() to initialize before calling any API operations.")
        }

        if(networkProvider == null)
            throw ExceptionInInitializerError("No network provider was submitted. Please call OSTSdk.init() to initialize before calling any API operations.")
    }

    private val mac = Mac.getInstance("HmacSHA256")

    /**
     * Signs the input signature and returns the result.
     */
    fun signString(input: String): String {
        val bytes = mac.doFinal(input.toByteArray(Charsets.UTF_8))
        return Hex.encodeHexString(bytes)
    }

    private fun doPost(urlPostfix: String, params: MutableMap<String, String>): JsonObject {
        // add in the required params
        params.apply {
            put("api_key", apiKey!!)
            put("request_timestamp", "1526605283")
            // add the signature after it is calculated
        }

        val paramsSorted = params.toSortedMap()
        var parameterString = paramsSorted.entries.joinToString (separator = "&") { "${it.key}=${it.value}" } // todo: postfix with a &
        // sign with the url + parameters
        val signed = signString("$urlPostfix/?$parameterString")
        // append the signed string a signature
        parameterString += "&signature=$signed"
        // post directly with the parameters
        val postBody = parameterString // todo: encode as a url form
        val postURL = BASE_API_URL + urlPostfix

        // send the actual post
        val responseString = networkProvider!!.doPost(postURL, postBody, CONTENT_TYPE_FORM)
        val jsonObj = jsonParser.parse(JsonReader(StringReader(responseString)).apply { isLenient = true }).asJsonObject

        // check if there is an error
        if(!jsonObj["success"].asBoolean) {
            // throw a response exception
            throw Exception("OST Network response was not successful: $jsonObj")
        }

        // parse the received json object, and return it
        return jsonObj
    }

    private fun doGet(urlPostfix: String, params: MutableMap<String, String>): JsonObject {
        // add in the required params
        params.apply {
            put("api_key", apiKey!!)
            put("request_timestamp", Instant.now().epochSecond.toString())
            // add the signature after it is calculated
        }

        val paramsSorted = params.toSortedMap()
        var parameterString = paramsSorted.entries.joinToString (separator = "&") { "${it.key}=${it.value}" } // todo: postfix with a &
        val signingString = "$urlPostfix?$parameterString"
        // sign with the url + parameters
        val signed = signString(signingString)
        // append the signed string a signature
        parameterString += "&signature=$signed"
        val getURL = BASE_API_URL + urlPostfix + parameterString

        // send the actual post
        val responseString = networkProvider!!.doGet(getURL)
        val jsonObj = jsonParser.parse(JsonReader(StringReader(responseString)).apply { isLenient = true }).asJsonObject

        // check if there is an error
        if(!jsonObj["success"].asBoolean) {
            // throw a response exception
            throw Exception("OST Network response was not successful: $jsonObj")
        }

        // parse the received json object, and return it
        return jsonObj
    }

    /**
     * Class pertaining specifically to any user API actions.
     */
    object Users {

        /**
         * Creates a user on the OST backend with the specified name.
         */
        fun create(nameIn: String): OSTUser {
            ensureReady()

            // add in the user specific params
            val params = mutableMapOf("name" to nameIn)
            val urlPostfix = "/users"

            // will autocatch unsuccessful responses
            val json = doPost(urlPostfix, params)
            // data will contain a 'user' obj
            val userJson = json["data"].asJsonObject["user"].asJsonObject
            // json obj contains:
            // id
            // addresses
            // name
            // airdropped_tokens
            // token_balance
            val id = userJson["id"].asString
            val addresses = userJson["addresses"].asJsonArray
            val name = userJson["name"].asString // name == nameIn
            val airdropped_tokens = userJson["airdropped_tokens"].asLong
            val token_balance = userJson["token_balance"].asLong

            // create and return an OSTUser object
            return OSTUser(id, OSTAddress.fromJsonArray(addresses), name, airdropped_tokens, token_balance)
        }

        fun update(toUpdate: OSTUser) {
            ensureReady()

        }

        fun get(id: String) {
            ensureReady()

            // the postfix contains the /users/ + the id of the user to retrieve
            val urlPostfix = "/users/$id"
            val response = doGet(urlPostfix, mutableMapOf())
        }

        fun list(ids: Array<String>? = null) {
            ensureReady()

        }

    }

    object Transactions {

    }
}