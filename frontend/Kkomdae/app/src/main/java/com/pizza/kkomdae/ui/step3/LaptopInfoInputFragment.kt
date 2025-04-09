package com.pizza.kkomdae.ui.step3

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.fragment.app.activityViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.pizza.kkomdae.MainActivity
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentBackShotGuideBinding
import com.pizza.kkomdae.databinding.FragmentLaptopInfoInputBinding
import com.pizza.kkomdae.presenter.viewmodel.MainViewModel
import com.pizza.kkomdae.presenter.viewmodel.Step2ViewModel
import com.pizza.kkomdae.presenter.viewmodel.Step3ViewModel
import com.pizza.kkomdae.ui.LoadingFragment
import com.pizza.kkomdae.di.GoogleVisionApi
import com.pizza.kkomdae.presenter.viewmodel.CameraViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.log

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
    private var imageUri: Uri? = null
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
    private val mainViewModel: MainViewModel by activityViewModels()

    // 시스템 백 버튼 콜백 선언
    private lateinit var backPressedCallback: OnBackPressedCallback
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private val cameraViewModel: CameraViewModel by activityViewModels()
    private lateinit var imageFile: File
    private var introDialog: Dialog? = null
    private var confirmDialog: Dialog? = null
    private var endDialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        // 시스템 백 버튼 동작 설정
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showQuitBottomSheet()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)

    }

    override fun onDestroy() {
        super.onDestroy()
        backPressedCallback.remove()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ocr
        // 1단계에서 감지된 livedata 반영
        cameraViewModel.ocrSerial.value?.let {
            binding.etSerial.setText(it)
            Log.d("OCR", "💡 Already set serial: $it")
        }

        cameraViewModel.ocrBarcode.value?.let {
            binding.etBarcode.setText(it)
            Log.d("OCR", "💡 Already set barcode: $it")
        }
        // laptopinfoinput 화면에서 변화되는 livedata 감지
        cameraViewModel.ocrSerial.observe(viewLifecycleOwner) {
            Log.d("OCR", "LiveData observe - serial: $it")  // 👈 LiveData로 전달받은 값
            binding.etSerial.setText(it)
            Log.d("OCR", "serial: $it")
        }

        cameraViewModel.ocrBarcode.observe(viewLifecycleOwner) {
            Log.d("OCR", "LiveData observe - barcode: $it")  // 👈 LiveData로 전달받은 값
            binding.etBarcode.setText(it)
            Log.d("OCR", "barcode: $it")
        }

        binding.topBar.pbStep.progress = 100
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

        //ocr
        loadOcrResult()

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
            it ?: return@observe
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

//        // OCR
//        // OCR 카메라 초기화
        // OCR 카메라 초기화
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                if (::imageFile.isInitialized && imageFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    bitmap?.let {
                        Log.d("OCR", "비트맵 성공!")

                        // OCR 결과 → SharedPreferences 저장
                        GoogleVisionApi.callOcr(requireContext(), encodeImageToBase64(it)) { serial, barcode ->
                            val prefs = requireContext().getSharedPreferences("ocr_prefs", Context.MODE_PRIVATE)
                            prefs.edit().apply {
                                putString("ocr_serial", serial)
                                putString("ocr_barcode", barcode)
                                apply()
                            }
                            Log.d("OCR", "📦 저장 완료 - serial: $serial, barcode: $barcode")

                            // 화면에도 바로 반영
                            binding.etSerial.setText(serial)
                            binding.etBarcode.setText(barcode)
                        }
                    }
                } else {
                    Log.e("OCR", "imageFile is not initialized")
                }
            }
        }


        binding.clBtnOcr.setOnClickListener {

            when {
                // 권한이 있는지 확인

                // 권한이 있을 때
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 권한이 있으므로 액션 실행
                    goOcr()
                }
                // 왜 필요한지 한번도 설명
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.CAMERA) -> {
                    showDialog()
                }

                else -> {
                    ActivityCompat.requestPermissions(requireActivity(),
                        arrayOf(Manifest.permission.CAMERA),
                        204)

                    showDialog()
                }

            }


        }

    }

    private fun goOcr() {
        imageFile = File(requireContext().cacheDir, "ocr_image_${System.currentTimeMillis()}.jpg")
        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    //ocr
    private fun loadOcrResult() {
        val prefs = requireContext().getSharedPreferences("ocr_prefs", Context.MODE_PRIVATE)
        val serial = prefs.getString("ocr_serial", "")
        val barcode = prefs.getString("ocr_barcode", "")

        Log.d("OCR_SHARED_PREF", "🔎 불러온 값 - serial: $serial, barcode: $barcode")

        if (!serial.isNullOrEmpty()) {
            binding.etSerial.setText(serial)
            Log.d("OCR", "💡 Loaded serial from prefs: $serial")
        }
        if (!barcode.isNullOrEmpty()) {
            binding.etBarcode.setText(barcode)
            Log.d("OCR", "💡 Loaded barcode from prefs: $barcode")
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
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
        binding.clReceiveDate.setOnClickListener {

            showCustomCalendarDialog()
        }
    }

    private fun showCustomCalendarDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_calendar_dialog, null)
        val calendarView = dialogView.findViewById<CalendarView>(R.id.calendarView)
        val btnSelectDate = dialogView.findViewById<Button>(R.id.btnSelectDate)



        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val calendar = Calendar.getInstance()

// 오늘 날짜의 끝으로 설정 (오늘 23:59:59)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

// 최대 선택 날짜는 오늘까지
        calendarView.maxDate = calendar.timeInMillis


        var selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())


        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->

            val fixedMonth = month + 1
            selectedDate = String.format("%04d-%02d-%02d", year, fixedMonth, dayOfMonth)
            Log.d("Post", "showCustomCalendarDialog:$selectedDate ")


        }

        btnSelectDate.setOnClickListener {
            // 선택된 날짜를 처리하는 코드 작성
            // 예: 선택된 날짜를 TextView에 표시하거나 다른 로직 수행

            binding.tvDate.text = selectedDate.toString()
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = parser.parse(selectedDate)!!
            date = dateFormat.format(parsedDate)
            checkNext()
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        alertDialog.show()


// ✅ 가운데 정렬 및 너비 조절
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        alertDialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        alertDialog.window?.setGravity(Gravity.CENTER)
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
            ArrayAdapter(requireContext(), R.layout.item_spinner_dropdown, items)
        binding.atvModelName.setAdapter(adapter)

        binding.etSerial.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etBarcode.requestFocus() // 다음 EditText로 이동
                true // 이벤트 소비 (키보드 기본 동작 막음)
            } else {
                false
            }
        }




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

    private fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_step3_confirm)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvTitle1 = dialog.findViewById<TextView>(R.id.tv_description1)
        val tvTitle2 = dialog.findViewById<TextView>(R.id.tv_description2)
        tvTitle1.text= "사진 촬영을 위해 카메라 권한이 필요합니다."
        tvTitle2.text= "설정에서 카메라 권한을 허용해 주세요."


        // 닫기 버튼 클릭 리스너
        val cancelButton = dialog.findViewById<View>(R.id.btn_cancel)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }


        // 입력 완료하기 버튼 클릭 리스너
        val confirmButton = dialog.findViewById<TextView>(R.id.btn_confirm)
        confirmButton.text="변경하러 가기"
        confirmButton.setOnClickListener {
            navigateToAppSetting()
            dialog.dismiss()

        }

        dialog.show()
    }

    // 권한 설정 화면으로 이동하는 함수
    private fun navigateToAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
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
            viewModel.postThirdStage(mainViewModel.release.value?:false)
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

    override fun onDestroyView() {
        super.onDestroyView()
        clearBinding()
        viewModel.clearPostResponse()
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
