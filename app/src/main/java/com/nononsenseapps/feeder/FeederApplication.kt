package com.nononsenseapps.feeder

import android.app.Application
import android.util.Log
import org.conscrypt.Conscrypt
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.security.Security

@Suppress("unused")
class FeederApplication: Application() {
    init {
        // Install Conscrypt to handle missing SSL cyphers on older platforms
        Security.insertProviderAt(Conscrypt.newProvider(), 1)

        val dt = DateTime(BuildConfig.BUILD_TIME_UTC, DateTimeZone.UTC)

        Log.d("JONAS", """
            VersionCode: ${BuildConfig.VERSION_CODE}
            VersionName: ${BuildConfig.VERSION_NAME}
            SHA: ${BuildConfig.GIT_SHA}
            BuildDate: ${dt.toDateTimeISO()}
            """)
/*
        packageManager?.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)?.signatures?.forEach {
            ByteArrayInputStream(it.toByteArray()).use {
                val cert: X509Certificate = CertificateFactory.getInstance("X509").generateCertificate(it) as X509Certificate

                Log.d("JONAS", """
                    Subject: ${cert.subjectDN}
                    Issuer: ${cert.issuerDN}
                    Serial: ${cert.serialNumber}
                    """)
            }
        }*/
    }
}
