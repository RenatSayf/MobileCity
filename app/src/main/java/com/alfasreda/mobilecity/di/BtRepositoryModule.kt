package com.alfasreda.mobilecity.di

import com.alfasreda.mobilecity.BuildConfig
import com.alfasreda.mobilecity.repositories.bt.BtRepository
import com.alfasreda.mobilecity.repositories.bt.MockBtRepository
import com.alfasreda.mobilecity.utils.DataSource

object BtRepositoryModule {

    fun provide(): BtRepository {
        return if (BuildConfig.DATA_SOURCE == DataSource.NETWORK) BtRepository() else MockBtRepository()
    }
}