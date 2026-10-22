package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
    val errorMessage: String? = null
)

class AuthManager(private val context: Context) {

    companion object {
        private const val TAG = "IronLogAuth"
        private const val PREFS_NAME = "ironlog_auth_prefs"
        private const val KEY_IS_GUEST = "key_is_guest"

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
            isLoading = false
        )
    )
    val authState: StateFlow<AuthUserState> = _authState.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            prefs.edit().putBoolean(KEY_IS_GUEST, false).apply()
            _authState.value = AuthUserState(
                user = currentUser,
                isGuest = false,
                isLoading = false,
                errorMessage = null
            )
        } else {
            val isGuest = prefs.getBoolean(KEY_IS_GUEST, true)
            _authState.value = AuthUserState(
                user = null,
                isGuest = isGuest,
                isLoading = false,
                errorMessage = null
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
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user

                if (user != null) {
                    prefs.edit().putBoolean(KEY_IS_GUEST, false).apply()
                    _authState.value = AuthUserState(
                        user = user,
                        isGuest = false,
                        isLoading = false,
                        errorMessage = null
                    )
                    Log.d(TAG, "Google Sign-In successful for UID: ${user.uid}")
                    Result.success(user)
                } else {
                    val err = "Authentication returned null user"
                    _authState.value = _authState.value.copy(isLoading = false, errorMessage = err)
                    Result.failure(Exception(err))
                }
            } else {
                val err = "Unexpected credential format received"
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = err)
                Result.failure(Exception(err))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User dismissed sign-in prompt")
            _authState.value = _authState.value.copy(isLoading = false, errorMessage = null)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            val friendlyError = when {
                e.message?.contains("network", ignoreCase = true) == true -> "Network connection error. Please try again."
                e.message?.contains("canceled", ignoreCase = true) == true -> null
                else -> e.localizedMessage ?: "Sign in failed"
            }
            _authState.value = _authState.value.copy(isLoading = false, errorMessage = friendlyError)
            Result.failure(e)
        }
    }

    fun continueAsGuest() {
        prefs.edit().putBoolean(KEY_IS_GUEST, true).apply()
        _authState.value = AuthUserState(
            user = null,
            isGuest = true,
            isLoading = false,
            errorMessage = null
        )
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out error", e)
        }
        prefs.edit().putBoolean(KEY_IS_GUEST, true).apply()
        _authState.value = AuthUserState(
            user = null,
            isGuest = true,
            isLoading = false,
            errorMessage = null
        )
    }

    private fun getWebClientId(): String {
        return runCatching {
            context.getString(R.string.default_web_client_id)
        }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: "174035695269-9028fin0o0qcl550nc9i76akp55si0uc.apps.googleusercontent.com"
    }
}
