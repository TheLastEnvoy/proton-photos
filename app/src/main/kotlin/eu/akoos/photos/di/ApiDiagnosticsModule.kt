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

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import eu.akoos.photos.data.api.ApiFailureRecorder
import me.proton.core.network.domain.interceptor.InterceptorInfo
import okhttp3.Interceptor
import javax.inject.Singleton

/** Adds a privacy-limited failure recorder after ProtonCore has applied its request headers. */
@Module
@InstallIn(SingletonComponent::class)
object ApiDiagnosticsModule {

    @Provides
    @Singleton
    @IntoSet
    fun provideApiFailureRecorder(recorder: ApiFailureRecorder): Pair<InterceptorInfo, Interceptor> =
        InterceptorInfo(
            type = InterceptorInfo.Type.APP,
            priority = InterceptorInfo.Priority.LEAST,
        ) to recorder
}
