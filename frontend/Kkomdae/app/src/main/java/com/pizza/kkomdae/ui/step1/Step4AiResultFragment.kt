package com.pizza.kkomdae.ui.step1

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pizza.kkomdae.R
import com.pizza.kkomdae.base.BaseFragment
import com.pizza.kkomdae.presenter.model.Step4AiResult
import com.pizza.kkomdae.ui.guide.Step2GuideFragment
import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.pizza.kkomdae.MainActivity
import com.pizza.kkomdae.databinding.FragmentStep4AiResultBinding
import com.pizza.kkomdae.presenter.viewmodel.MainViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "Step1ResultFragment"

