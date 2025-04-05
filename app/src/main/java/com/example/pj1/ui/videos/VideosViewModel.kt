package com.example.pj1.ui.videos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VideosViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "T"
    }
    val text: LiveData<String> = _text
}