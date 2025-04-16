package com.example.pj1.ui.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.pj1.databinding.FragmentNavWebBinding
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class webFragment : Fragment() {

    private val viewModel: WebViewModel by viewModels()
    private var _binding: FragmentNavWebBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNavWebBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
        setupListeners()
        setupBackButton()
    }

    private fun setupWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?, request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString()
                    if (url != null) {
                        view?.loadUrl(url)
                        binding.urlEditText.setText(url)
                    }
                    return true
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBar.visibility = View.VISIBLE
                    binding.urlEditText.setText(url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError
                ) {
                    if (request.isForMainFrame) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, "Error: ${error.description}", Toast.LENGTH_SHORT).show()
                    }
                }

                @Suppress("DEPRECATION")
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error: $description", Toast.LENGTH_SHORT).show()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if (newProgress < 100 && binding.progressBar.visibility == View.GONE) {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    binding.progressBar.progress = newProgress
                    if (newProgress == 100) {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.goButton.setOnClickListener { handleUrlInput() }

        binding.urlEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                handleUrlInput()
                true
            } else false
        }
    }

    private fun handleUrlInput() {
        val input = binding.urlEditText.text.toString().trim()
        if (input.isNotEmpty()) {
            hideKeyboard()
            // Evita bloqueo del hilo principal al cargar
            binding.webView.post {
                loadUrlOrSearch(input)
            }
        }
    }

    private fun loadUrlOrSearch(input: String) {
        if (Patterns.WEB_URL.matcher(input).matches()) {
            var url = input
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            binding.webView.loadUrl(url)
        } else {
            try {
                val query = URLEncoder.encode(input, StandardCharsets.UTF_8.toString())
                val searchUrl = "https://www.bing.com/search?q=$query"
                binding.webView.loadUrl(searchUrl)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al codificar búsqueda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                }
            })
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.destroy()
        _binding = null
    }
}
