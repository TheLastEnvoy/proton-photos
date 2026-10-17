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

package eu.akoos.photos.data.repository.drive

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.drive.sdk.CorePublicAddressResolver
import me.proton.drive.sdk.CoreUserAddressResolver
import me.proton.drive.sdk.LoggerProvider
import me.proton.drive.sdk.ProtonDriveSdk
import me.proton.drive.sdk.ProtonPhotosClient
import me.proton.drive.sdk.SdkLogger
import me.proton.drive.sdk.entity.ClientCreateRequest
import me.proton.drive.sdk.entity.LegacyNodeUid
import me.proton.drive.sdk.entity.NodeUid
import okhttp3.HttpUrl
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Adapter between this app's separate legacy IDs and the SDK's compound volume~link UID. */
internal fun sdkNodeUid(volumeId: String, linkId: String): NodeUid = LegacyNodeUid(volumeId, linkId)

/** The app's persisted link IDs are link-only; the SDK uses volume~link UIDs. */
internal fun sdkNodeLinkId(nodeUid: NodeUid): String = nodeUid.value.substringAfterLast('~')

/** The app persists only the revision component of the SDK's volume~link~revision UID. */
internal fun sdkRevisionId(revisionUid: me.proton.drive.sdk.entity.RevisionUid): String =
    revisionUid.value.substringAfterLast('~')

/** Owns native SDK clients and their coroutine scopes for each signed-in account. */
@Singleton
class ProtonDriveSdkClientManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @BaseProtonApiUrl private val baseApiUrl: HttpUrl,
    private val apiProvider: ApiProvider,
    private val cryptoContext: CryptoContext,
    private val userAddressRepository: UserAddressRepository,
    private val publicAddressRepository: PublicAddressRepository,
) {
    private data class Entry(
        val client: ProtonPhotosClient,
        val scope: CoroutineScope,
    )

    private val mutex = Mutex()
    private val clients = mutableMapOf<UserId, Entry>()
    private var loggerProvider: LoggerProvider? = null

    suspend fun photosClient(userId: UserId): ProtonPhotosClient = mutex.withLock {
        clients[userId]?.client ?: createClient(userId).also { clients[userId] = it }.client
    }

    /** Close native handles before the account's session and unlocked keys are removed. */
    suspend fun remove(userId: UserId) {
        mutex.withLock {
            clients.remove(userId)?.let { entry ->
                runCatching { entry.client.close() }
                entry.scope.cancel()
            }
        }
    }

    private suspend fun createClient(userId: UserId): Entry {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return try {
            val sdkLogger = loggerProvider ?: ProtonDriveSdk.loggerProvider(SDK_LOGGER).also {
                loggerProvider = it
            }
            val client = ProtonDriveSdk.protonPhotosClientCreate(
                coroutineScope = scope,
                userId = userId,
                apiProvider = apiProvider,
                request = ClientCreateRequest(
                    baseUrl = baseApiUrl.toString(),
                    loggerProvider = sdkLogger,
                    bindingsLanguage = "kotlin",
                    uid = getOrCreateClientUid(),
                ),
                userAddressResolver = CoreUserAddressResolver(
                    userId = userId,
                    cryptoContext = cryptoContext,
                    userAddressRepository = userAddressRepository,
                ),
                publicAddressResolver = CorePublicAddressResolver(
                    userId = userId,
                    publicAddressRepository = publicAddressRepository,
                ),
                // This app does not emit SDK telemetry or enable undocumented remote flags.
                metricCallback = null,
                featureEnabled = { false },
            )
            Entry(client, scope)
        } catch (t: Throwable) {
            scope.cancel()
            throw t
        }
    }

    private fun getOrCreateClientUid(): String = synchronized(CLIENT_UID_LOCK) {
        val prefs = context.getSharedPreferences(CLIENT_UID_PREFS, Context.MODE_PRIVATE)
        prefs.getString(CLIENT_UID_KEY, null)?.let { return@synchronized it }
        UUID.randomUUID().toString().also { uid ->
            check(prefs.edit().putString(CLIENT_UID_KEY, uid).commit()) {
                "Could not persist the Proton Drive SDK client UID"
            }
        }
    }

    private companion object {
        private const val CLIENT_UID_PREFS = "proton_drive_sdk"
        private const val CLIENT_UID_KEY = "client_uid"
        private val CLIENT_UID_LOCK = Any()
        private val SDK_LOGGER: SdkLogger = { _, _, _ -> }
    }
}
