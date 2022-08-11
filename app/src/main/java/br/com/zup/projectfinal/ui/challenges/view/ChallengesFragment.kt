package br.com.zup.projectfinal.ui.challenges.view

import android.os.Bundle
import android.view.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.zup.projectfinal.R
import br.com.zup.projectfinal.databinding.FragmentChallengesBinding
import br.com.zup.projectfinal.domain.model.ChallengeModel
import br.com.zup.projectfinal.ui.InitialActivity
import br.com.zup.projectfinal.ui.challenges.view.adapter.ChallengesAdapter
import br.com.zup.projectfinal.ui.challenges.viewmodel.ChallengesViewModel
import br.com.zup.projectfinal.ui.viewstate.ViewState
import br.com.zup.projectfinal.utils.TITLE_DESAFIOS

class ChallengesFragment : Fragment() {
    private lateinit var binding: FragmentChallengesBinding

    private val viewModel: ChallengesViewModel by lazy {
        ViewModelProvider(this)[ChallengesViewModel::class.java]
    }

    private val challengesAdapter: ChallengesAdapter by lazy {
        ChallengesAdapter(arrayListOf(), this::onCheckboxClicked)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        actionBarAccess()
        showUserData()

        showUserName()
        viewModel.saveUsernameFirebaseDatabase(viewModel.getUserName())
        viewModel.getPointsDatabase()
        viewModel.getLevelDatabase()
        viewModel.getFourRandomChallenges()
        initObserver()
        showChallengesRecyclerView()
    }

    private fun actionBarAccess() {
        (activity as InitialActivity).supportActionBar?.show()
        (activity as InitialActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as InitialActivity).supportActionBar?.title = TITLE_DESAFIOS
    }

    private fun showUserData() {
        val name = viewModel.getUserName()
        binding.tvHelloZupper.text = getString(R.string.texto_home, name)
    }

    private fun navigateToLoginFragment() {
        NavHostFragment.findNavController(this)
            .navigate(R.id.action_challengesFragment_to_loginFragment)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.exit -> {
                viewModel.logout()
                this.onDestroy()
                navigateToLoginFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showChallengesRecyclerView() {
        binding.rvChallenges.adapter = challengesAdapter
        binding.rvChallenges.layoutManager = LinearLayoutManager(context)
    }

    private fun initObserver() {
        viewModel.challengesListState.observe(this.viewLifecycleOwner) {
            when (it) {
                is ViewState.Success -> {
                    challengesAdapter.updateNotesList(it.data.toMutableList())
                }
                is ViewState.Error -> {
                    Toast.makeText(context, "${it.throwable.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.msgState.observe(this.viewLifecycleOwner){
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }

        viewModel.levelState.observe(this.viewLifecycleOwner){
            showLevel(it)
        }

        viewModel.pointsState.observe(this.viewLifecycleOwner){
            showPoints(it)
        }
    }

    private fun showUserName(){
        val greetings = buildString {
            append("Olá, ")
            append(viewModel.getUserName())
            append("!")
        }
        binding.tvHelloZupper.text = greetings
    }

    private fun savePoints(doneChallenge: ChallengeModel){
        viewModel.savePoints(doneChallenge.challengePoints)
    }

    private fun showPoints(pointList: List<String>){
        var totalPoints = 0
        if(pointList.isNotEmpty()){
            pointList.forEach { point ->
                totalPoints += point.toInt()
            }
        }else{
            totalPoints = 0
        }
        binding.tvNumbPoints.text = totalPoints.toString()
        saveLevel(totalPoints)
    }

    private fun saveLevel(points: Int){
        //0pts = level 1
        //100pts = level 2
        //200pts = level 3
        //300pts = level 4
        //500pts = level 5

        if(points in 100..199){
            viewModel.saveLevel(2)
        }
        if(points in 200..299){
            viewModel.saveLevel(3)
        }
        if(points in 300..499){
            viewModel.saveLevel(4)
        }
        if(points in 500..599){
            viewModel.saveLevel(5)
        }
    }

    private fun showLevel(levelList: List<String>){
        if(levelList.isNotEmpty()){
            levelList.sortedBy {
                it.toInt().inc()
            }
            binding.tvLevel.text = "Nível " + levelList[0]
        }else{
            binding.tvLevel.text = "Nível 1"
        }
    }

    fun onCheckboxClicked(view: View, challengeModel: ChallengeModel) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            when (view.id) {
                R.id.cbCheck -> {
                    if (checked) {
                        savePoints(challengeModel)
                    }
                }
            }
        }
    }
}