/*
 * Photos for Proton
 * Copyright (C) 2026 Akoos <https://akoos.eu>
 *
 * Source:  https://github.com/gitakoos/proton-photos
 * Website: https://www.photosforproton.eu
 *
 * This file is part of Photos for Proton.
 *
 * Photos for Proton is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package eu.akoos.photos.data.api

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Records minimal Proton API failure facts to diagnose server-side auth and routing errors. */
@Singleton
class ApiFailureRecorder @Inject constructor(
    @ApplicationContext private val context: Context,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        // Authentication routes have fixed paths; avoid logging arbitrary Drive paths that may
        // contain resource identifiers. No request/response payload or account identifier is stored.
        if (!response.isSuccessful && request.url.encodedPath.startsWith("/auth/")) {
            runCatching {
                val body = response.peekBody(MAX_PEEK_BYTES).string()
                append(
                    "${response.code} code=${CODE.find(body)?.groupValues?.get(1) ?: "-"}" +
                        " appversion=${request.header("x-pm-appversion") ?: "MISSING"}" +
                        " host=${request.url.host} ${request.method} ${request.url.encodedPath}"
                )
            }
        }
        return response
    }

    private fun append(line: String) = synchronized(FILE_LOCK) {
        val file = file(context)
        if (file.length() > MAX_FILE_BYTES) file.writeText("")
        file.appendText(line + "\n")
    }

    companion object {
        private const val FILE_NAME = "api_failures.log"
        private const val MAX_PEEK_BYTES = 8L * 1024
        private const val MAX_FILE_BYTES = 32L * 1024
        private val CODE = Regex("\"Code\"\\s*:\\s*(\\d+)")
        private val FILE_LOCK = Any()

        fun file(context: Context): File = File(context.filesDir, FILE_NAME)
    }
}
