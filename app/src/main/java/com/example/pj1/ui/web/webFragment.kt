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
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.pj1.databinding.FragmentNavWebBinding // Importa la clase de binding generada
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

    // Necesario para habilitar JavaScript
    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWebView()
        setupListeners()
        setupBackButton()

        // Podemos tener una página inicial
        // binding.webView.loadUrl("https://www.google.com")
    }

    private fun setupWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true

            webViewClient = object : WebViewClient() {
                // Cuando el WebView está a punto de cargar una URL.
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString()
                    if (url != null) {
                        view?.loadUrl(url)

                        // Actualizar barra de dirección
                        binding.urlEditText.setText(url)
                    }
                    return true
                }

                // Cuando la página empieza a cargarse
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBar.visibility = View.VISIBLE
                    binding.urlEditText.setText(url)
                }

                // Cuando la página ha terminado de cargarse
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error: $description", Toast.LENGTH_SHORT).show()

                    // Podríamos crear una vista de una página de error
                    // view?.loadUrl("file:///android_asset/error_page.html")
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
        binding.goButton.setOnClickListener {
            handleUrlInput()
        }

        binding.urlEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                handleUrlInput()
                true
            } else {
                false
            }
        }
    }

    private fun handleUrlInput() {
        val input = binding.urlEditText.text.toString().trim()
        if (input.isNotEmpty()) {
            loadUrlOrSearch(input)
            hideKeyboard()
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
            // Si no es una URL, realiza una búsqueda en Google
            try {
                val searchQuery = URLEncoder.encode(input, StandardCharsets.UTF_8.toString())
                val searchUrl = "https://www.google.com/search?q=$searchQuery"
                binding.webView.loadUrl(searchUrl)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al codificar la búsqueda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBackButton() {
        // Botón de retroceso del sistema
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Si el WebView puede retroceder en su historial
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        // Si no puede retroceder, permite que el sistema maneje el botón
                        // (por ejemplo, cerrar el fragment o la actividad)
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