package com.jackzone.simpletimer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jackzone.simpletimer.dao.TimerDao
import com.jackzone.simpletimer.helper.Config
import com.jackzone.simpletimer.helper.Converters
import com.jackzone.simpletimer.model.Timer
import com.jackzone.simpletimer.model.TimerState
import java.util.concurrent.Executors

@Database(entities = [Timer::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun TimerDao(): TimerDao

    companion object {

        private var db: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            db?.let {
                return it
            }
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app.db")
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        insertDefaultTimer(context)
                    }
                })
                .build().apply { db = this }
        }

        private fun insertDefaultTimer(context: Context) {
            Executors.newSingleThreadScheduledExecutor().execute {
                val config = Config.newInstance(context.applicationContext)
                db!!.TimerDao().insertOrUpdateTimer(
                    Timer(
                        id = null,
                        seconds = config.timerSeconds,
                        state = TimerState.Idle,
                        vibrate = config.timerVibrate,
                        soundUri = config.timerSoundUri,
                        soundTitle = config.timerSoundTitle,
                        label = config.timerLabel ?: "",
                        createdAt = System.currentTimeMillis(),
                        channelId = config.timerChannelId,
                    )
                )
            }
        }
    }
}