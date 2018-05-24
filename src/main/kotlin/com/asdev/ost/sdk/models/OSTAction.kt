/*
 *  NOTICE FOR FILE:
 *  OSTAction.kt
 *
 *  Copyright (c) 2015-2018. Created by and property of Asdev Software Development, on 23/05/18 8:52 PM. All rights reserved.
 *  Unauthorized copying via any medium is strictly prohibited.
 *
 *  Authored by Shahbaz Momi <shahbaz@asdev.ca> as part of
 *  edu-backend under the module IdeaProjects-ost-sdk_main
 */

package com.asdev.ost.sdk.models

import com.asdev.ost.sdk.OSTSdk
import com.google.gson.JsonObject

data class OSTAction(
        var id: String,
        var name: String,
        var kind: String,
        var currency: String,
        var amount: Float?,
        var arbitrary_amount: Boolean,
        var commission_percent: Float?,
        var arbitrary_commission: Boolean) {

    companion object {

        fun fromJson(j: JsonObject): OSTAction {
            return OSTAction(
                    id = j["id"].asString,
                    name = j["name"].asString,
                    kind = j["kind"].asString,
                    currency = j["currency"].asString,
                    amount = if(j["amount"].isJsonNull) null else j["amount"].asString.toFloat(),
                    arbitrary_amount = if(j["arbitrary_amount"].isJsonNull) false else j["arbitrary_amount"].asBoolean,
                    commission_percent = if(j["commission_percent"].isJsonNull) null else j["commission_percent"].asString.toFloat(),
                    arbitrary_commission = if(j["arbitrary_commission"].isJsonNull) false else j["arbitrary_commission"].asBoolean
            )
        }

    }

    fun execute(from_id: String, to_id: String, amount: Float? = null, commission_percent: Float? = null): OSTTransaction {
        return OSTSdk.Actions.execute(from_id, to_id, this.id, amount, commission_percent)
    }

}