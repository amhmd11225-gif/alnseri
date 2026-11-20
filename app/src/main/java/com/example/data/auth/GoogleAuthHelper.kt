package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest

data class AuthDiagnostics(
    val hasFirebaseApp: Boolean,
    val hasWebClientId: Boolean,
    val appSigningSha1: String?,
)

object GoogleAuthHelper {
    const val CHANNEL_OWNER_EMAIL = "amhmd11225@gmail.com"
    private const val TAG = "AlAnsariAuth"

    fun isVerifiedChannelOwner(user: FirebaseUser?): Boolean {
        return user != null &&
            user.isEmailVerified &&
            user.email.equals(CHANNEL_OWNER_EMAIL, ignoreCase = true)
    }

    fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        addAuthStateListener(listener)
        awaitClose { removeAuthStateListener(listener) }
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    private fun resolveWebClientId(context: Context): String? {
        val resId = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName,
        )
        if (resId == 0) return null
        return runCatching { context.getString(resId).trim() }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
    }

    fun inspectAuthDiagnostics(context: Context): AuthDiagnostics {
        val hasFirebase = runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)
        val hasClientId = resolveWebClientId(context) != null
        val sha1 = getApkSigningSha1(context)
        return AuthDiagnostics(
            hasFirebaseApp = hasFirebase,
            hasWebClientId = hasClientId,
            appSigningSha1 = sha1,
        )
    }

    @Suppress("DEPRECATION")
    private fun getApkSigningSha1(context: Context): String? {
        return runCatching {
            val pm = context.packageManager
            val certBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = info.signingInfo
                val signatures = if (signingInfo?.hasMultipleSigners() == true) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo?.signingCertificateHistory
                }
                signatures?.firstOrNull()?.toByteArray()
            } else {
                val info = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                info.signatures?.firstOrNull()?.toByteArray()
            } ?: return@runCatching null

            val digest = MessageDigest.getInstance("SHA-1").digest(certBytes)
            digest.joinToString(":") { byte -> "%02X".format(byte) }
        }.getOrNull()
    }

    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onAuthSuccess: () -> Unit,
        onUnauthenticated: () -> Unit,
    ) {
        val hasFirebase = runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)
        if (!hasFirebase) {
            onUnauthenticated()
            return
        }
        if (runCatching { Firebase.auth.currentUser }.getOrNull() != null) {
            onAuthSuccess()
            return
        }
        val clientId = resolveWebClientId(context)
        if (clientId == null) {
            onUnauthenticated()
            return
        }
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val cred = credentialManager.getCredential(context, request).credential
                if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                    Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                    onAuthSuccess()
                } else {
                    onUnauthenticated()
                }
            } catch (_: Exception) {
                onUnauthenticated()
            }
        }
    }

    fun onGoogleSignInClicked(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit,
        onAuthCancelled: (String) -> Unit = {},
    ) {
        val hasFirebase = runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)
        if (!hasFirebase) {
            onAuthError("ملف إعدادات Firebase (google-services.json) غير مدمج في هذه النسخة من التطبيق. أعد بناء APK من AI Studio مباشرة أو أضف GOOGLE_SERVICES_JSON في GitHub Secrets.")
            return
        }
        val clientId = resolveWebClientId(context)
        if (clientId == null) {
            onAuthError("إعدادات تسجيل دخول Google (default_web_client_id) غير موجودة في هذه النسخة. تأكد من دمج google-services.json عند البناء.")
            return
        }
        val activity = context.findActivity()
        if (activity == null) {
            onAuthError("تعذر ربط نافذة تسجيل الدخول بنشاط الشاشة الحالي")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val cred = credentialManager.getCredential(activity, request).credential
                if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                    Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                    onAuthSuccess()
                } else {
                    onAuthError("نوع بيانات اعتماد غير مدعوم (${cred.type})")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelled or dismissed: ${e.errorMessage ?: e.message}", e)
                val detail = e.errorMessage?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                val message = if (detail != null) {
                    "تم إغلاق أو رفض نافذة Google ($detail). تحقق من أن بصمة توقيع التطبيق (SHA-1) مطابقة لإعدادات Firebase."
                } else {
                    "تم إلغاء نافذة Google أو رفضتها خدمات Google Play بسبب عدم تطابق بصمة التوقيع (SHA-1) للنسخة المثبّتة."
                }
                onAuthCancelled(message)
            } catch (e: NoCredentialException) {
                Log.w(TAG, "No Google credential available: ${e.errorMessage ?: e.message}", e)
                onAuthError("لم يتم العثور على حساب Google فعّال على الجهاز أو تعذّر التحقق من بصمة التوقيع (SHA-1) مع Firebase.")
            } catch (e: GetCredentialException) {
                Log.e(TAG, "CredentialManager error [${e.type}]: ${e.errorMessage}", e)
                val detail = e.errorMessage?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: e.type
                onAuthError("تعذر تسجيل الدخول عبر خدمات Google ($detail)")
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onAuthError(e.localizedMessage ?: "تعذر تسجيل الدخول بحساب Google")
            }
        }
    }

    fun signOut(
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSignOutComplete: () -> Unit = {},
    ) {
        scope.launch {
            withContext(NonCancellable) {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clear credential state: ${e.message}")
                } finally {
                    runCatching { Firebase.auth.signOut() }
                    onSignOutComplete()
                }
            }
        }
    }
}
