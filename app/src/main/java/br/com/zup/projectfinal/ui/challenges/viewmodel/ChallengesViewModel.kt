package br.com.zup.projectfinal.ui.challenges.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.zup.projectfinal.domain.model.ChallengeModel
import br.com.zup.projectfinal.domain.repository.AuthenticationRepository
import br.com.zup.projectfinal.domain.repository.ChallengesRepository
import br.com.zup.projectfinal.domain.usecase.ChallengesUseCase
import br.com.zup.projectfinal.ui.viewstate.ViewState
import br.com.zup.projectfinal.utils.CHALLENGES_LIST_ERROR
import br.com.zup.projectfinal.utils.CONGRATULATION
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ChallengesViewModel(application: Application) : AndroidViewModel(application) {
    private val authenticationRepository = AuthenticationRepository()
    private val challengesUseCase = ChallengesUseCase(application)
    private val challengesRepository = ChallengesRepository()

    private var _challengesListState = MutableLiveData<ViewState<List<ChallengeModel>>>()
    val challengesListState: LiveData<ViewState<List<ChallengeModel>>> = _challengesListState

    private var _msgState = MutableLiveData<String>()
    val msgState: LiveData<String> = _msgState

    private var _levelState = MutableLiveData<List<String>>()
    val levelState: LiveData<List<String>> = _levelState

    private var _pointsState = MutableLiveData<List<String>>()
    val pointsState: LiveData<List<String>> = _pointsState

    fun logout() {
        authenticationRepository.logout()
    }

    fun getFourRandomChallenges() {
        try {
            _challengesListState.value = challengesUseCase.getFourRandomChallenges()
        } catch (e: Exception) {
            _challengesListState.value = ViewState.Error(Throwable(CHALLENGES_LIST_ERROR))
        }
    }

    fun getUserName(): String{
        return challengesUseCase.getUserName()
    }

    fun savePoints(point: Int) {
        val pointsPath = getPointsPath(point)
        challengesRepository.databaseReferencePoints().child("$pointsPath")
            .setValue(point) { error, reference ->
                if (error != null) {
                    _msgState.value = error.message
                }
                _msgState.value = CONGRATULATION
            }
    }

    private fun getPointsPath(point: Int): String {
        val uri = Uri.parse(point.toString())
        return uri.toString()
    }

    fun saveLevel(level: Int) {
        val levelPath = getLevelPath(level)
        challengesRepository.databaseReferenceLevel().child("$levelPath")
            .setValue(level) { error, reference ->
                if (error != null) {
                    _msgState.value = error.message
                }
            }
    }

    private fun getLevelPath(level: Int): String {
        val uri = Uri.parse(level.toString())
        return uri.toString()
    }

    fun getLevelDatabase() {
        challengesRepository.getLevel().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val levelList = mutableListOf<String>()
                for (resultSnapshot in snapshot.children) {
                    val levelResponse = resultSnapshot.value.toString()
                    levelResponse.let {
                        levelList.add(it)
                    }
                }
                _levelState.value = levelList
            }

            override fun onCancelled(error: DatabaseError) {
                _msgState.value = error.message
            }
        })
    }

    fun getPointsDatabase() {
        challengesRepository.getPoints().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pointsList = mutableListOf<String>()
                for (resultSnapshot in snapshot.children) {
                    val pointsResponse = resultSnapshot.value.toString()
                    pointsResponse.let {
                        pointsList.add(it)
                    }
                }
                _pointsState.value = pointsList
            }

            override fun onCancelled(error: DatabaseError) {
                _msgState.value = error.message
            }
        })
    }

    //############ SETAR NOME DE USUÁRIO NO REALTIME DATABASE DO FIREBASE ############

    private fun getUserNamePath(username: String): String {
        val uri = Uri.parse(username)
        return uri.toString()
    }

    fun saveUsernameFirebaseDatabase(username: String) {
        val usernamePath = getUserNamePath(username)
        challengesRepository.databaseReferenceUsername().child(usernamePath)
            .setValue(username) { error, _ ->
                if (error != null) {
                    _msgState.value = error.message
                }
            }
    }
}