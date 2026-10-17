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

package eu.akoos.photos.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eu.akoos.photos.R
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.presentation.DefaultUserCheck
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import javax.inject.Singleton

/** ProtonCore's standard account checks plus the scopes required by Drive API calls. */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun providePostLoginUserCheck(
        @ApplicationContext context: Context,
        accountManager: AccountManager,
        userManager: UserManager,
        sessionManager: SessionManager,
    ): PostLoginAccountSetup.UserCheck = object : DefaultUserCheck(context, accountManager, userManager) {
        override suspend fun invoke(user: User): PostLoginAccountSetup.UserCheckResult {
            val hasDriveScopes = sessionManager.getSessionId(user.userId)
                ?.let { sessionManager.getSession(it)?.scopes?.containsAll(DRIVE_SCOPES) }
                ?: false
            return if (hasDriveScopes) {
                super.invoke(user)
            } else {
                PostLoginAccountSetup.UserCheckResult.Error(
                    localizedMessage = context.getString(R.string.auth_drive_scope_required),
                )
            }
        }
    }

    private val DRIVE_SCOPES = listOf("full", "drive")
}
