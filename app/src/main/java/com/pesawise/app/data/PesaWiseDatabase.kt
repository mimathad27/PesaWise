package com.pesawise.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserAccount::class], version = 1, exportSchema = false)
abstract class PesaWiseDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var instance: PesaWiseDatabase? = null

        fun getInstance(context: Context): PesaWiseDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PesaWiseDatabase::class.java,
                "pesawise.db"
            ).build().also { instance = it }
        }
    }
}
