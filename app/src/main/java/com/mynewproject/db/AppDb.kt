package com.mynewproject.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mynewproject.dao.PostDao
import com.mynewproject.entity.PostEntity

@Database(entities = [PostEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDb =
            Room.databaseBuilder(context, AppDb::class.java, "app.db")
                .build()
    }
}