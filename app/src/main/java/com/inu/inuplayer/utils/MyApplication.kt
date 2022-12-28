package com.inu.inuplayer.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.widget.Toast
import com.inu.inuplayer.R
import com.inu.inuplayer.repository.Repository
import com.inu.inuplayer.repository.db.MusicRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.acra.BuildConfig
import org.acra.config.mailSender
import org.acra.config.toast
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

class MyApplication : Application(){

    init{
        instance = this

    }

    companion object {
        lateinit var instance: MyApplication
        fun applicationContext() : Context {
            return instance.applicationContext
        }

        val database by lazy { MusicRoomDatabase.getInstance(instance.applicationContext) }
        val repository by lazy { database?.let { Repository(it.musicDao()) } }
    }

    @SuppressLint("Range")
    override fun attachBaseContext(base:Context) {
        super.attachBaseContext(base)

        initAcra {
            //core configuration:
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            //each plugin you chose above can be configured in a block like this:
            toast {
                text = getString(R.string.acra_toast_text)
                length = Toast.LENGTH_SHORT
                //opening this block automatically enables the plugin.
            }
/*

            httpSender {
                //required. Https recommended
                uri = "https://your.server.com/report"
                //optional. Enables http basic auth
                basicAuthLogin = "acra"
                //required if above set
                basicAuthPassword = "password"
                // defaults to POST
                httpMethod = HttpSender.Method.POST
                //defaults to 5000ms
                connectionTimeout = 5000
                //defaults to 20000ms
                socketTimeout = 20000
                // defaults to false
                dropReportsOnTimeout = false
                //the following options allow you to configure a self signed certificate
//                keyStoreFactoryClass = MyKeyStoreFactory::class.java
//                certificatePath = "asset://mycert.cer"
//                resCertificate = R.raw.mycert
                certificateType = "X.509"
                //defaults to false. Recommended if your backend supports it
                compress = false
                //defaults to all
                tlsProtocols = arrayOf(TLS.V1_3, TLS.V1_2, TLS.V1_1, TLS.V1)
            }
*/

            mailSender {
                //required
                mailTo = "isson33@naver.com"
                //defaults to true
                reportAsFile = true
                //defaults to ACRA-report.stacktrace
                reportFileName = "Crash.txt"
                //defaults to "<applicationId> Crash Report"
                subject = getString(R.string.mail_subject)
                //defaults to empty
                body = getString(R.string.mail_body)
            }
        }
    }

}