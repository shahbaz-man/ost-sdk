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

data class OSTAddress(val chainId: Long, val address: String) {

    companion object {

        fun fromJsonArray(array: JsonArray): OSTAddress {
            return OSTAddress(array[0].asLong, array[1].asString)
        }

    }

}

data class OSTUser(
        var id: String?,
        var address: OSTAddress?,
        var name: String,
        var airdropped_tokens: Long,
        var token_balance: Long)