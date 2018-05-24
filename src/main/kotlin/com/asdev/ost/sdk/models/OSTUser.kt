/*
 *  NOTICE FOR FILE:
 *  OSTUser.kt
 *
 *  Copyright (c) 2015-2018. Created by and property of Asdev Software Development, on 17/05/18 7:09 PM. All rights reserved.
 *  Unauthorized copying via any medium is strictly prohibited.
 *
 *  Authored by Shahbaz Momi <shahbaz@asdev.ca> as part of
 *  edu-backend under the module ost-sdk
 */

package com.asdev.ost.sdk.models

import com.google.gson.JsonArray
import com.google.gson.JsonObject

data class OSTAddress(val chainId: Long, val address: String) {

    companion object {

        fun fromJsonArray(array: JsonArray): OSTAddress {
            val subarray = array[0].asJsonArray
            return OSTAddress(subarray[0].asString.toLong(), subarray[1].asString)
        }

    }

}

data class OSTUser(
        var id: String?,
        var address: OSTAddress?,
        var name: String?,
        var airdropped_tokens: Long,
        var token_balance: Long) {

    companion object {

        fun fromJsonObject(json: JsonObject): OSTUser {
            return OSTUser(
                    id = json["id"].asString,
                    address = OSTAddress.fromJsonArray(json["addresses"].asJsonArray),
                    name = if(json["name"].isJsonNull) null else json["name"].asString,
                    airdropped_tokens = json["airdropped_tokens"].asString.toLong(),
                    token_balance = json["token_balance"].asString.toLong()
            )
        }

    }

}