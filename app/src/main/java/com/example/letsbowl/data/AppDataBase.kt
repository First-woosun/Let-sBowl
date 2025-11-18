package com.example.letsbowl.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Game::class], version = 1, exportSchema = false)
abstract class AppDataBase : RoomDatabase(){
    abstract fun gameDao(): GameDao

    //앱 전체에서 데이터베이스의 인스턴스를 하나만 생성하여 공유(싱글톤)
    companion object{
        @Volatile
        private  var INSTANCE: AppDataBase? = null

        //데이터베이스 인스턴스를 반환하는 메서드
        fun getDataBase(context: Context): AppDataBase{
            //이미 인스턴스가 있으면 즉시 반환
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "bowling_database"
                )
                .build()

                INSTANCE = instance

                instance
            }
        }
    }
}