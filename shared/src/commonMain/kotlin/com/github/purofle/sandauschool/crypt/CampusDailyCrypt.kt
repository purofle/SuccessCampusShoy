package com.github.purofle.sandauschool.crypt

import com.github.purofle.sandauschool.data.DynamicSecretKeyRequest
import com.github.purofle.sandauschool.data.SALT
import com.github.purofle.sandauschool.data.ServiceSecret
import com.github.purofle.sandauschool.network.CpDailyNetworkRequest.ktorfit
import com.github.purofle.sandauschool.network.api.createCampusDailyAPI
import com.github.purofle.sandauschool.res.Res
import com.github.purofle.sandauschool.utils.StringUtils.toBase64
import io.ktor.util.decodeBase64String
import io.ktor.utils.io.core.toByteArray
import kotlin.io.encoding.Base64
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object CampusDailyCrypt {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun getDynamicKeyFromRemote(publicKey: ByteArray, privateKey: ByteArray): ServiceSecret {
        val api = ktorfit.createCampusDailyAPI()
        val randomUUID = Uuid.random()

        val privateData = "${randomUUID}|first_v4"

        val request = api.getDynamicSecretKey(
            DynamicSecretKeyRequest(
                private = rsaEncrypt(
                    privateData.toByteArray(),
                    publicKey,
                ).toBase64(),
                sign = sumMD5("$privateData&${SALT}".toByteArray()).toHexString(),
            )
        )
        val parts = rsaDecrypt(Base64.decode(request.data), privateKey)
            .decodeToString()
            .split("|")

        return ServiceSecret(
            randomString = parts[0],
            cpdailySecret = parts[1],
            catSecret = parts[2],
        )
    }

    fun getCampushoySecret() {

    }
}