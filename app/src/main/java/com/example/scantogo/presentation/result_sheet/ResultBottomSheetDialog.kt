package com.example.scantogo.presentation.result_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scantogo.MainActivity
import com.example.scantogo.R
import com.example.scantogo.databinding.ViewResultBottomsheetDialogBinding
import com.example.scantogo.extensions.toDP
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ResultBottomSheetDialog : BottomSheetDialogFragment() {

    companion object {
        const val EMAIL_TAG = "ResultBottomSheetDialogEmailType"
        const val SMS_TAG = "ResultBottomSheetDialogSmsType"
        const val TEXT_TAG = "ResultBottomSheetDialogTextType"

        fun newInstance(contents: Array<String>, contentType: String): ResultBottomSheetDialog =
            ResultBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putStringArray("contents", contents)
                    putString("contentType", contentType)
                }
            }
    }

    private var _binding: ViewResultBottomsheetDialogBinding? = null
    private val binding get() = _binding!!

    private val bottomSheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback by lazy {
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }
    }
    private val contents: MutableList<String> by lazy { arguments?.getStringArray("contents")?.toMutableList() ?: mutableListOf() }

    private val contentType: String by lazy { arguments?.getString("contentType") ?: "" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ViewResultBottomsheetDialogBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            initViews()
            initListener()
        }
    }

    private fun initViews() {
        binding.tvContent.text = getString(R.string.barcode_content_type, contentType)
        with(binding.rvContacts) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = ResultAdapter(
                contents = contents,
                onCopyListener = {
                    (requireActivity() as MainActivity).onContentCopy(it)
                }
            )
            addItemDecoration(VerticalSpaceDivider(verticalSpaceHeight = 8.toDP()))
        }
    }

    private fun initListener() {
        dialog?.apply {
            setOnShowListener {
                validateBottomSheet()
            }

            setOnDismissListener {
                (requireActivity() as MainActivity).onDismiss()
            }
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
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    interface OnDismissedListener {
        fun onDismiss()
        fun onContentCopy(content: String)
    }

    override fun getTheme() = R.style.ResultBottomSheetDialog
}