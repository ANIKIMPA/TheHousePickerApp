package com.design2net.the_house.activity

import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.design2net.the_house.R
import com.design2net.the_house.interfaces.BarcodeListener
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKManager.EMDKListener
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.Scanner.StatusListener
import com.symbol.emdk.barcode.*
import com.symbol.emdk.barcode.ScannerException
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

abstract class BarcodeScannerActivity: BaseActivity(), EMDKListener, StatusListener, Scanner.DataListener, BarcodeListener {

    private var emdkManager: EMDKManager? = null
    private var barcodeManager: BarcodeManager? = null
    private var scanner: Scanner? = null
    private var scannerSetupCompleted = false

    private fun initializeEmdkManager() {
        try {
            val results = EMDKManager.getEMDKManager(applicationContext, this)
            if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
                Toast.makeText(this, "EMDKManager Request Failed", Toast.LENGTH_LONG).show()
            }
        }
        catch(e: Exception) { e.printStackTrace() }
    }
    private fun destroyEmdkManager() {
        try {
            if (scanner != null) {
                // releases the scanner hardware resources for other application
                // to use. You must call this as soon as you're done with the
                // scanning.
                scanner!!.removeDataListener(this)
                scanner!!.removeStatusListener(this)
                scanner!!.disable()
                scanner = null

                scannerSetupCompleted = false
            }
        } catch (e: ScannerException) {
            e.printStackTrace()
        }

        try {
            if (emdkManager != null) {
                emdkManager!!.release()
                emdkManager = null
            }
        } catch (e: ScannerException) {
            e.printStackTrace()
        }
    }

    override fun onStatus(statusData: StatusData?) {
        if(statusData == null) return

        doAsync {
            // Different states of Scanner
            when (statusData.state) {
                // Scanner is IDLE
                StatusData.ScannerStates.IDLE -> {
                    if(scanner == null || ! scannerSetupCompleted) return@doAsync
                    try {
                        scanner!!.read()
                    }
                    catch(e: Exception) {}
                }
                else -> {}
            }
        }
    }
    override fun onOpened(emdkManager: EMDKManager?) {
        this.emdkManager = emdkManager
        initializeScanner()
    }
    override fun onClosed() {
        if (this.emdkManager != null) {
            this.emdkManager!!.release()
            this.emdkManager = null
        }
    }
    override fun onData(scanDataCollection: ScanDataCollection?) {
        doAsync {
            if(scanDataCollection == null) return@doAsync

            if(scanDataCollection.result != ScannerResults.SUCCESS) {
                return@doAsync
            }

            val scanData  = scanDataCollection.scanData
            if(scanData.size == 0) return@doAsync

            uiThread {
                onBarcodeScanned(scanData.first().data)
            }
        }
    }

    private fun initializeScanner() {
        if(emdkManager == null) return

        if (scanner == null) {
            doAsync {
                // Get the Barcode Manager object
                barcodeManager = emdkManager?.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
                // Get default scanner defined on the device
                scanner = barcodeManager!!.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT)
                // Add data and status listeners
                scanner!!.addDataListener(this@BarcodeScannerActivity)
                scanner!!.addStatusListener(this@BarcodeScannerActivity)
                // Hard trigger. When this mode is set, the user has to manually
                // press the trigger on the device after issuing the read call.
                scanner!!.triggerType = Scanner.TriggerType.HARD
                // Enable the scanner
                uiThread {
                    scanner!!.enable()
                    val config = scanner!!.config
                    config.decoderParams.i2of5.enabled = true
                    config.decoderParams.upce0.enabled = true
                    config.decoderParams.upce0.convertToUpca = true
                    scanner!!.config = config
                    scanner!!.read()

                    scannerSetupCompleted = true
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (emdkManager != null) {
            emdkManager!!.release()
            emdkManager = null
        }
    }

    override fun onResume() {
        super.onResume()

        try {
            initializeEmdkManager()
        } catch (e: ScannerException) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()

        try {
            destroyEmdkManager()
        } catch (e: ScannerException) {
            e.printStackTrace()
        }
    }

//    fun itemNotFound() {
//        val toastView = layoutInflater.inflate(R.layout.toast_view, null) as TextView
//        toastView.text = "Item not found"
//
//        val toast = Toast(applicationContext)
//        toast.view = toastView
//        toast.duration = Toast.LENGTH_LONG
//        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
//        toast.show()
//    }
}