package com.example.smtpmonitoreo.di

import com.example.smtpmonitoreo.data.repository.AdminRepository
import com.example.smtpmonitoreo.data.repository.AdminRepositoryImpl
import com.example.smtpmonitoreo.data.repository.AuthRepository
import com.example.smtpmonitoreo.data.repository.AuthRepositoryImpl
import com.example.smtpmonitoreo.data.repository.MonitoringRepository
import com.example.smtpmonitoreo.data.repository.MonitoringRepositoryImpl
import com.example.smtpmonitoreo.ui.auth.AuthViewModel
import com.example.smtpmonitoreo.ui.dashboard.AdminViewModel
import com.example.smtpmonitoreo.ui.dashboard.AlcaldiaViewModel
import com.example.smtpmonitoreo.ui.dashboard.ChoferViewModel
import com.example.smtpmonitoreo.ui.dashboard.PasajeroViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dataModule = module {
    single<AuthRepository> { AuthRepositoryImpl() }
    single<MonitoringRepository> { MonitoringRepositoryImpl() }
    single<AdminRepository> { AdminRepositoryImpl() }
}

val viewModelModule = module {
    viewModelOf(::AuthViewModel)
    viewModelOf(::PasajeroViewModel)
    viewModelOf(::ChoferViewModel)
    viewModelOf(::AlcaldiaViewModel)
    viewModelOf(::AdminViewModel)
}

val appModules = listOf(dataModule, viewModelModule)