package com.example.letsbowl.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

// 게임 데이터를 저장하기위한 클래스
@Entity(tableName = "games_table")
data class Game(
    
    //각 게임을 식별하기위한 고유 ID
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    //게임을 한 날자
    @ColumnInfo(name = "date_millis")
    val dateMillis: Long,

    //해당 게임의 점수
    @ColumnInfo(name = "score")
    val score: Int
)