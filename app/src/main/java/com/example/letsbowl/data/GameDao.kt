package com.example.letsbowl.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    //하나의 게임 객체를 DB에 삽입
    //suspend 키워드를 통해 비동기 환경에서 실행
    @Insert
    suspend fun insertGame(game: Game)

    //game_table에 저장된 모든 게임을 조회
    //저장된 날자를 기준으로 내림차순 정렬
    @Query("SELECT * From games_table ORDER BY date_millis DESC")
    fun getAllGames(): Flow<List<Game>>

    //game_table에 저장된 최고 점수 조회
    @Query("SELECT MAX(score) FROM games_table")
    fun getHighScore(): Flow<Int>
}