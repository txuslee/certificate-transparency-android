/*
 * Copyright 2019 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.babylon.certificatetransparency.sampleapp.examples.okhttp.kotlin

import android.content.Context
import com.babylon.certificatetransparency.CTLogger
import com.babylon.certificatetransparency.cache.AndroidDiskCache
import com.babylon.certificatetransparency.certificateTransparencyInterceptor
import com.babylon.certificatetransparency.sampleapp.examples.BaseExampleViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class OkHttpKotlinExampleViewModel(val context: Context) : BaseExampleViewModel(context) {

    override val sampleCodeTemplate
        get() = "okhttp-kotlin.txt"

    // A normal client would create this ahead of time and share it between network requests
    // We create it dynamically as we allow the user to set the hosts for certificate transparency
    private fun createOkHttpClient(hosts: Set<String>, isFailOnError: Boolean, defaultLogger: CTLogger): OkHttpClient {
        // Create a network interceptor
        val networkInterceptor = certificateTransparencyInterceptor {
            hosts.forEach {
                +it
            }
            failOnError = isFailOnError
            logger = defaultLogger
            diskCache = AndroidDiskCache(getApplication())
        }

        // Set the interceptor when creating the OkHttp client
        return OkHttpClient.Builder().apply {
            addNetworkInterceptor(networkInterceptor)
        }.build()
    }

    override fun openConnection(connectionHost: String, hosts: Set<String>, isFailOnError: Boolean, defaultLogger: CTLogger) {
        val client = createOkHttpClient(hosts, isFailOnError, defaultLogger)

        val request = Request.Builder().url("https://$connectionHost").build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                // Failure. Send message to the UI as logger won't catch generic network exceptions
                sendException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                // Success. Reason will have been sent to the logger
            }
        })
    }
}
