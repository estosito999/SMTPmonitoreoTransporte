package com.example.smtpmonitoreo.di

import com.example.smtpmonitoreo.data.repository.AuthRepository
import com.example.smtpmonitoreo.data.repository.AuthRepositoryImpl
import com.example.smtpmonitoreo.data.repository.MonitoringRepository
import com.example.smtpmonitoreo.data.repository.MonitoringRepositoryImpl
import com.example.smtpmonitoreo.ui.auth.AuthViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.example.smtpmonitoreo.ui.dashboard.PasajeroViewModel
import com.example.smtpmonitoreo.ui.dashboard.ChoferViewModel
import com.example.smtpmonitoreo.ui.dashboard.AlcaldiaViewModel

val dataModule = module {
    single<AuthRepository> { AuthRepositoryImpl() }
    single<MonitoringRepository> { MonitoringRepositoryImpl() }
}

val viewModelModule = module {
    viewModelOf(::AuthViewModel)
    viewModelOf(::PasajeroViewModel)
    viewModelOf(::ChoferViewModel)
    viewModelOf(::AlcaldiaViewModel)
    // 2E: Admin
}

val appModules = listOf(dataModule, viewModelModule)