package com.example.readerqr

import android.app.Activity
import android.app.Notification
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.net.MalformedURLException
import java.net.URL
import java.util.jar.Manifest

class QRActivity : AppCompatActivity(),ZXingScannerView.ResultHandler {

    private val PERMISO_CAMERA: Int = 1
    private var scannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerView = ZXingScannerView(this)
        setContentView(scannerView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checarPermiso()){
                if (scannerView == null){
                    scannerView?.setResultHandler { this }
                    scannerView?.startCamera()
                }

            }else{
                solicitarPermiso()
            }
        }


    }

    fun checarPermiso(): Boolean{
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    private fun solicitarPermiso(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), PERMISO_CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISO_CAMERA -> {
                if(grantResults.isNotEmpty()){
                    if (grantResults[0]!= PackageManager.PERMISSION_GRANTED){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
                                AlertDialog.Builder(this)
                                    .setTitle("Permiso requerido")
                                    .setMessage("Se necesita acceder a la cámara para leer los códigod QR")
                                    .setPositiveButton("Acepater", DialogInterface.OnClickListener{ dialog, which ->
                                        solicitarPermiso()
                                    }).setNegativeButton("", DialogInterface.OnClickListener{dialog, which ->
                                        dialog.dismiss()
                                        finish()
                                    })
                                    .create()
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun handleResult(p0: Result?) {
        val scanResult = p0?.text
        Log.d("", scanResult!!)

        try {
            val url = URL(scanResult)
            val i = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(scanResult))
            startActivity(i)
            finish()

        }catch (e: MalformedURLException){
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("QR no valido")
                .setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    finish()
                })
                .create()
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerView = null
    }


}