package com.gokuxedition.arrowlogic

import android.app.Application
import com.gokuxedition.arrowlogic.data.local.ArrowLogicDatabase
import com.gokuxedition.arrowlogic.data.preferences.UserPreferencesRepository
import com.gokuxedition.arrowlogic.data.repository.ArrowLogicRepository

class ArrowLogicApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    private val database = ArrowLogicDatabase.getInstance(application)

    val gameRepository = ArrowLogicRepository(database.gameDataDao())
    val userPreferencesRepository = UserPreferencesRepository(application)
}
