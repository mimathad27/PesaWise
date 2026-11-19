package com.pesawise.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [UserAccount::class, ExpenseEntity::class], version = 2, exportSchema = false)
abstract class PesaWiseDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao

    private class Migrations {
        companion object {
            val MIGRATION_1_2 = object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS expenses (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "userId TEXT NOT NULL, " +
                            "title TEXT NOT NULL, " +
                            "amount REAL NOT NULL, " +
                            "category TEXT NOT NULL, " +
                            "dateMillis INTEGER NOT NULL, " +
                            "currencyCode TEXT NOT NULL)"
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_userId ON expenses(userId)")
                }
            }
        }
    }

    companion object {
        @Volatile private var instance: PesaWiseDatabase? = null

        fun getInstance(context: Context): PesaWiseDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PesaWiseDatabase::class.java,
                "pesawise.db"
            ).addMigrations(Migrations.MIGRATION_1_2).build().also { instance = it }
        }
    }
}
