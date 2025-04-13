package com.pizza.kkomdae.ui.step3

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.databinding.FragmentFinalResultBinding
import com.pizza.kkomdae.presenter.viewmodel.FinalViewModel
import com.pizza.kkomdae.ui.SubmitCompleteFragment


class FinalResultFragment : BaseFragment<FragmentFinalResultBinding>(
    FragmentFinalResultBinding::bind,
    R.layout.fragment_final_result
) {

    // ViewModel 연결
    private val viewModel : FinalViewModel by activityViewModels()
    // 시스템 백 버튼 콜백 선언
    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 시스템 백 버튼 동작 설정
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 시스템 백 버튼 클릭 시 바텀시트 동작
                showQuitBottomSheet()
            }
        }


        // 콜백 등록
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 안내 다이얼로그 표시
        showIntroDialog()

        // 상단 타이틀 설정
        binding.tvTitle.text = "제출 내용 확인"

        // 각 step layout 애니메이션 순차 적용
        binding.clStep1.slideInFromLeft(0L)
        binding.clStep2.slideInFromLeft(100L)
        binding.clStep3.slideInFromLeft(200L)
        binding.clStep4.slideInFromLeft(350L)

        binding.btnCancel.setOnClickListener {
            showQuitBottomSheet()
        }

        // 이미지 ViewPager 어댑터 설정
        val adapter = FinalResultAdapter(requireContext())
        binding.viewPager.adapter = adapter

        // 서버에서 최종 결과 요청
        viewModel.getLaptopTotalResult()


        // 결과 수신 후 UI 세팅
        setData(adapter)



        // 페이지 변경 감지
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 인디케이터 업데이트
                setupIndicators(position)
            }
        })

        // 제출하기 버튼 클릭 시 다이얼로그 표시
        binding.btnSubmit.setOnClickListener {
            showSubmitDialog()
        }



        // PDF 제출 성공 시 제출 완료 페이지로 이동
        goToNext()

        // 결과 실패 했을때
        viewModel.pdfNameResponse.observe(viewLifecycleOwner){
            it ?: return@observe
            if(it==true){
                showErrorDialog()
            }
        }

//        showErrorDialog()

    }

    // 에러 다이얼로그
    private fun showErrorDialog() {

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_error_dialog)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val confirmButton = dialog.findViewById<TextView>(R.id.tv_confirm)
        val errorText = dialog.findViewById<TextView>(R.id.tv_error_message)
        val errorTitleText = dialog.findViewById<TextView>(R.id.tv_error_title)
//
        errorTitleText.text="제출을 실패하였습니다."
        errorText.text="서버 오류로 인한 제출 실패입니다."
