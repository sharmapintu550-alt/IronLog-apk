package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.auth.AuthUserState
import com.example.data.sync.CloudSyncManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = AuthManager.getInstance(application)
    private val syncManager = CloudSyncManager.getInstance(application)

    val authState: StateFlow<AuthUserState> = authManager.authState

    val hasCompletedOnboarding: StateFlow<Boolean> = authState
        .map { it.hasCompletedOnboarding }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authManager.hasCompletedOnboarding)

    val isLoading: StateFlow<Boolean> = authState
        .map { it.isLoading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val authErrorMessage = mutableStateOf<String?>(null)

    val errorMessage: StateFlow<String?> = authState
        .map { it.errorMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun signInWithGoogle(activity: Activity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            authErrorMessage.value = null
            try {
                Log.i("IronLogAuth", "AuthViewModel: Initiating Google Sign-In with Credential Manager...")
                val result = authManager.signInWithGoogle(activity)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    Log.i("IronLogAuth", "AuthViewModel: Sign-in successful for UID: ${user?.uid}")
                    if (user != null) {
                        // Trigger sync to migrate local guest data to Firestore under users/{firebaseUid}/
                        syncManager.migrateGuestDataToCloud(user.uid)
                    }
                    onSuccess()
                } else {
                    val ex = result.exceptionOrNull()
                    if (ex != null) {
                        throw ex
                    }
                }
            } catch (e: Exception) {
                authErrorMessage.value = "${e.javaClass.simpleName}: ${e.message}"
                Log.e("IronLogAuth", "Sign-in exception details: ", e)
            }
        }
    }

    fun continueAsGuest(onSuccess: () -> Unit = {}) {
        Log.i("IronLogAuth", "AuthViewModel: Continuing as guest (local Room DB).")
        authErrorMessage.value = null
        authManager.continueAsGuest()
        onSuccess()
    }

    fun clearError() {
        authErrorMessage.value = null
        authManager.clearError()
    }
}
