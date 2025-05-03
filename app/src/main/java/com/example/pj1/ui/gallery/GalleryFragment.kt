package com.example.pj1.ui.gallery

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pj1.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ImageAdapter

    // Selector de imagen desde el dispositivo
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            adapter.addImage(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this)[GalleryViewModel::class.java]

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)

        // Carga inicial de imágenes desde drawable
        val drawableUris = listOf(
            Uri.parse("android.resource://${requireContext().packageName}/drawable/imagen1"),
            Uri.parse("android.resource://${requireContext().packageName}/drawable/imagen2")
        ).toMutableList()

        // Inicializa el adaptador y RecyclerView
        adapter = ImageAdapter(drawableUris)
        binding.recyclerViewGallery.adapter = adapter
        binding.recyclerViewGallery.layoutManager = GridLayoutManager(requireContext(), 2)

        // Observa el texto del ViewModel (opcional)
        galleryViewModel.text.observe(viewLifecycleOwner) {
            binding.textGallery.text = it
        }

        // Botón para agregar nuevas imágenes
        binding.btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
