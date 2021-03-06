package com.github.kr328.ibr.remote

import android.content.Context
import android.os.Parcel
import android.os.RemoteException
import android.util.Log
import com.github.kr328.ibr.Constants
import com.github.kr328.ibr.compat.ServiceManager
import com.github.kr328.ibr.compat.SystemProperties
import com.github.kr328.ibr.remote.shared.*


object RemoteConnection {
    private const val REDIRECT_SERVICE_STATUE_KEY = "sys.ibr.status"

    private const val REDIRECT_SERVICE_STATUE_RIRU_LOADED = "riru_loaded"
    private const val REDIRECT_SERVICE_STATUS_SYSTEM_SERVER_FORKED = "system_server_forked"
    private const val REDIRECT_SERVICE_STATUS_INJECT_SUCCESS = "inject_success"
    private const val REDIRECT_SERVICE_STATUS_SERVICE_CREATED = "service_created"
    private const val REDIRECT_SERVICE_STATUS_RUNNING = "running"

    val connection: IRemoteService
        get() {
            if (privateConnection.version != SharedVersion.VERSION_INT)
                throw RemoteException("Invalid service version")
            return privateConnection
        }

    private val privateConnection: IRemoteService by lazy {
        openRemoteConnection()
    }

    private fun openRemoteConnection(): IRemoteService {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()

        try {
            val activity = ServiceManager.getService(Context.ACTIVITY_SERVICE)

            data.writeInterfaceToken(IRemoteService::class.java.name)

            activity.transact(ServiceHandle.SERVER, data, reply, 0)

            return IRemoteService.Stub.asInterface(reply.readStrongBinder())
                    ?: throw RemoteException("Unable to connect RemoteService")
        } finally {
            data.recycle()
            reply.recycle()
        }
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

    fun currentStatus(): RCStatus {
        return try {
            if (privateConnection.version == SharedVersion.VERSION_INT)
                RCStatus.RUNNING
            else
                RCStatus.SERVICE_VERSION_NOT_MATCHES
        } catch (e: Exception) {
            when (SystemProperties.get(REDIRECT_SERVICE_STATUE_KEY, "")) {
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


