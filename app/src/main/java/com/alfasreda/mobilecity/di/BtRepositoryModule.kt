package com.alfasreda.mobilecity.di

import com.alfasreda.mobilecity.BuildConfig
import com.alfasreda.mobilecity.repositories.bt.BtRepository
import com.alfasreda.mobilecity.repositories.bt.MockBtRepository

object BtRepositoryModule {

    fun provide(): BtRepository {
        return if (BuildConfig.DATA_SOURCE == "NETWORK") BtRepository() else MockBtRepository()
    }
}