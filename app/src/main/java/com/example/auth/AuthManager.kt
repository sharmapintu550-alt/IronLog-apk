package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class AuthUserState(
    val user: FirebaseUser? = null,
    val isGuest: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasCompletedOnboarding: Boolean = false
)

class AuthManager(private val context: Context) {

    companion object {
        private const val TAG = "IronLogAuth"
        private const val PREFS_NAME = "ironlog_auth_prefs"
        private const val KEY_IS_GUEST = "key_is_guest"
        const val KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"

        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(
        AuthUserState(
            user = auth.currentUser,
            isGuest = auth.currentUser == null && prefs.getBoolean(KEY_IS_GUEST, true),
            isLoading = false,
            hasCompletedOnboarding = prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false) || auth.currentUser != null
        )
    )
    val authState: StateFlow<AuthUserState> = _authState.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val currentUser = firebaseAuth.currentUser
        val hasCompleted = prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false) || currentUser != null
        if (currentUser != null) {
            prefs.edit().putBoolean(KEY_IS_GUEST, false).apply()
            _authState.value = AuthUserState(
                user = currentUser,
                isGuest = false,
                isLoading = false,
                errorMessage = null,
                hasCompletedOnboarding = true
            )
        } else {
            val isGuest = prefs.getBoolean(KEY_IS_GUEST, true)
            _authState.value = AuthUserState(
                user = null,
                isGuest = isGuest,
                isLoading = false,
                errorMessage = null,
                hasCompletedOnboarding = hasCompleted
            )
        }
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isGuest: Boolean
        get() = _authState.value.isGuest

    /**
     * Executes Google One-Tap / Credential Manager authentication flow.
     */
    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> = withContext(Dispatchers.Main) {
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

        try {
            val webClientId = getWebClientId()
            val credentialManager = CredentialManager.create(activity)

            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )

            val credential = result.credential
            val idToken = when {
                credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    GoogleIdTokenCredential.createFrom(credential.data).idToken
                }
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    GoogleIdTokenCredential.createFrom(credential.data).idToken
                }
                else -> null
            }

            if (!idToken.isNullOrBlank()) {
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user

                if (user != null) {
                    prefs.edit()
                        .putBoolean(KEY_IS_GUEST, false)
                        .putBoolean(KEY_HAS_COMPLETED_ONBOARDING, true)
                        .apply()
                    _authState.value = AuthUserState(
                        user = user,
                        isGuest = false,
                        isLoading = false,
                        errorMessage = null,
                        hasCompletedOnboarding = true
                    )
                    Log.d(TAG, "Google Sign-In successful for UID: ${user.uid}")
                    Log.i("AuthSyncDebug", "Google Sign-In successful: UID=${user.uid}, email=${user.email}")
                    Result.success(user)
                } else {
                    val err = "Firebase authentication returned null user"
                    _authState.value = _authState.value.copy(isLoading = false, errorMessage = err)
                    Log.e("AuthSyncDebug", "Google Sign-In failed: $err")
                    Result.failure(IllegalStateException(err))
                }
            } else {
                val err = "Unable to retrieve valid Google ID token (credential type: ${credential.type})"
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = err)
                Log.e("AuthSyncDebug", "Google Sign-In failed: $err")
                Result.failure(IllegalStateException(err))
            }
        } catch (e: GetCredentialCancellationException) {
            // Catch GetCredentialCancellationException cleanly when dismissed by the user.
            Log.d(TAG, "Google Sign-In prompt dismissed by user: ${e.message}")
            _authState.value = _authState.value.copy(isLoading = false, errorMessage = null)
            Result.failure(e)
        } catch (e: Throwable) {
            // For all other exceptions (ApiException, SHA-1 mismatch, developer error, FirebaseAuthException),
            // do NOT label them as "cancelled". Show the exact exception message string so it's clearly visible.
            Log.e(TAG, "Google Sign-In exception [${e.javaClass.simpleName}]: ${e.message}", e)
            Log.e("AuthSyncDebug", "Google Sign-In failed [${e.javaClass.simpleName}]: ${e.message}", e)
            val exactMessage = e.message?.takeIf { it.isNotBlank() }
                ?: e.localizedMessage?.takeIf { it.isNotBlank() }
                ?: "${e.javaClass.simpleName}: Authentication failed"
            _authState.value = _authState.value.copy(isLoading = false, errorMessage = exactMessage)
            Result.failure(e)
        }
    }

    val hasCompletedOnboarding: Boolean
        get() = _authState.value.hasCompletedOnboarding

    fun setHasCompletedOnboarding(completed: Boolean = true) {
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, completed).apply()
        _authState.value = _authState.value.copy(hasCompletedOnboarding = completed)
    }

    fun continueAsGuest() {
        prefs.edit()
            .putBoolean(KEY_IS_GUEST, true)
            .putBoolean(KEY_HAS_COMPLETED_ONBOARDING, true)
            .apply()
        _authState.value = AuthUserState(
            user = null,
            isGuest = true,
            isLoading = false,
            errorMessage = null,
            hasCompletedOnboarding = true
        )
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out error", e)
        }
        try {
            val credentialManager = CredentialManager.create(context)
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear credential state", e)
        }
        val hasCompleted = prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, true)
        prefs.edit().putBoolean(KEY_IS_GUEST, true).apply()
        _authState.value = AuthUserState(
            user = null,
            isGuest = true,
            isLoading = false,
            errorMessage = null,
            hasCompletedOnboarding = hasCompleted
        )
    }

    private fun getWebClientId(): String {
        return context.getString(R.string.default_web_client_id)
    }
}
