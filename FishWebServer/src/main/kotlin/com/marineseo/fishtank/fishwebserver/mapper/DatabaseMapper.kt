package com.marineseo.fishtank.fishwebserver.mapper

import com.marineseo.fishtank.fishwebserver.model.Task
import com.marineseo.fishtank.fishwebserver.model.Temperature
import com.marineseo.fishtank.fishwebserver.model.User
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import java.sql.Date
import java.sql.Timestamp

const val TB_USER = "user"
const val COL_USER_ID = "id"
const val COL_USER_NAME = "name"
const val COL_USER_PASSWORD = "password"

const val TB_TEMPERATURE = "temperature"
const val COL_TEMP_ID = "id"
const val COL_TEMP_TEMPERATURE = "temperature"
const val COL_TEMP_TIME = "time"

@Mapper
interface DatabaseMapper {

    @Select("SELECT * FROM $TB_USER WHERE id=#{userId}")
    fun getUser(@Param("userId") id: String): User

    @Insert("INSERT INTO $TB_TEMPERATURE($COL_TEMP_TEMPERATURE, $COL_TEMP_TIME) VALUES(#{temperature},#{time})")
    fun insertTemperature(@Param("temperature") temperature: Temperature)

    @Select("SELECT * FROM $TB_TEMPERATURE WHERE $COL_TEMP_TIME BETWEEN #{from} AND #{until}")
    fun fetchTemperature(@Param("from") from: Date, @Param("until") until: Date): List<Temperature>

    @Insert( "INSERT INTO ${Task.TB_TASK}" +
            "(${Task.COL_USER_ID}, ${Task.COL_TYPE}, " +
            "${Task.COL_DATA},${Task.COL_EXECUTE_TIME}, ${Task.COL_STATE}" +
            ")" +
            "VALUES(#{userId},#{type},#{data},#{executeTime},#{state})")
    fun insertTask(@Param("task") task: Task)

    @Select(
        "SELECT * FROM ${Task.TB_TASK} " +
                "WHERE ${Task.COL_STATE}=${Task.STATE_STANDBY} AND ${Task.COL_EXECUTE_TIME} < #{time}" +
                "ORDER BY ${Task.COL_EXECUTE_TIME} ASC " +
                "LIMIT 1"
    )
    fun fetchTask(@Param("time") timeStamp: Timestamp): Task?

    @Select(
        "SELECT * FROM ${Task.TB_TASK} " +
                "WHERE ${Task.COL_TYPE}=${Task.TYPE_REPLACE_WATER} " +
                "ORDER BY ${Task.COL_EXECUTE_TIME} DESC " +
                "LIMIT 1"
    )
    fun getLastReplaceTask(): Task?

    @Update(
        "UPDATE ${Task.TB_TASK} " +
                "SET ${Task.COL_TYPE}=#{type}, ${Task.COL_STATE}=#{state}, ${Task.COL_DATA}=#{data}, " +
                "${Task.COL_USER_ID}=#{userId}, ${Task.COL_EXECUTE_TIME}=#{executeTime} " +
                "WHERE ${Task.COL_ID}=#{id}"
    )
    fun updateTask(@Param("task") task: Task)
}