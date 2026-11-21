package com.example.smtpmonitoreo.di

import com.example.smtpmonitoreo.data.demo.DemoSeeder
import com.example.smtpmonitoreo.data.repository.AdminRepository
import com.example.smtpmonitoreo.data.repository.AdminRepositoryImpl
import com.example.smtpmonitoreo.data.repository.AuthRepository
import com.example.smtpmonitoreo.data.repository.AuthRepositoryImpl
import com.example.smtpmonitoreo.data.repository.MonitoringRepository
import com.example.smtpmonitoreo.data.repository.MonitoringRepositoryImpl
import com.example.smtpmonitoreo.data.routing.OsrmRoutingService
import com.example.smtpmonitoreo.data.tracking.LocationTracker
import com.example.smtpmonitoreo.data.tracking.ServiceLocationTracker
import com.example.smtpmonitoreo.domain.EtaCalculator
import com.example.smtpmonitoreo.domain.RoutingService
import com.example.smtpmonitoreo.ui.auth.AuthViewModel
import com.example.smtpmonitoreo.ui.dashboard.AdminViewModel
import com.example.smtpmonitoreo.ui.dashboard.AlcaldiaViewModel
import com.example.smtpmonitoreo.ui.dashboard.ChoferViewModel
import com.example.smtpmonitoreo.ui.dashboard.PasajeroViewModel
import com.example.smtpmonitoreo.ui.map.RoutePathsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
}

val dataModule = module {
    single<AuthRepository> { AuthRepositoryImpl(auth = get(), db = get()) }
    single<MonitoringRepository> { MonitoringRepositoryImpl(db = get(), auth = get()) }
    single<AdminRepository> { AdminRepositoryImpl(db = get(), auth = get()) }
    single { DemoSeeder(db = get()) }
    single<LocationTracker> { ServiceLocationTracker(androidContext()) }
    single<RoutingService> { OsrmRoutingService(androidContext()) }
    single { EtaCalculator(routing = get()) }
}

val viewModelModule = module {
    viewModelOf(::AuthViewModel)
    viewModelOf(::PasajeroViewModel)
    viewModelOf(::ChoferViewModel)
    viewModelOf(::AlcaldiaViewModel)
    viewModelOf(::AdminViewModel)
    viewModelOf(::RoutePathsViewModel)
}

val appModules = listOf(firebaseModule, dataModule, viewModelModule)