package dev.whaltermc.veyra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whaltermc.veyra.VeyraApplication
import dev.whaltermc.veyra.auth.AuthManager
import dev.whaltermc.veyra.data.model.AccountEntity
import dev.whaltermc.veyra.data.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AccountViewModel(
    private val repository: AccountRepository,
    private val authManager: AuthManager
) : ViewModel() {

    val accounts: Flow<List<AccountEntity>> = repository.getAllAccounts()

    fun loginOffline(username: String): kotlinx.coroutines.Deferred<Result<AccountEntity>> {
        return kotlinx.coroutines.CoroutineScope(Dispatchers.IO).async {
            try {
                val uuid = repository.generateOfflineUuid(username)
                val account = AccountEntity(
                    id = UUID.randomUUID().toString(),
                    displayName = username,
                    uuid = uuid,
                    authType = dev.whaltermc.veyra.data.model.AuthType.OFFLINE,
                    encryptedTokenBlob = "{}",
                    lastUsed = kotlinx.datetime.Clock.System.now()
                )
                repository.insertAccount(account)
                authManager.setActiveAccount(account.id)
                Result.success(account)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun loginElyBy(username: String, password: String): kotlinx.coroutines.Deferred<Result<AccountEntity>> {
        return kotlinx.coroutines.CoroutineScope(Dispatchers.IO).async {
            try {
                // TODO: Implement Ely.by OAuth2 flow
                // For now, return error
                Result.failure(Exception("Ely.by auth not yet implemented"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    companion object {
        class Factory(private val app: VeyraApplication) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AccountViewModel(app.accountRepository, app.authManager) as T
            }
        }
    }
}