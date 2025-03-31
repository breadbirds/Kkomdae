package com.pizza.kkomdae.ui.step3

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentBackShotGuideBinding
import com.pizza.kkomdae.databinding.FragmentLaptopInfoInputBinding
import com.pizza.kkomdae.presenter.viewmodel.MainViewModel
import com.pizza.kkomdae.presenter.viewmodel.Step3ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class LaptopInfoInputFragment : BaseFragment<FragmentLaptopInfoInputBinding>(
    FragmentLaptopInfoInputBinding::bind,
    R.layout.fragment_laptop_info_input
) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val viewModel : Step3ViewModel by activityViewModels()

    private var laptopCount=1
    private var powerCount=1
    private var adapterCount=1
    private var mouseCount=1
    private var bagCount=1
    private var padCount=1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.topBar.pbStep.progress=100
        binding.topBar.tvTitle.text = "Step3"

        showIntroDialog()

        binding.etSerial.doOnTextChanged { text, start, before, count ->
           // 유효성 검사
            checkNext()
        }

        binding.etBarcode.doOnTextChanged { text, start, before, count ->
            // 유효성 검사
            checkNext()
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(Date())
        viewModel.setLocalDate(currentDate)

        // 날짜 설정
        settingDate()

        // 모델명 드랍다운 설정
        settingModelNameDropDown()

        // 노트북 개수 설정
        settingLaptopCount()

        // 전원선 개수 설정
        settingPowerCount()

        // 어댑터 개수 설정
        settingAdapterCount()

        // 마우스 개수 설정
        settingMouseCount()

        // 가방 개수 설정
        settingBagCount()

        // 마우스패드 개수 설정
        settingPadCount()



        binding.btnConfirm.setOnClickListener {
            inputData()

//            viewModel.postThirdStage()


            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fl_main, FinalResultFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

    }


    private fun inputData(){
        viewModel.apply {
            setModelCode(binding.atvModelName.text.toString())
            setSerialNum(binding.etSerial.text.toString())
            setBarcodeNum(binding.etBarcode.text.toString())
            setLaptop(binding.tvLaptopCount.text.toString().toInt())
            setPowerCable(binding.tvPowerCount.text.toString().toInt())
            setAdapter(binding.tvAdapterCount.text.toString().toInt())
            setMouse(binding.tvMouseCount.text.toString().toInt())
            setBag(binding.tvBagCount.text.toString().toInt())
            setMousePad(binding.tvPadCount.text.toString().toInt())
        }
    }

    private fun checkNext() {
        if (binding.etSerial.text.toString() != "" && binding.etBarcode.text.toString() != "") {
            binding.btnConfirm.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.blue500
                )
            )
            binding.btnConfirm.isClickable = true
        } else {

            binding.btnConfirm.isClickable = false
            binding.btnConfirm.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.blue200
                )
            )
        }
    }

    // 마우스패드 개수 설정
    private fun settingPadCount() {
        //플러스 버튼
        binding.btnPadPlus.setOnClickListener {
            if (padCount == 0) {
                binding.btnPadMinus.isClickable = true
                binding.btnPadMinus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray500
                    )
                )
            }
            padCount++
            binding.tvPadCount.text = padCount.toString()

        }

        // 마이너스 버튼
        binding.btnPadMinus.setOnClickListener {
            padCount--
            binding.tvPadCount.text = padCount.toString()
            if (padCount == 0) {
                binding.btnPadMinus.isClickable = false
                binding.btnPadMinus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray100
                    )
                )
            }
        }
    }

    // 가방 개수 설정
    private fun settingBagCount() {
        //플러스 버튼
        binding.btnBagPlus.setOnClickListener {
            if (bagCount == 0) {
                binding.btnBagMinus.isClickable = true
                binding.btnBagMinus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray500
                    )
                )
            }
            bagCount++
            binding.tvBagCount.text = bagCount.toString()

        }

        // 마이너스 버튼
        binding.btnBagMinus.setOnClickListener {
            bagCount--
            binding.tvBagCount.text = bagCount.toString()
            if (bagCount == 0) {
                binding.btnBagMinus.isClickable = false
                binding.btnBagMinus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray100
                    )
                )
            }
        }
    }

    // 마우스 개수 설정
    private fun settingMouseCount() {
        //플러스 버튼
        binding.btnMousePlus.setOnClickListener {
            if (mouseCount == 0) {
                binding.btnMouseMinus.isClickable = true
                binding.btnMouseMinus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray500
                    )
                )
            }
            mouseCount++
            binding.tvMouseCount.text = mouseCount.toString()

        }

        // 마이너스 버튼
        binding.btnMouseMinus.setOnClickListener {
            mouseCount--
            binding.tvMouseCount.text = mouseCount.toString()
            if (mouseCount == 0) {
                binding.btnMouseMinus.isClickable = false
                binding.btnMouseMinus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray100
                    )
                )
            }
        }
    }

    // 어댑터 개수 설정
    private fun settingAdapterCount() {
        //플러스 버튼
        binding.btnAdapterPlus.setOnClickListener {
            if (adapterCount == 0) {
                binding.btnAdapterMinus.isClickable = true
                binding.btnAdapterMinus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray500
                    )
                )
            }
            adapterCount++
            binding.tvAdapterCount.text = adapterCount.toString()

        }

        // 마이너스 버튼
        binding.btnAdapterMinus.setOnClickListener {
            adapterCount--
            binding.tvAdapterCount.text = adapterCount.toString()
            if (adapterCount == 0) {
                binding.btnAdapterMinus.isClickable = false
                binding.btnAdapterMinus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray100
                    )
                )
            }
        }
    }

    // 전원선 개수 설정
    private fun settingPowerCount() {
        // 플러스 버튼
        binding.btnPowerPlus.setOnClickListener {
            if (laptopCount == 0) {
                binding.btnPowerMinus.isClickable = true
                binding.btnPowerMinus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray500
                    ) // `blue`는 res/colors.xml에 정의된 색상
                )
            }
            powerCount++
            binding.tvPowerCount.text = powerCount.toString()

        }

        // 마이너스 버튼
        binding.btnPowerMinus.setOnClickListener {
            powerCount--
            binding.tvPowerCount.text = powerCount.toString()
            if (powerCount == 0) {
                binding.btnPowerMinus.isClickable = false
                binding.btnPowerMinus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray100
                    ) // `blue`는 res/colors.xml에 정의된 색상
                )
            }
        }
    }

    // 노트북 개수 설정
    private fun settingLaptopCount() {
        // 플러스 버튼
        binding.btnLaptopPlus.setOnClickListener {
            if (laptopCount == 0) {
                binding.btnLaptopMinus.isClickable = true
                binding.btnLaptopMinus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray500
                    ) // `blue`는 res/colors.xml에 정의된 색상
                )
            }
            laptopCount++
            binding.tvLaptopCount.text = laptopCount.toString()

        }

        // 마이너스 버튼
        binding.btnLaptopMinus.setOnClickListener {
            laptopCount--
            binding.tvLaptopCount.text = laptopCount.toString()
            if (laptopCount == 0) {
                binding.btnLaptopMinus.isClickable = false
                binding.btnLaptopMinus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray100
                    ) // `blue`는 res/colors.xml에 정의된 색상
                )
            }
        }
    }

    // 날짜 설정
    private fun settingDate() {
        binding.btnDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("날짜 선택") // 상단 제목
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // 기본 선택 날짜 (오늘)
                .build()

            // 날짜 선택 리스너
            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
                val targetFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val selectedDate = sdf.format(Date(selection))
                val targetDate = targetFormat.format(Date(selection))
                binding.tvDate.text = selectedDate
                viewModel.setLocalDate(targetDate)
            }

            // 다이얼로그 표시
            datePicker.show(requireActivity().supportFragmentManager, "DATE_PICKER")
        }
    }

    // 모델명 드랍다운 설정
    private fun settingModelNameDropDown() {
        val items = listOf(
            "NT850XCJ-XB72B(10세대)",
            "NT761XDA-X07/C(11세대)",
            "NT961XFH-X01/C(13세대)",
            " NT961XGL-COM(14세대)"
        )
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        binding.atvModelName.setAdapter(adapter)
    }

    // 시작 다이얼로그
    private fun showIntroDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_step3_intro)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val confirmButton = dialog.findViewById<Button>(R.id.btn_confirm)
        confirmButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LaptapInfoInputFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LaptopInfoInputFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}