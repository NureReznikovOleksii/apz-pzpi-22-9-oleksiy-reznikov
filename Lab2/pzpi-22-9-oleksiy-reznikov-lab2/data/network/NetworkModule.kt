package com.electricmonitor.mobile.data.network

import android.content.Context
import android.content.SharedPreferences
import com.electricmonitor.mobile.data.api.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    // For Android Emulator: 10.0.2.2 maps to host machine's localhost
    // For Real Device: Replace with your machine's actual IP address (e.g., "http://192.168.1.100:3000/")
    // For Production: Replace with your server's domain (e.g., "https://your-api.com/")
    private const val BASE_URL = "http://10.0.2.2:3000/"

    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    private var retrofit: Retrofit? = null
    private var authToken: String? = null

    fun initialize(context: Context) {
        val sharedPrefs = context.getSharedPreferences("ElectricMonitorPrefs", Context.MODE_PRIVATE)
        authToken = sharedPrefs.getString("auth_token", null)

        retrofit = createRetrofit(context)
    }

    fun setAuthToken(token: String, context: Context) {
        authToken = token
        val sharedPrefs = context.getSharedPreferences("ElectricMonitorPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("auth_token", token).apply()

        // Recreate retrofit with new token
        retrofit = createRetrofit(context)
    }

    fun clearAuthToken(context: Context) {
        authToken = null
        val sharedPrefs = context.getSharedPreferences("ElectricMonitorPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().remove("auth_token").apply()

        // Recreate retrofit without token
        retrofit = createRetrofit(context)
    }

    private fun createRetrofit(context: Context): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()

        val okHttpClient = createOkHttpClient(context)

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun createOkHttpClient(context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)

        // Add auth interceptor
        builder.addInterceptor(createAuthInterceptor())

        // Add logging interceptor for debug builds
        if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addNetworkInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()

            if (authToken != null) {
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $authToken")
                    .build()
                chain.proceed(newRequest)
            } else {
                chain.proceed(originalRequest)
            }
        }
    }

    fun getAuthApi(): AuthApi {
        return retrofit?.create(AuthApi::class.java)
            ?: throw IllegalStateException("NetworkModule not initialized")
    }

    fun getDeviceApi(): DeviceApi {
        return retrofit?.create(DeviceApi::class.java)
            ?: throw IllegalStateException("NetworkModule not initialized")
    }

    fun getPowerDataApi(): PowerDataApi {
        return retrofit?.create(PowerDataApi::class.java)
            ?: throw IllegalStateException("NetworkModule not initialized")
    }

    fun getAlertApi(): AlertApi {
        return retrofit?.create(AlertApi::class.java)
            ?: throw IllegalStateException("NetworkModule not initialized")
    }

    // Helper function to get current base URL (useful for debugging)
    fun getBaseUrl(): String = BASE_URL
}

// Extension function to handle common network errors
fun <T> retrofit2.Response<T>.handleResponse(): Result<T> {
    return try {
        if (isSuccessful) {
            body()?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Empty response body"))
        } else {
            val errorMsg = errorBody()?.string() ?: "Unknown error"
            Result.failure(NetworkException(code(), errorMsg))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class NetworkException(val code: Int, message: String) : Exception(message)

// Network state checking
class NetworkUtils {
    companion object {
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as android.net.ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

            return networkCapabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
}