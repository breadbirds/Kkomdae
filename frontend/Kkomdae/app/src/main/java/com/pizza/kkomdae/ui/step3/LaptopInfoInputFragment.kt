package com.pizza.kkomdae.ui.step3

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.pizza.kkomdae.MainActivity
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentBackShotGuideBinding
import com.pizza.kkomdae.databinding.FragmentLaptopInfoInputBinding
import com.pizza.kkomdae.presenter.viewmodel.Step2ViewModel
import com.pizza.kkomdae.presenter.viewmodel.Step3ViewModel
import com.pizza.kkomdae.ui.LoadingFragment
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
@RequiresApi(Build.VERSION_CODES.O)
class LaptopInfoInputFragment : BaseFragment<FragmentLaptopInfoInputBinding>(
    FragmentLaptopInfoInputBinding::bind,
    R.layout.fragment_laptop_info_input
) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var laptopCount=1
    private var powerCount=1
    private var adapterCount=1
    private var mouseCount=1
    private var bagCount=1
    private var padCount=1
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val now = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant())

    private var date = dateFormat.format(now)
    private val viewModel: Step3ViewModel by activityViewModels()


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

        // post 통신 결과
        viewModel.postResponse.observe(viewLifecycleOwner){
            if(it.success && it.status=="OK"){ // 통신 성공
                showEndDialog()
            }else{ // todo 통신 실패시

            }
        }

        // X 클릭 이벤트 설정
        binding.topBar.backButtonContainer.setOnClickListener {
            showQuitBottomSheet()
        }

        // 완료 버튼
        binding.btnConfirm.setOnClickListener {
            val serialValid = binding.etSerial.text.toString().isNotEmpty()
            val barcodeValid = binding.etBarcode.text.toString().isNotEmpty()
            val modelValid = binding.atvModelName.text.toString().isNotEmpty()
            val dateValid = binding.tvDate.text.toString() != "날짜 선택"

            if (serialValid && barcodeValid && modelValid && dateValid) {
                // 모든 필수 정보가 입력됨 - 다이얼로그 표시
                inputData()
                showConfirmDialog()
            } else {
                // 어떤 필드가 누락되었는지 사용자에게 알려줍니다
                val missingFields = mutableListOf<String>()
                if (!serialValid) missingFields.add("시리얼 번호")
                if (!barcodeValid) missingFields.add("바코드 번호")
                if (!modelValid) missingFields.add("모델명")
                if (!dateValid) missingFields.add("수령일자")

                val message = "노트북 정보를 모두 입력해주세요."
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun inputData(){
        viewModel.apply {
            setModelCode(binding.atvModelName.text.toString())
            setSerialNum(binding.etSerial.text.toString())
            setBarcodeNum(binding.etBarcode.text.toString())
            setLocalDate(date)
            setLaptop(laptopCount)
            setPowerCable(powerCount)
            setAdapter(adapterCount)
            setMouse(mouseCount)
            setBag(bagCount)
            setMousePad(padCount)

        }
    }

    private fun showQuitBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_bottom_sheet, null)

        // 계속하기 버튼 클릭 시 바텀시트 닫기
        val btnContinue = bottomSheetView.findViewById<View>(R.id.btn_continue)
        btnContinue.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // 그만두기 버튼 클릭 시 메인 화면으로 이동
        val btnQuit = bottomSheetView.findViewById<View>(R.id.btn_quit)
        btnQuit.setOnClickListener {
            bottomSheetDialog.dismiss()
            // 메인 화면으로 이동
            val mainActivity = requireActivity() as MainActivity
            mainActivity.supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }


    private fun checkNext() {
        val serialValid = binding.etSerial.text.toString().isNotEmpty()
        val barcodeValid = binding.etBarcode.text.toString().isNotEmpty()
        val modelValid = binding.atvModelName.text.toString().isNotEmpty()
        val dateValid = binding.tvDate.text.toString() != "날짜 선택"

        val allValid = serialValid && barcodeValid && modelValid && dateValid

        binding.btnConfirm.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(),
                if (allValid) R.color.blue500 else R.color.blue200
            )
        )
        binding.btnConfirm.isClickable = allValid
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
                .setTitleText("날짜 선택")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // 기본 선택 날짜 (오늘)
                .build()

            // 날짜 선택 리스너
            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
                val selectedDate = sdf.format(Date(selection))
                binding.tvDate.text = selectedDate
                date = dateFormat.format(Date(selection))
                checkNext()

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

        binding.atvModelName.setOnItemClickListener { _, _, _, _ ->
            checkNext()
        }
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

    // 완료 다이얼로그
    private fun showConfirmDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_step3_confirm)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 닫기 버튼 클릭 리스너
        val cancelButton = dialog.findViewById<View>(R.id.btn_cancel)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // 입력 완료하기 버튼 클릭 리스너
        val confirmButton = dialog.findViewById<View>(R.id.btn_confirm)
        confirmButton.setOnClickListener {
            viewModel.postThirdStage()
            dialog.dismiss()

        }

        dialog.show()
    }

    // 종료 다이얼로그
    private fun showEndDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_step_end)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // "분석하러 가기" 버튼에 클릭 리스너 추가
        val confirmButton = dialog.findViewById<Button>(R.id.btn_confirm)
        confirmButton.setOnClickListener {
            dialog.dismiss() // 다이얼로그 닫기

            // AiResultFragment로 전환
//            val transaction = requireActivity().supportFragmentManager.beginTransaction()
//            transaction.replace(R.id.fl_main, AiResultFragment())
//            transaction.addToBackStack(null) // 뒤로 가기 버튼으로 이전 화면으로 돌아갈 수 있도록 설정
//            transaction.commit()

            // LoadingFragment 전환
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fl_main, LoadingFragment())
            transaction.addToBackStack(null) // 뒤로 가기 버튼으로 이전 화면으로 돌아갈 수 있도록 설정
            transaction.commit()
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
