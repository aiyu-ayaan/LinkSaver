package com.atech.backup.login

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.atech.backup.utils.KEY_BACK_UP_FILE_ID
import com.atech.backup.utils.KEY_BACK_UP_FOLDER_ID
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class LogInRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    val googleSignInClient: GoogleSignInClient,
    private val pref: SharedPreferences
) {

    fun signInWithCredential(credential: AuthCredential, result: (Exception?) -> Unit = {}) {
        Log.d("AAA", "signInWithCredential: called")
        auth.signInWithCredential(credential).addOnSuccessListener {
            val currentUser = auth.currentUser?.let { currentUser ->
                UserModelModel(
                    currentUser.uid,
                    currentUser.email ?: "No Email",
                    currentUser.displayName ?: "No Name",
                    currentUser.photoUrl.toString(),
                )
            }
            fireStore.collection("users").document(it.user?.uid!!).set(currentUser!!)
                .addOnSuccessListener {
                    result.invoke(null)
                }.addOnFailureListener(result::invoke)
        }.addOnFailureListener(result::invoke)
    }


    fun getSignInAccount(intent: Intent?) = GoogleSignIn.getSignedInAccountFromIntent(intent)

    fun getCredentials(token: String) = GoogleAuthProvider.getCredential(token, null)


    fun isSignedIn() = auth.currentUser != null


    fun getCurrentUser() = auth.currentUser?.let { currentUser ->
        UserModelModel(
            currentUser.uid,
            currentUser.email ?: "No Email",
            currentUser.displayName ?: "No Name",
            currentUser.photoUrl.toString(),
        )
    }

    fun updateFolderId(path: String, onComplete: (Exception?) -> Unit = {}) {
        val currentUser = auth.currentUser
        currentUser?.let {
            fireStore.collection("users").document(it.uid).update("backUpFolderId", path)
                .addOnSuccessListener {
                    onComplete.invoke(null)
                }.addOnFailureListener(onComplete::invoke)
        }
    }

    fun updateFileId(fileId: String, onComplete: (Exception?) -> Unit = {}) {
        val currentUser = auth.currentUser
        currentUser?.let {
            fireStore.collection("users").document(it.uid).update("backUpFileId", fileId)
                .addOnSuccessListener {
                    onComplete.invoke(null)
                }.addOnFailureListener(onComplete::invoke)
        }
    }

    fun logOut(customAction: () -> Unit = {}) {
        auth.signOut()
        googleSignInClient.signOut()
        pref.edit().apply {
            putString(KEY_BACK_UP_FOLDER_ID, null)
            putString(KEY_BACK_UP_FILE_ID, null)
        }.apply()
        customAction.invoke()
    }

}