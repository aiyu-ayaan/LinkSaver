package com.atech.backup.module

import android.content.Context
import com.atech.backup.utils.BACK_UP_FOLDER_NAME
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.Scopes.DRIVE_FILE
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DriveModule {

    @Provides
    @Singleton
    fun provideDriveService(@ApplicationContext context: Context): Drive? {
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        googleSignInAccount?.let { account ->
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DRIVE_FILE)
            )
            credential.selectedAccount = account.account
            return Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName(BACK_UP_FOLDER_NAME)
                .build()
        }
        return null
    }
}