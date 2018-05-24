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

import com.asdev.ost.sdk.models.OSTAction
import com.asdev.ost.sdk.models.OSTTransaction
import com.asdev.ost.sdk.models.OSTUser
import com.asdev.ost.sdk.network.NetworkProvider
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import org.apache.commons.codec.binary.Hex
import java.io.StringReader
import java.net.URLEncoder
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal const val BASE_API_URL = "https://sandboxapi.ost.com/v1"

const val ORDER_BY_TIME = "created"
const val ORDER_BY_NAME = "name"

const val ORDER_ASC = "asc"
const val ORDER_DESC = "desc"

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
    private fun signString(input: String): String {
        val bytes = mac.doFinal(input.toByteArray(Charsets.UTF_8))
        return Hex.encodeHexString(bytes)
    }

    /**
     * Performs a POST request with the given parameters, automatically applying
     * the required OST parameters, including the API key, request timestamp,
     * and signature.
     */
    private fun doPost(urlPostfix: String, params: MutableMap<String, String>): JsonObject {
        // add in the required params
        params.apply {
            put("api_key", apiKey!!)
            put("request_timestamp", Instant.now().epochSecond.toString())
            // add the signature after it is calculated
        }

        val paramsSorted = params.toSortedMap()
        val parameterString = paramsSorted.entries.joinToString (separator = "&")

        val signing = "$urlPostfix?$parameterString"

        val signed = signString(signing)

        val body = paramsSorted.toMutableMap()
        body["signature"] = signed

        // post directly with the parameters
        val postURL = BASE_API_URL + urlPostfix

        // send the actual post
        val responseString = networkProvider!!.doPost(postURL, body)
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
     * Performs a GET request with the given parameters, automatically applying
     * the required OST parameters, including the API key, request timestmap,
     * and signature.
     */
    private fun doGet(urlPostfix: String, params: MutableMap<String, String>): JsonObject {
        // add in the required params
        params.apply {
            put("api_key", apiKey!!)
            put("request_timestamp", Instant.now().epochSecond.toString())
            // add the signature after it is calculated
        }

        val paramsSorted = params.toSortedMap()
        var parameterString = paramsSorted.entries.joinToString (separator = "&", prefix = "?") { "${it.key}=${it.value}" } // todo: postfix with a &
        val signingString = "$urlPostfix$parameterString"
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
     * Class pertaining specifically to any User API actions.
     */
    object Users {

        /**
         * Creates a user on the OST backend with the specified name.
         */
        fun create(nameIn: String?): OSTUser {
            ensureReady()

            // add in the user specific params
            val params = mutableMapOf<String, String>()

            nameIn?.let {
                params.put("name", URLEncoder.encode(nameIn, "UTF8"))
            }

            val urlPostfix = "/users"

            // will autocatch unsuccessful responses
            val json = doPost(urlPostfix, params)
            // data will contain a 'user' obj
            val userJson = json["data"].asJsonObject["user"].asJsonObject

            // create and return an OSTUser object
            return OSTUser.fromJsonObject(userJson)
        }

        fun update(toUpdate: OSTUser) {
            ensureReady()

        }

        /**
         * Retrieves the OSTUser with the given id.
         */
        fun get(id: String): OSTUser {
            ensureReady()

            // the postfix contains the /users/ + the id of the user to retrieve
            val urlPostfix = "/users/$id"
            val response = doGet(urlPostfix, mutableMapOf())

            // get user data part
            val userJson = response.getAsJsonObject("data").getAsJsonObject("user")
            return OSTUser.fromJsonObject(userJson)
        }

        /**
         * Returns a list of all OST users, with the supplied parameters.
         */
        fun list(page_no: Int = 1, limit: Int = 10, order_by: String = ORDER_BY_TIME, order: String = ORDER_DESC): List<OSTUser> {
            ensureReady()

            val urlPostfix = "/users/"
            val params = mutableMapOf<String, String>()

            params.apply {
                put("page_no", page_no.toString())
                put("limit", limit.toString())
                put("order_by", order_by)
                put("order", order)
            }

            val json = doGet(urlPostfix, params)

            val users = mutableListOf<OSTUser>()

            // get the users json array
            val usersJson = json.getAsJsonObject("data").getAsJsonArray("users")
            for(userRaw in usersJson) {
                val userJson = userRaw.asJsonObject
                val user = OSTUser.fromJsonObject(userJson)
                users.add(user)
            }

            return users
        }

    }

    /**
     * Class pertaining specifically to any Transaction API actions.
     */
    object Transactions {

        fun get(transaction_id: String): OSTTransaction {
            ensureReady()
            val urlPostfix = "/transactions/$transaction_id"
            val response = doGet(urlPostfix, mutableMapOf())

            // get the data part as an OST Action
            val json = response.getAsJsonObject("data").getAsJsonObject("transaction")
            return OSTTransaction.fromJson(json)
        }

        fun list(page_no: Int = 1, order: String = ORDER_DESC, limit: Int = 10): List<OSTTransaction> {
            ensureReady()

            val urlPostfix = "/transactions/"
            val params = mutableMapOf<String, String>()

            params.apply {
                put("page_no", page_no.toString())
                put("limit", limit.toString())
                put("order", order)
            }

            val json = doGet(urlPostfix, params)

            val transactions = mutableListOf<OSTTransaction>()

            val transactionsJson = json.getAsJsonObject("data").getAsJsonArray("transactions")
            for(transactionRaw in transactionsJson) {
                val transactionJson = transactionRaw.asJsonObject
                val transaction = OSTTransaction.fromJson(transactionJson)
                transactions.add(transaction)
            }

            return transactions
        }

    }

    /**
     * Class pertaining specifically to any Actions API actions.
     */
    object Actions {

        fun get(action_id: String): OSTAction {
            ensureReady()
            val urlPostfix = "/actions/$action_id"

            val response = doGet(urlPostfix, mutableMapOf())
            val json = response.getAsJsonObject("data").getAsJsonObject("action")
            return OSTAction.fromJson(json)
        }

        fun execute(from_id: String, to_id: String, action_id: String, amount: Float? = null, commission_percent: Float? = null): OSTTransaction {
            ensureReady()
            val urlPostfix = "/transactions"

            val params = mutableMapOf<String, String>()
            params["from_user_id"] = from_id
            params["to_user_id"] = to_id
            params["action_id"] = action_id
            // apply opts
            amount?.let { params["amount"] = it.toString() }
            commission_percent?.let { params["commission_percent"] = it.toString() }

            val response = doPost(urlPostfix, params)
            // response is a transaction
            val json = response.getAsJsonObject("data").getAsJsonObject("transaction")
            return OSTTransaction.fromJson(json)
        }

        fun list(page_no: Int = 1, limit: Int = 10, order_by: String = ORDER_BY_TIME, order: String = ORDER_DESC): List<OSTAction> {
            ensureReady()

            val urlPostfix = "/actions/"
            val params = mutableMapOf<String, String>()

            params.apply {
                put("page_no", page_no.toString())
                put("limit", limit.toString())
                put("order_by", order_by)
                put("order", order)
            }

            val json = doGet(urlPostfix, params)

            val actions = mutableListOf<OSTAction>()

            val actionsJson = json.getAsJsonObject("data").getAsJsonArray("actions")
            for(actionRaw in actionsJson) {
                val actionJson = actionRaw.asJsonObject
                val action = OSTAction.fromJson(actionJson)
                actions.add(action)
            }

            return actions
        }
    }

}