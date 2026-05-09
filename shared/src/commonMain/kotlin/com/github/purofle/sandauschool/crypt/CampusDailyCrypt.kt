package com.github.purofle.sandauschool.crypt

import com.github.purofle.sandauschool.data.DynamicSecretKeyRequest
import com.github.purofle.sandauschool.data.SALT
import com.github.purofle.sandauschool.data.ServiceSecret
import com.github.purofle.sandauschool.network.CpDailyNetworkRequest.ktorfit
import com.github.purofle.sandauschool.network.api.createCampusDailyAPI
import com.github.purofle.sandauschool.utils.StringUtils.toBase64
import io.ktor.utils.io.core.toByteArray
import kotlin.io.encoding.Base64
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object CampusDailyCrypt {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun getDynamicKeyFromRemote(
        publicKey: ByteArray,
        privateKey: ByteArray
    ): ServiceSecret {
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

    /**
     * Obfuscates the provided secret by interleaving its characters with a predefined password.
     * The transformation separates characters at **even** indices from those at **odd** indices and concatenates them.
     *
     * @param secret The input secret string to be processed.
     * @return The transformed secret string.
     */
    fun getCampushoySecret(secret: String): String {
        val inputString = LOCAL_DIS_PASSWORD + secret
        val result = StringBuilder()

        for (i in 0..1) {
            inputString.forEachIndexed { index, ch ->
                if (index % 2 == i) {
                    result.append(ch)
                }
            }
        }

        return result.toString()
    }
}