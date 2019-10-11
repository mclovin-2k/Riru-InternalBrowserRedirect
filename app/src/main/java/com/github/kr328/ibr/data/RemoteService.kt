package com.github.kr328.ibr.data

import com.github.kr328.ibr.Constants
import com.github.kr328.ibr.compat.SystemProperties
import com.github.kr328.ibr.remote.RemoteConnection

class RemoteService {
    companion object {
        const val REDIRECT_SERIVCE_STATUE_KEY = "sys.ibr.status"

        const val REDIRECT_SERVICE_STATUE_RIRU_LOADED = "riru_loaded"
        const val REDIRECT_SERVICE_STATUS_SYSTEM_SERVER_FORKED = "system_server_forked"
        const val REDIRECT_SERVICE_STATUS_INJECT_SUCCESS = "inject_success"
        const val REDIRECT_SERVICE_STATUS_SERVICE_CREATED = "service_created"
        const val REDIRECT_SERVICE_STATUS_RUNNING = "running"
    }

    enum class RCStatus {
        RUNNING,
        RIRU_NOT_LOADED,
        RIRU_NOT_CALL_SYSTEM_SERVER_FORKED,
        INJECT_FAILURE,
        SERVICE_NOT_CREATED,
        UNABLE_TO_HANDLE_REQUEST,
        SYSTEM_BLOCK_IPC,
        SERVICE_VERSION_NOT_MATCHES,
        UNKNOWN
    }

    fun getStatus(): RCStatus {
        return try {
            if (RemoteConnection.connection.version == Constants.REMOTE_SERVICE_VERSION)
                RCStatus.RUNNING
            else
                RCStatus.SERVICE_VERSION_NOT_MATCHES
        } catch (e: Exception) {
            when (SystemProperties.get(REDIRECT_SERIVCE_STATUE_KEY, "")) {
                "" -> RCStatus.RIRU_NOT_LOADED
                REDIRECT_SERVICE_STATUE_RIRU_LOADED ->
                    RCStatus.RIRU_NOT_CALL_SYSTEM_SERVER_FORKED
                REDIRECT_SERVICE_STATUS_SYSTEM_SERVER_FORKED ->
                    RCStatus.INJECT_FAILURE
                REDIRECT_SERVICE_STATUS_INJECT_SUCCESS ->
                    RCStatus.SERVICE_NOT_CREATED
                REDIRECT_SERVICE_STATUS_SERVICE_CREATED ->
                    RCStatus.UNABLE_TO_HANDLE_REQUEST
                REDIRECT_SERVICE_STATUS_RUNNING ->
                    RCStatus.SYSTEM_BLOCK_IPC
                else ->
                    RCStatus.UNKNOWN
            }
        }
    }
}