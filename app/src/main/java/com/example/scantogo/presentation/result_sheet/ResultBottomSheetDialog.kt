package com.example.scantogo.presentation.result_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import com.example.scantogo.MainActivity
import com.example.scantogo.R
import com.example.scantogo.databinding.ViewResultBottomsheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.math.abs

class ResultBottomSheetDialog : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(qrContent: String): ResultBottomSheetDialog =
            ResultBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putString("qrContent", qrContent)
                }
            }
    }

    private var _binding: ViewResultBottomsheetDialogBinding? = null
    private val binding get() = _binding!!

    private val bottomSheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback by lazy {
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (isAdded) {
                    animationBottomSheetArrow(slideOffset)
                }
            }
        }
    }
    private val qrContent: String by lazy { arguments?.getString("qrContent") ?: "" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewResultBottomsheetDialogBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            initViews()
            dialog?.apply {
                setOnShowListener {
                    validateBottomSheet()
                }

                setOnDismissListener {
                    (requireActivity() as MainActivity).run { startCamera() }
                }
            }
        }
    }

    private fun initViews() {
        with(binding) {
            tvContent.isVisible = true
            tvContent.text = qrContent
        }
    }

    private fun animationBottomSheetArrow(slideOffSet: Float) {
        with(binding) {
            ivSwipeDownIcon.rotationX = (180 + (180 * slideOffSet))
        }
    }

    private fun validateBottomSheet() {
        val bottomSheetDialog = dialog as? BottomSheetDialog
        bottomSheetDialog?.let {
            val defaultSheet = dialog!!.findViewById<View>(R.id.design_bottom_sheet)

            BottomSheetBehavior.from(defaultSheet).run {
                state = BottomSheetBehavior.STATE_EXPANDED
                addBottomSheetCallback(bottomSheetBehaviorCallback)
                skipCollapsed = true
                setHeight(defaultSheet)
            }
        }
    }

    private fun setHeight(view: View) {
        view.layoutParams = view.layoutParams.apply {
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }



    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun getTheme() = R.style.ResultBottomSheetDialog
}