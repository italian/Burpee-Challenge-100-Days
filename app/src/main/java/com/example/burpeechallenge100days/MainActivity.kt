package com.example.burpeechallenge100days

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {
    private val viewModel: BurpeeViewModel by lazy {
        val repository = BurpeeRepository(applicationContext)
        ViewModelProvider(this, BurpeeViewModelFactory(repository))[BurpeeViewModel::class.java]
    }

    private lateinit var tvCurrentDay: TextView
    private lateinit var tvTotalBurpees: TextView
    private lateinit var tvBurpeesLeft: TextView
    private lateinit var tvBurpeesDone: TextView
    private lateinit var etBurpeesInput: EditText
    private lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupObservers()
        setupListeners()
    }

    private fun initViews() {
        tvCurrentDay = findViewById(R.id.tvCurrentDay)
        tvTotalBurpees = findViewById(R.id.tvTotalBurpees)
        tvBurpeesLeft = findViewById(R.id.tvBurpeesLeft)
        tvBurpeesDone = findViewById(R.id.tvBurpeesDone)
        etBurpeesInput = findViewById(R.id.etBurpeesInput)
        btnConfirm = findViewById(R.id.btnConfirm)
    }

    private fun setupObservers() {
        viewModel.currentDay.observe(this) { day ->
            tvCurrentDay.text = "День: $day"
        }
        viewModel.totalBurpeesToday.observe(this) { total ->
            tvTotalBurpees.text = "Всего бёрпи сегодня: $total"
        }
        viewModel.burpeesLeft.observe(this) { left ->
            tvBurpeesLeft.text = "Осталось бёрпи: $left"
        }
        viewModel.burpeesDone.observe(this) { done ->
            tvBurpeesDone.text = "Выполнено бёрпи: $done"
        }
    }

    private fun setupListeners() {
        btnConfirm.setOnClickListener {
            val input = etBurpeesInput.text.toString()
            if (input.startsWith("0")) {
                val day = input.toIntOrNull() ?: 1
                viewModel.setDay(day)
            } else {
                val burpees = input.toIntOrNull() ?: 0
                viewModel.addBurpees(burpees)
            }
            etBurpeesInput.text.clear()
        }
    }
}

class BurpeeViewModelFactory(private val repository: BurpeeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BurpeeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BurpeeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
