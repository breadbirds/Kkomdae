package com.pizza.kkomdae.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.pizza.kkomdae.R
import com.pizza.kkomdae.ui.step3.FinalResultFragment
import com.pizza.kkomdae.ui.step4.Step4AiResultFragment

class LoadingFragment : Fragment() {

    // 진행 상태와 관련된 변수
    private var currentProgress = 0
    private var currentStage = 0

    // UI 컴포넌트
    private lateinit var tv_title: TextView
    private lateinit var tv_subtitle: TextView
    private lateinit var tv_progress: TextView
    private lateinit var tv_progress_percent: TextView
    private lateinit var tv_progress_time: TextView
    private lateinit var iv_character: ImageView

    // 로딩 프로그레스바 및 체크 이미지
    private lateinit var progress1: ProgressBar
    private lateinit var progress2: ProgressBar
    private lateinit var progress3: ProgressBar
    private lateinit var check1: ImageView
    private lateinit var check2: ImageView
    private lateinit var check3: ImageView

    private lateinit var btn_next: Button

    // 제목
    private val stageTitles = arrayOf(
        "사진을 준비하고 있어요",
        "꼼꼼히 살펴보는 중이에요",
        "거의 다 왔어요!",
        "점검 완료!"
    )

    private val stageSubtitles = arrayOf(
        "점검을 시작할게요!",
        "조금만 기다려주세요",
        "흠집과 스크래치를 찾고있어요",
        "결과를 확인하러 가볼까요?"
    )

    // 핸들러 - 주기적으로 진행률 업데이트
    private val handler = Handler(Looper.getMainLooper())

    // 진행 속도 설정 (ms 단위)
    private val updateInterval = 100L

    // 이전 진행 단계 추적
    private var previousStage = 0

    // 진행률 업데이트 Runnable
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            // 진행이 100%가 될 때까지 계속 업데이트
            if (currentProgress < 100) {
                handler.postDelayed(this, updateInterval)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 레이아웃 inflate
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI 요소 초기화
        tv_title = view.findViewById(R.id.tv_title)
        tv_subtitle = view.findViewById(R.id.tv_subtitle)
        tv_progress = view.findViewById(R.id.tv_progress)
        tv_progress_percent = view.findViewById(R.id.tv_progress_percent)
        tv_progress_time = view.findViewById(R.id.tv_progress_time)
        iv_character = view.findViewById(R.id.iv_character)

        // 로딩 및 체크 이미지 초기화
        progress1 = view.findViewById(R.id.progress1)
        progress2 = view.findViewById(R.id.progress2)
        progress3 = view.findViewById(R.id.progress3)
        check1 = view.findViewById(R.id.check1)
        check2 = view.findViewById(R.id.check2)
        check3 = view.findViewById(R.id.check3)

        btn_next = view.findViewById(R.id.btn_next)

        // 버튼 초기에는 비활성화
        btn_next.visibility = View.GONE

        // 버튼 클릭 리스너 설정
        btn_next.setOnClickListener {
            val resultFragment = Step4AiResultFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fl_main, resultFragment)
                .addToBackStack(null)
                .commit()
        }

        // 로딩 시작
        startLoading()
    }

    private fun startLoading() {
        // 초기 상태 설정
        currentStage = 0
        currentProgress = 0
        previousStage = 0

        // 현재 단계 UI 업데이트
        updateStageUI()

        // 진행 업데이트 시작
        handler.post(updateRunnable)
    }

    private fun updateProgress() {
        // 진행률 증가 (속도 조절)
        currentProgress += 1

        // 단계별 처리
        when {
            // 첫 번째 단계 (0-35%)
            currentProgress <= 35 -> {
                tv_progress_percent.text = "${currentProgress}%"
                tv_progress_time.text = " (3분 소요 예상)"

                // 35%에 도달하면 첫 번째 항목 체크 및 단계 변경
                if (currentProgress == 35) {
                    setCheckComplete(1)
                    moveToNextStage()
                }
            }

            // 두 번째 단계 (36-70%)
            currentProgress <= 70 -> {
                tv_progress_percent.text = "${currentProgress}%"
                tv_progress_time.text = " (2분 소요 예상)"

                // 70%에 도달하면 두 번째 항목 체크 및 단계 변경
                if (currentProgress == 70) {
                    setCheckComplete(2)
                    moveToNextStage()
                }
            }

            // 세 번째 단계 (71-100%)
            currentProgress <= 100 -> {
                tv_progress_percent.text = "${currentProgress}%"
                tv_progress_time.text = " (1분 소요 예상)"

                // 100%에 도달하면 세 번째 항목 체크 및 완료 처리
                if (currentProgress == 100) {
                    setCheckComplete(3)
                    moveToNextStage()
                    completeLoading()
                }
            }
        }
    }

    // 항목 체크 완료 표시
    private fun setCheckComplete(index: Int) {
        when(index) {
            1 -> {
                progress1.visibility = View.INVISIBLE
                check1.visibility = View.VISIBLE
            }
            2 -> {
                progress2.visibility = View.INVISIBLE
                check2.visibility = View.VISIBLE
            }
            3 -> {
                progress3.visibility = View.INVISIBLE
                check3.visibility = View.VISIBLE
            }
        }
    }

    private fun moveToNextStage() {
        // 현재 단계 저장
        previousStage = currentStage

        // 현재 단계 변경 (정방향으로 진행)
        currentStage++
        if (currentStage > 3) currentStage = 3

        // 단계가 실제로 변경되었을 때만 UI 업데이트
        if (previousStage != currentStage) {
            updateStageUI()
        }
    }

    private fun updateStageUI() {
        // 현재 단계에 맞는 UI 업데이트
        tv_title.text = stageTitles[currentStage]
        tv_subtitle.text = stageSubtitles[currentStage]

        // 단계에 따라 캐릭터 이미지 변경
        // 단계에 따라 GIF 이미지 로드
        when (currentStage) {
            0 -> Glide.with(this)
                .asGif()
                .load(R.raw.loading1)
                .into(iv_character)
            1 -> Glide.with(this)
                .asGif()
                .load(R.raw.loading2)
                .into(iv_character)
            2 -> Glide.with(this)
                .asGif()
                .load(R.raw.loading3)
                .into(iv_character)
            3 -> iv_character.setImageResource(R.drawable.ic_loading_final)
        }

        // 로그
        println("Stage updated to: $currentStage with GIF image: ${when(currentStage) {
            0 -> "loading1"
            1 -> "loading2"
            2 -> "loading3"
            3 -> "ic_loading_final"
            else -> "unknown"
        }}")
    }

    private fun completeLoading() {
        tv_progress.text = "점검 완료! "
        tv_progress_percent.text = "100%"
        tv_progress_time.text = ""

        // 명시적으로 최종 단계 이미지로 설정
        iv_character.setImageResource(R.drawable.ic_loading_final)

        // 단계 명시적 설정
        currentStage = 3
        tv_title.text = stageTitles[3]
        tv_subtitle.text = stageSubtitles[3]

        // 버튼 활성화 (약간 지연시켜 표시)
        handler.postDelayed({
            btn_next.visibility = View.VISIBLE
        }, 300)
    }

    override fun onDestroy() {
        // 핸들러 콜백 제거 (메모리 누수 방지)
        handler.removeCallbacks(updateRunnable)
        super.onDestroy()
    }
}