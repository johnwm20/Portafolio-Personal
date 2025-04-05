package com.example.pj1.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Aquí encontraras diferentes herramientas que te mostraran toda la información contenida en  este portafolio.\n"
    }
    val text: LiveData<String> = _text
}