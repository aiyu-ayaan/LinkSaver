package com.atech.backup.login

import android.content.Intent
import android.util.Log
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
    val googleSignInClient: GoogleSignInClient
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

    fun updateFolderPath(path: String, onComplete: (Exception?) -> Unit = {}) {
        val currentUser = auth.currentUser
        currentUser?.let {
            fireStore.collection("users").document(it.uid).update("backUpFolderPath", path)
                .addOnSuccessListener {
                    onComplete.invoke(null)
                }.addOnFailureListener(onComplete::invoke)
        }
    }

    fun logOut(customAction: () -> Unit = {}) {
        auth.signOut()
        googleSignInClient.signOut()
        customAction.invoke()
    }

}