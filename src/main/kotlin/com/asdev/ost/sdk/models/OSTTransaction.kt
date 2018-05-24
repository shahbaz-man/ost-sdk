/*
 *  NOTICE FOR FILE:
 *  OSTTransaction.kton.kt
 *
 *  Copyright (c) 2015-2018. Created by and property of Asdev Software Development, on 22/05/18 11:22 PM. All rights reserved.
 *  Unauthorized copying via any medium is strictly prohibited.
 *
 *  Authored by Shahbaz Momi <shahbaz@asdev.ca> as part of
 *  edu-backend under the module IdeaProjects-ost-sdk_main
 */

package com.asdev.ost.sdk.models

import com.google.gson.JsonObject

data class OSTTransaction(
        var id: String,
        var from_user_id: String,
        var to_user_id: String,
        var transaction_hash: String?,
        var action_id: String,
        var timestamp: Long,
        var status: String,
        var gas_price: Long,
        var gas_used: Long?,
        var transaction_fee: Float?,
        var block_number: Long?,
        var amount: Float?,
        var commission_amount: Float?) {


    companion object {

        fun fromJson(j: JsonObject): OSTTransaction {
            return OSTTransaction(
                    id = j["id"].asString,
                    from_user_id = j["from_user_id"].asString,
                    to_user_id = j["to_user_id"].asString,
                    transaction_hash = if(j["transaction_hash"].isJsonNull) null else j["transaction_hash"].asString,
                    action_id = j["action_id"].asString,
                    timestamp = j["timestamp"].asLong,
                    status = j["status"].asString,
                    gas_price = j["gas_price"].asString.toLong(),
                    gas_used = if(j["gas_used"].isJsonNull) null else j["gas_used"].asLong,
                    transaction_fee = if(j["transaction_fee"].isJsonNull) null else j["transaction_fee"].asString.toFloat(),
                    block_number = if(j["block_number"].isJsonNull) null else j["block_number"].asString.toLong(),
                    amount = if(j["amount"].isJsonNull) null else j["amount"].asString.toFloat(),
                    commission_amount = if(j["commission_amount"].isJsonNull) null else j["commission_amount"].asString.toFloat()
            )
        }

    }

}