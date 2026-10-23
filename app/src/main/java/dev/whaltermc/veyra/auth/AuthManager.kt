package dev.whaltermc.veyra.auth

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.common.util.concurrent.ListenableFuture
import dev.whaltermc.veyra.data.model.AccountEntity
import dev.whaltermc.veyra.data.model.AuthType
import dev.whaltermc.veyra.data.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.Executors

// Keys for secure storage
private val KEY_ACTIVE_ACCOUNT_ID = preferencesKey<String>("active_account_id")

class AuthManager(
    private val context: Context,
    private val accountRepository: AccountRepository
) {
    private val securePrefs = createSecurePrefs()
    private val scope = CoroutineScope(Dispatchers.IO)

    var activeAccountId: String?
        get() = securePrefs.getString(KEY_ACTIVE_ACCOUNT_ID, "")
            .takeIf { it.isNotBlank() }
        set(value) {
            securePrefs.edit { it[KEY_ACTIVE_ACCOUNT_ID] = value ?: "" }.apply()
        }

    private fun createSecurePrefs(): android.content.SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "veyra_auth_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getActiveAccount(): AccountEntity? {
        activeAccountId?.let { accountRepository.getAccountById(it).await() }
    }

    suspend fun setActiveAccount(accountId: String) {
        accountRepository.updateLastUsed(accountId)
        activeAccountId = accountId
    }

    suspend fun removeAccount(accountId: String) {
        if (activeAccountId == accountId) {
            activeAccountId = null
        }
        accountRepository.deleteAccount(accountId)
        // TODO: Delete skin cache file
    }

    suspend fun refreshAccount(accountId: String) {
        accountRepository.getAccountById(accountId)?.let { account ->
            when (account.authType) {
                AuthType.MICROSOFT -> refreshMicrosoftToken(account)
                AuthType.ELY_BY -> refreshElyByToken(account)
                AuthType.OFFLINE -> { /* nothing to refresh */ }
            }
        }
    }

    private suspend fun refreshMicrosoftToken(account: AccountEntity) {
        // TODO: Implement Microsoft token refresh using refresh token
        // This would exchange refresh token for new access token
    }

    private suspend fun refreshElyByToken(account: AccountEntity) {
        // TODO: Implement Ely.by token refresh
    }

    suspend fun ensureValidToken(accountId: String) {
        accountRepository.getAccountById(accountId)?.let { account ->
            if (account.authType != AuthType.OFFLINE) {
                // Check if token is expired (would need to decode token blob)
                // For now, just refresh proactively
                refreshAccount(accountId)
            }
        }
    }

    // Microsoft OAuth device-code flow
    suspend fun startMicrosoftAuth(): Result<MicrosoftDeviceCodeResponse> {
        // TODO: Implement actual HTTP call to Microsoft device code endpoint
        return async(Dispatchers.IO) {
            // Placeholder - would call https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode
            try {
                // Simulate for now
                Result.success(MicrosoftDeviceCodeResponse(
                    deviceCode = "dummy_device_code",
                    userCode = "ABCD-EFGH",
                    verificationUri = "https://aka.ms/devicelogin",
                    interval = 5,
                    expiresIn = 900
                ))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }.await()
    }

    suspend fun pollMicrosoftToken(deviceCode: String): Result<MicrosoftTokenResponse> {
        return async(Dispatchers.IO) {
            // TODO: Poll Microsoft token endpoint
            Result.failure(Exception("authorization_pending"))
        }.await()
    }

    suspend fun completeMicrosoftAuth(tokens: MicrosoftTokenResponse): Result<AccountEntity> {
        return async(Dispatchers.IO) {
            // TODO: Exchange for Xbox Live -> XSTS -> Minecraft tokens
            // Then fetch profile and create account
            Result.failure(Exception("Not implemented"))
        }.await()
    }
}

data class MicrosoftDeviceCodeResponse(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val interval: Int,
    val expiresIn: Int
)

data class MicrosoftTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val tokenType: String
)

data class XboxLiveTokenResponse(
    val token: String,
    val displayClaims: Map<String, List<DisplayClaim>>
)

data class DisplayClaim(
    val xui: List<String>,
    val uhs: String
)

data class XSTSTokenResponse(
    val token: String,
    val displayClaims: Map<String, List<DisplayClaim>>
)

data class MinecraftTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int
)

data class MinecraftProfile(
    val id: String,
    val name: String,
    val skins: List<Skin>,
    val capes: List<Cape>
)

data class Skin(
    val id: String,
    val state: String,
    val url: String,
    val variant: String
)

data class Cape(
    val id: String,
    val state: String,
    val url: String
)