//
        confirmButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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

            // UI 스레드에서 약간의 지연 후 화면 전환
            view?.post {
                try {
                    // 메인 화면으로 이동
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.setReorderingAllowed(true)
                    transaction.replace(R.id.fl_main, com.pizza.kkomdae.ui.MainFragment())
                    transaction.commit()

                    // 백스택 즉시 비우기
                    requireActivity().supportFragmentManager.popBackStackImmediate(null, 1)
                } catch (e: Exception) {
                    Log.e("Step1GuideFragment", "MainFragment로 이동 중 오류: ${e.message}", e)
                }
            }
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    // PDF 제출 완료 신호 수신 시 제출완료 화면으로 이동
    private fun goToNext() {
        viewModel.pdfName.observe(viewLifecycleOwner) {
            it ?: return@observe
            // 제출완료 화면으로 전환
            val submitCompleteFragment = SubmitCompleteFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.fl_main, submitCompleteFragment)
                .commit()
        }
    }

    // 서버에서 전달된 최종 결과 데이터를 UI에 반영
    private fun setData(adapter: FinalResultAdapter) {

        viewModel.getFinalResult.observe(viewLifecycleOwner) {
            it ?: return@observe
            binding.apply {



                // 외관 ai 분석 결과
                adapter.submitList(it.imageUrls)

                viewPager.post {
                    if (adapter.itemCount > 0) {
                        setupIndicators(0) // 처음은 보통 0으로 초기화
                    }
                }


                // 자가 진단 결과
                // 키보드
                if (!it.keyboardStatus) {
                    ivKeyboard.setImageResource(R.drawable.ic_fail)
                }

                // usb
                if (!it.useStatus) {
                    ivUsb.setImageResource(R.drawable.ic_fail)
                }

                // 카메라
                if (!it.cameraStatus) {
                    ivCamera.setImageResource(R.drawable.ic_fail)
                }

                // 배터리 성능
                if (!it.batteryStatus) {
                    ivBattery.setImageResource(R.drawable.ic_fail)
                }

                // 충전기
                if (!it.chargerStatus) {
                    ivCharger.setImageResource(R.drawable.ic_fail)
                }

                // 노트북 정보
                tvInputModelName.text = it.modelCode  // 모델명
                tvInputSerial.text = it.serialNum     // 시리얼 번호
                tvInputBarcode.text = it.barcodeNum   // 바코드 번호
                tvInputDate.text = it.date            // 수령일자

                // 구성품 수량
                tvInputLaptopCount.text = it.laptopCount.toString()     // 노트북
                tvInputMouseCount.text = it.mouseCount.toString()       // 마우스
                tvInputAdapterCount.text = it.adapterCount.toString()    // 어댑터
                tvInputPowerCount.text = it.powerCableCount.toString()    // 전원선
                tvInputBagCount.text = it.bagCount.toString()           // 가방
                tvInputPadCount.text = it.mousepadCount.toString()      // 마우스 패드

                // 비고 사항
                if (it.description==""){
                    tvFrontTitle.text = "특이사항이 없습니다."
                }else{
                    tvFrontTitle.text = it.description
                }


                // 데이터가 로드되고 어댑터에 설정된 후 인디케이터를 초기화

            }
        }
    }


    // 이미지 인디케이터를 설정
    private fun setupIndicators(position: Int) {
        // 인디케이터 컨테이너 초기화
        binding.indicatorContainer.removeAllViews()

        // 이미지 개수만큼 인디케이터 추가
        val imageCount = 6

        // 인디케이터 점들 추가
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(8, 0, 8, 0)

        for (i in 0 until imageCount) {
            val dot = ImageView(requireContext())
            dot.layoutParams = params

            // 현재 위치는 선택된 점, 나머지는 선택되지 않은 점으로 표시
            if (i == position) {
                dot.setImageResource(R.drawable.indicator_dot_selected)
            } else {
                dot.setImageResource(R.drawable.indicator_dot_unselected)
            }

            binding.indicatorContainer.addView(dot)
        }
    }

    // 제출하기 버튼 클릭 시 표시되는 다이얼로그
    private fun showSubmitDialog() {
        // 다이얼로그 생성
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_submit_confirm)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 취소 버튼
        val btnCancel = dialog.findViewById<TextView>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // 제출하기 버튼
        val btnConfirm = dialog.findViewById<TextView>(R.id.btn_confirm)
        btnConfirm.setOnClickListener {
            // 다이얼로그 닫기
            dialog.dismiss()

            // api호출
            viewModel.postPdf()


        }

        dialog.show()
    }


    // 화면 진입 시 처음 보여주는 안내 다이얼로그
    private fun showIntroDialog() {
        // 다이얼로그 생성
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_final_result)

        // 다이얼로그 배경 투명하게
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 너비 : 화면 너비의 90%
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 다이얼로그 확인 버튼 클릭 이벤트
        val confirmButton = dialog.findViewById<Button>(R.id.btn_confirm)
        confirmButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    // step별 왼쪽에서 부드럽게 등장하는 애니메이션
    fun View.slideInFromLeft(delay: Long = 0L, duration: Long = 700L) {
        translationX = 800f // 왼쪽 바깥에서 시작
        alpha = 0f
        postDelayed({
            animate()
                .translationX(0f)
                .alpha(1f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(duration)
                .start()
        }, delay)
    }

    // View 소멸 시 ViewModel 값 초기화 및 바인딩 해제
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearPostFinal()
        clearBinding()
    }


}