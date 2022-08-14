package br.com.zup.projectfinal.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import br.com.zup.projectfinal.domain.repository.AuthenticationRepository

class ProfileViewModel(application: Application): AndroidViewModel(application) {
    private val authenticationRepository = AuthenticationRepository()

    fun logout() {
        authenticationRepository.logout()
    }
}