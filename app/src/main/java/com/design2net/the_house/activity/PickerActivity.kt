package com.design2net.the_house.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import com.design2net.the_house.R
import com.design2net.the_house.adapters.PickerRecyclerViewAdapter
import com.design2net.the_house.fragments.PickerDialogFragment
import com.design2net.the_house.interfaces.ApiResponseListener
import com.design2net.the_house.models.Producto
import com.design2net.the_house.MyApplication
import com.design2net.the_house.fragments.SustitutoDialogFragment
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode
import kotlinx.android.synthetic.main.activity_picker.*
import kotlinx.android.synthetic.main.app_bar_picker.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.IndexOutOfBoundsException
import kotlin.collections.ArrayList

class PickerActivity : BarcodeScannerActivity(), PickerRecyclerViewAdapter.PickerRecyclerViewListener, ApiResponseListener, PickerDialogFragment.ProductPickedListener, SustitutoDialogFragment.SustitutoListener {

    private var client: OkHttpRequest? = null
    private val mProductosAll = ArrayList<Producto>()
    private val mProductos = ArrayList<Producto>()
    private val mProductosStatus = ArrayList<Producto>()
    private var pasillos = ArrayList<String>()
    private val sustitutoDialogFragment = SustitutoDialogFragment()
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private var mOrderNumber = ""
    private var mOrderId = 0
    private var mStatus = 0.0
    private var pasilloPos: Int = 0

    private var pendientesQty = 0
    private var listosQty = 0
    private var enEsperaQty = 0
    private var noDisponiblesQty = 0

    private var inPendintes = true
        get() = field && !inListo && !inEspera && !inNoDisponible

    private var inListo = false
        get() = field && !inPendintes && !inEspera && !inNoDisponible

    private var inEspera = false
        get() = field && !inPendintes && !inListo && !inNoDisponible

    private var inNoDisponible = false
        get() = field && !inPendintes && !inListo && !inEspera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_bar_picker)

        setSupportActionBar(toolbar)
        backArrow.visibility = View.VISIBLE
        toolbarLogo.setOnClickListener { onBackPressed() }

        client = OkHttpRequest(getString(R.string.url_picker), this)

        // Rescatando valores del intent y asigandolos a respectivos textViews.
        mOrderId = intent.getIntExtra("order_id", 0)
        mOrderNumber = intent.getStringExtra("order_number")
        txtViewTitle.text = mOrderNumber
        intent.getStringExtra("nombre_cliente")
        intent.getStringExtra("hora_desde") + " - " + intent.getStringExtra("hora_hasta")
        mStatus = intent.getDoubleExtra("status", 2.0)

        chatNotificationIcon.visibility = View.VISIBLE
        initTabs()
        initRecyclerView()
        requestProductos()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtViewUserFullName.text = Html.fromHtml(getString(R.string.confirmacion_orden, "<b>${MyApplication.getAuth().fullName}</b>"), Html.FROM_HTML_MODE_LEGACY)
        } else {
            txtViewUserFullName.text = Html.fromHtml(getString(R.string.confirmacion_orden, "<b>${MyApplication.getAuth().fullName}</b>"))
        }

        refreshLayout.setOnRefreshListener {
            Handler().postDelayed({
                requestProductos()
                refreshLayout.isRefreshing = false
            }, 300)
        }
        imgBtnPasilloSiguiente.setOnClickListener { showNextPasillo() }
        imgBtnPasilloAnterior.setOnClickListener { showPreviousPasillo() }
        chatNotificationIcon.setOnClickListener { showChatActivity() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        requestMessageNotifications()
    }

    private fun showChatActivity() {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("order_number", mOrderNumber)
        startActivityForResult(intent, 9)
    }

    private fun showPreviousPasillo() {
        if (pasilloPos > 0)
            pasilloPos--
        else
            pasilloPos = pasillos.size - 1

        filtrarPorPasillo(pasilloPos)
    }

    private fun showNextPasillo() {
        if (pasilloPos < pasillos.size - 1)
            pasilloPos++
        else
            pasilloPos = 0

        filtrarPorPasillo(pasilloPos)
    }

    override fun onBackPressed() {
        var pickedProducts = 0
        var notAvailable = 0

        for (mProduct in mProductosAll) {
            if (mProduct.isPicked)
                pickedProducts++

            if (mProduct.isNoDisponible)
                notAvailable++
        }

        val resultIntent = Intent()
        resultIntent.putExtra("products_completed", pickedProducts)
        resultIntent.putExtra("not_availables", notAvailable)

        setResult(Activity.RESULT_CANCELED, resultIntent)
        super.onBackPressed()
    }

    private fun initTabs() {
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                mProductos.clear()
                mProductosStatus.clear()

                when (p0!!.position) {
                    0 -> {
                        en(pendintes = true)
                        for (mProduct in mProductosAll)
                            if (mProduct.isPendiente) {
                                mProductosStatus.add(mProduct)
                                mProductos.add(mProduct)
                            }
                    }
                    1 -> {
                        en(listo = true)
                        for (mProduct in mProductosAll)
                            if (mProduct.isListo) {
                                mProductosStatus.add(mProduct)
                                mProductos.add(mProduct)
                            }
                    }
                    2 -> {
                        en(espera = true)
                        for (mProduct in mProductosAll)
                            if (mProduct.isEnEspera) {
                                mProductosStatus.add(mProduct)
                                mProductos.add(mProduct)
                            }
                    }
                    3 -> {
                        en(noDisponible = true)
                        for (mProduct in mProductosAll)
                            if (mProduct.isNoDisponible) {
                                mProductosStatus.add(mProduct)
                                mProductos.add(mProduct)
                            }
                    }
                }

                countProductsInStatus()
                updateData()
            }
        })
    }

    private fun en(pendintes: Boolean = false, listo: Boolean = false, espera: Boolean = false, noDisponible: Boolean = false) {
        this.inPendintes = pendintes
        this.inListo = listo
        this.inEspera = espera
        this.inNoDisponible = noDisponible
    }

    override fun onRetryClick() {
        requestProductos()
    }

    private fun requestProductos() {
        recyclerViewPick.visibility = View.INVISIBLE
        progressPicker.visibility = View.VISIBLE

        val params = hashMapOf("order_number" to mOrderNumber)
        client!!.makePostRequest(RequestCode.GET_PRODUCTS.code, "get-productos", params)
    }

    private fun initRecyclerView() {
        with(recyclerViewPick) {
            layoutManager = LinearLayoutManager(context)
            adapter = PickerRecyclerViewAdapter(mProductos, this@PickerActivity)
        }
    }

    private fun initSpinner() {
        pasillos.clear()
        pasillos.add("Todos los pasillos")

        if (mProductosStatus.isNotEmpty()) {
            try {
                pasillos.addAll(mProductosStatus.map { it.aisle }.distinct() as ArrayList<String>)
            } catch (e: java.lang.ClassCastException) {
                for (mProduct in mProductosStatus) {
                    pasillos.add(mProduct.aisle)
                }
            }
        }

        spinnerAdapter = ArrayAdapter(this@PickerActivity, android.R.layout.simple_spinner_dropdown_item, pasillos)

        with(spinnerPasillos) {
            adapter = spinnerAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    filtrarPorPasillo(position)
                }
            }
        }

        pasilloPos = 0
    }

    private fun filtrarPorPasillo(position: Int) {
        pasilloPos = position
        spinnerPasillos.setSelection(pasilloPos)
        requestMessageNotifications()

        when (pasilloPos) {
            0 -> {
                if (mProductos.size != mProductosStatus.size) {
                    mProductos.clear()
                    mProductos.addAll(mProductosStatus)
                }
            }
            else -> {
                mProductos.clear()

                try {
                    for (mProduct in mProductosStatus) {
                        if (mProduct.aisle == pasillos[pasilloPos])
                            mProductos.add(mProduct)
                    }
                } catch (e: IndexOutOfBoundsException) {
                    pasilloPos = 0
                    spinnerPasillos.setSelection(0)

                    for (mProduct in mProductosStatus) {
                        if (mProduct.aisle == pasillos[pasilloPos])
                            mProductos.add(mProduct)
                    }
                }
            }
        }

        recyclerViewPick.adapter!!.notifyDataSetChanged()
    }

    override fun onApiResponse(requestCode: Int, response: JSONObject, item: Any?) {
        when(requestCode) {
            RequestCode.GET_PRODUCTS.code -> processGetProductsResponse(response)
            RequestCode.COMPLETE_ORDER.code -> processCompleteOrderResponse()
            RequestCode.GET_NOTIFICATIONS.code -> processGetMessageNotifications(response)
        }
    }

    private fun processGetMessageNotifications(response: JSONObject) {
        runOnUiThread {
            mNotificationsQty = response.getInt("notifications_qty")
            updateTxtViewNotifications()
        }
    }

    private fun updateTxtViewNotifications() {
        if (mNotificationsQty > 0) {
            txtViewNotification.visibility = View.VISIBLE
            txtViewNotification.text = mNotificationsQty.toString()
        } else {
            txtViewNotification.visibility = View.GONE
            txtViewNotification.text = mNotificationsQty.toString()
        }
    }

    private fun processCompleteOrderResponse() {
        runOnUiThread {
            val resultIntent = Intent()
            resultIntent.putExtra("result", mOrderId)

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun processGetProductsResponse(myResponse: JSONObject) {
        runOnUiThread{
            try {
                val jsonProductos = myResponse.getJSONArray("productos")

                mProductos.clear()
                mProductosAll.clear()
                mProductosStatus.clear()

                for (i in 0 until jsonProductos.length()) {
                    val productoObj = jsonProductos.getJSONObject(i)

                    val sku = productoObj.getString("sku")
                    val upcStr = productoObj.getString("upc")
                    val upc = upcStr.split(",")
                    val description = productoObj.getString("description")
                    val aisle = productoObj.getString("aisle")
                    val pickQty = productoObj.getInt("picker_qty")
                    val checkQty = productoObj.getInt("check_qty")
                    val imageUrl = productoObj.getString("image_url")
                    val orderQty = productoObj.getInt("qty")
                    val pickerUser = productoObj.getString("picker_user")
                    val adminComments = productoObj.getString("admin_comments")
                    val notAvailable = productoObj.getInt("not_available")
                    val notAvailableReason = productoObj.getString("not_available_reason")
                    val newAisle = productoObj.getString("new_aisle")
                    val comments = productoObj.getString("comments")
                    val substitute = productoObj.getString("substitute")
                    val waitingSubstitute = productoObj.getInt("waiting_substitute")

                    mProductosAll.add(Producto(sku, upcStr, upc, description, aisle, pickQty, imageUrl, checkQty, orderQty, pickerUser, adminComments, notAvailable, notAvailableReason, newAisle, comments, substitute, waitingSubstitute))

                    when {
                        inPendintes -> {
                            if (pickerUser.isEmpty()) {
                                mProductosStatus.add(Producto(sku, upcStr, upc, description, aisle, pickQty, imageUrl, checkQty, orderQty, pickerUser, adminComments, notAvailable, notAvailableReason, newAisle, comments, substitute, waitingSubstitute))
                                mProductos.add(Producto(sku, upcStr, upc, description, aisle, pickQty, imageUrl, checkQty, orderQty, pickerUser, adminComments, notAvailable, notAvailableReason, newAisle, comments, substitute, waitingSubstitute))
                            }
                        }
                        inListo -> {
                            if (pickerUser.isNotEmpty() && notAvailable == 0) {
                                mProductosStatus.add(Producto(sku, upcStr, upc, description, aisle, pickQty, imageUrl, checkQty, orderQty, pickerUser, adminComments, notAvailable, notAvailableReason, newAisle, comments, substitute, waitingSubstitute))
                                mProductos.add(Producto(sku, upcStr, upc, description, aisle, pickQty, imageUrl, checkQty, orderQty, pickerUser, adminComments, notAvailable, notAvailableReason, newAisle, comments, substitute, waitingSubstitute))
                            }
                        }
                        inEspera -> {
                            if (waitingSubstitute == 1) {
                                mProductosStatus.add(Producto(sku, upcStr, upc, description, aisle, pickQty, imageUrl, checkQty, orderQty, pickerUser, adminComments, notAvailable, notAvailableReason, newAisle, comments, substitute, waitingSubstitute))
                                mProductos.add(Producto(sku, upcStr, upc, description, aisle, pickQty, imageUrl, checkQty, orderQty, pickerUser, adminComments, notAvailable, notAvailableReason, newAisle, comments, substitute, waitingSubstitute))
                            }
                        }
                        inNoDisponible -> {
                            if (pickerUser.isNotEmpty() && notAvailable == 1) {
                                mProductosStatus.add(Producto(sku, upcStr, upc, description, aisle, pickQty, imageUrl, checkQty, orderQty, pickerUser, adminComments, notAvailable, notAvailableReason, newAisle, comments, substitute, waitingSubstitute))
                                mProductos.add(Producto(sku, upcStr, upc, description, aisle, pickQty, imageUrl, checkQty, orderQty, pickerUser, adminComments, notAvailable, notAvailableReason, newAisle, comments, substitute, waitingSubstitute))
                            }
                        }
                    }
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("Niovan", "Sucedió un error: " + e.message)
            }

            updateData()
            countProductsInStatus()
        }
    }

    private fun updateData() {
        recyclerViewPick.adapter!!.notifyDataSetChanged()
        initSpinner()

        recyclerViewPick.visibility = View.VISIBLE

        var pendientesCantidad = 0
        for (mProduct in mProductosAll) {
            if (mProduct.isNotPicked) {
                pendientesCantidad++
                break
            }
        }

        val pendientes: Boolean = pendientesCantidad > 0
        val productos:Boolean = mProductos.isNotEmpty()
        val status:Boolean = mStatus >= 2.5

        if (!pendientes && !productos && !status) {
            chkBoxCompleted.setOnCheckedChangeListener { _, isChecked ->
                btnCompleteOrder.setOnClickListener { requestCompleteOrder() }
                btnCompleteOrder.isEnabled = isChecked

                if (isChecked) {
                    btnCompleteOrder.setBackgroundResource(R.drawable.ripple_verde_borde_negro_buttons)
                    btnCompleteOrder.setTextColor(Color.BLACK)
                } else {
                    btnCompleteOrder.setBackgroundResource(R.drawable.ripple_rectangulo_transparent_borde_negro)
                    btnCompleteOrder.setTextColor(Color.LTGRAY)
                }
            }
            LayoutCompleteOrder.visibility = View.VISIBLE
            txtViewNoProductos.visibility = View.GONE
        } else if (!pendientes && !productos && status) {
            LayoutCompleteOrder.visibility = View.GONE
            txtViewNoProductos.visibility = View.VISIBLE
        } else if (!pendientes && productos && !status) {
            chkBoxCompleted.setOnCheckedChangeListener { _, isChecked ->
                btnCompleteOrder.setOnClickListener { requestCompleteOrder() }
                btnCompleteOrder.isEnabled = isChecked

                if (isChecked) {
                    btnCompleteOrder.setBackgroundResource(R.drawable.ripple_verde_borde_negro_buttons)
                    btnCompleteOrder.setTextColor(Color.BLACK)
                } else {
                    btnCompleteOrder.setBackgroundResource(R.drawable.ripple_rectangulo_transparent_borde_negro)
                    btnCompleteOrder.setTextColor(Color.LTGRAY)
                }
            }
            LayoutCompleteOrder.visibility = View.VISIBLE
            txtViewNoProductos.visibility = View.GONE
        } else if (!pendientes && productos && status) {
            LayoutCompleteOrder.visibility = View.GONE
            txtViewNoProductos.visibility = View.GONE
        } else if (pendientes && productos && !status) {
            LayoutCompleteOrder.visibility = View.GONE
            txtViewNoProductos.visibility = View.GONE
        } else if (pendientes && !productos && !status) {
            LayoutCompleteOrder.visibility = View.GONE
            txtViewNoProductos.visibility = View.VISIBLE
        } else if (pendientes && !productos && status) {
            LayoutCompleteOrder.visibility = View.GONE
            txtViewNoProductos.visibility = View.VISIBLE
//            throw IllegalStateException("Tienes pendientes y status completado")
        } else if (pendientes && productos && status) {
            LayoutCompleteOrder.visibility = View.GONE
            txtViewNoProductos.visibility = View.GONE
//            throw IllegalStateException("Tienes pendientes y status completado")
        }


        progressPicker.visibility = View.GONE
    }

    private fun requestMessageNotifications() {
        val params = hashMapOf("order_number" to mOrderNumber)
        client!!.makePostRequest(RequestCode.GET_NOTIFICATIONS.code, "get-notifications", params)
    }

    private fun requestCompleteOrder() {
        Toast.makeText(this, "Orden completada.", Toast.LENGTH_SHORT).show()

        client!!.makePostRequest(RequestCode.COMPLETE_ORDER.code, "complete-orden", hashMapOf("order_id" to mOrderId.toString()))
    }

    override fun onBarcodeScanned(data: String) {
        val converter = BarcodeConverter(data)

        if(sustitutoDialogFragment.dialog != null && sustitutoDialogFragment.dialog.isShowing) {
            sustitutoDialogFragment.requestSustituto(converter.barcode().toString())
            return
        }

        log(converter.barcode())
        for (position in 0 until mProductos.size)
            if (mProductos[position].upcStr.contains(converter.barcode()) || mProductos[position].sku.contains(data, true)) {
                showPickerDialog(position)
                return
            }


        val toast = Toast.makeText(this, "Producto no está en lista", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        val toastContentView = toast.view as LinearLayout
        toastContentView.setBackgroundResource(R.drawable.spinner_background)
        toastContentView.setPadding(converTodp(10), converTodp(10), converTodp(10), converTodp(10))
        val textView = ((toast.view as LinearLayout)).getChildAt(0) as TextView
        textView.setTextColor(Color.RED)
        toast.show()
    }

    private fun countProductsInStatus() {
        pendientesQty = 0
        listosQty = 0
        enEsperaQty = 0
        noDisponiblesQty = 0

        for (producto in mProductosAll) {
            when {
                producto.isPendiente -> pendientesQty++
                producto.isListo -> listosQty++
                producto.isEnEspera -> enEsperaQty++
                producto.isNoDisponible -> noDisponiblesQty++
            }
        }

        noDisponiblesQty += enEsperaQty

        setQtyOnTabs()
    }

    private fun setQtyOnTabs() {
        tabs.getTabAt(0)!!.text = "Pend. ($pendientesQty)"
        tabs.getTabAt(1)!!.text = "Listo ($listosQty)"
        tabs.getTabAt(2)!!.text = "Espera ($enEsperaQty)"
        tabs.getTabAt(3)!!.text = "No Disp. ($noDisponiblesQty)"
    }

    override fun onProductoClick(position: Int) {
        showPickerDialog(position)
    }

    private fun showPickerDialog(position: Int) {
        // Si la orden está completada no levanta alertDialog.
        if (mStatus >= 2.5) {
            Toast.makeText(this, "Esta orden ya está completada.", Toast.LENGTH_SHORT).show()
            return
        } else if (mProductos[position].pickerUser.isNotEmpty() && mProductos[position].pickerUser != MyApplication.getAuth().username) {
            Toast.makeText(this, "Product picked by ${mProductos[position].pickerUser}", Toast.LENGTH_SHORT).show()
            return
        }

        val bundle = Bundle()
        bundle.putString("orderNumber", mOrderNumber)
        bundle.putString("sku", mProductos[position].sku)
        bundle.putStringArray("upc", mProductos[position].upc.toTypedArray())
        bundle.putString("productImage", mProductos[position].imageUrl)
        bundle.putString("description", mProductos[position].description)
        bundle.putString("adminComments", mProductos[position].adminComments)
        bundle.putString("aisle", mProductos[position].aisle)
        bundle.putString("new_aisle", mProductos[position].newAisle)
        bundle.putString("picker_user", mProductos[position].pickerUser)
        bundle.putInt("ord", mProductos[position].orderQty)
        bundle.putInt("pick", mProductos[position].pickQty)
        bundle.putString("comments", mProductos[position].comments)
        bundle.putString("substitute", mProductos[position].substitute)
        bundle.putInt("pick", mProductos[position].pickQty)
        bundle.putInt("position", position)

        val dialog = PickerDialogFragment()
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "PickerDialogFragment")
    }

    private fun showSustitutoDialog(position: Int) {
        val bundle = Bundle()
        bundle.putString("orderNumber", mOrderNumber)
        bundle.putString("sku", mProductos[position].sku)

        sustitutoDialogFragment.arguments = bundle
        sustitutoDialogFragment.show(supportFragmentManager, "SustitutoDialogFragment")
    }

    override fun onbtnOfrecerSustitutoClicked(position: Int) {
        showSustitutoDialog(position)
    }

    override fun onProductPicked(position: Int, pickedQty: Int, comment: String, notAvailable: Int, newAisle: String) {
        runOnUiThread {
            // Asiganar comentario al producto dentro de la lista.
            for (idx in 0 until mProductosAll.size) {
                if (mProductosAll[idx] == mProductos[position]) {
                    mProductosAll[idx].pickQty = pickedQty
                    mProductosAll[idx].adminComments = comment
                    mProductosAll[idx].pickerUser = MyApplication.getAuth().username
                    mProductosAll[idx].notAvailable = notAvailable
                    mProductosAll[idx].newAisle = newAisle
                    break
                }
            }

            pasilloPos = pasillos.indexOf(mProductos[position].aisle)

            if ((inListo && notAvailable == 0) || (inNoDisponible && notAvailable == 1)) {
                mProductos[position].pickQty = pickedQty
                mProductos[position].adminComments = comment
                mProductos[position].pickerUser = MyApplication.getAuth().username
                mProductos[position].notAvailable = notAvailable
                mProductos[position].newAisle = newAisle
                recyclerViewPick.adapter!!.notifyItemChanged(position)
            } else {
                mProductosStatus.remove(mProductos[position])
                mProductos.removeAt(position)
                recyclerViewPick.adapter!!.notifyItemRemoved(position)
            }

            when {
                mProductosStatus.isEmpty() -> updateData()
                mProductos.isEmpty() -> {
                    pasillos.removeAt(pasilloPos)
                    spinnerAdapter.notifyDataSetChanged()
                    filtrarPorPasillo(pasilloPos)
                }
                else -> recyclerViewPick.adapter!!.notifyItemRangeChanged(position, mProductos.size)
            }
            updateData()
            countProductsInStatus()
        }
    }

    override fun onProductReseted(response: JSONObject) {
        runOnUiThread {
            val productoReseted = fetchProduct(response)

            updateProduct(productoReseted, mProductosAll)
            deleteProduct(productoReseted, mProductosStatus)
            val position = deleteProduct(productoReseted, mProductos)

            recyclerViewPick.adapter!!.notifyItemRemoved(position)
            recyclerViewPick.adapter!!.notifyItemRangeChanged(position, mProductos.size)
            countProductsInStatus()
            updateData()
        }
    }

    override fun onSustitutosSent(response: JSONObject) {
        runOnUiThread {
            val producto = fetchProduct(response)

            this.updateProduct(producto, mProductosAll)

            if (inPendintes) {
                this.deleteProduct(producto, mProductosStatus)
                val position = this.deleteProduct(producto, mProductos)
                recyclerViewPick.adapter!!.notifyItemRemoved(position)
                recyclerViewPick.adapter!!.notifyItemRangeChanged(position, mProductos.size)
            } else {
                this.updateProduct(producto, mProductosStatus)
                val position = this.updateProduct(producto, mProductos)
                recyclerViewPick.adapter!!.notifyItemChanged(position)
            }

            countProductsInStatus()
            updateData()
        }
    }

    private fun fetchProduct(response: JSONObject): Producto {
        val productoObj = response.getJSONObject("producto")

        val sku = productoObj.getString("sku")
        val upcStr = productoObj.getString("upcs")
        val upc = upcStr.split(",")
        val description = productoObj.getString("description")
        val aisle = productoObj.getString("aisle_name")
        val pickQty = productoObj.getInt("picker_qty")
        val checkQty = productoObj.getInt("total_check_qty")
        val imageUrl = productoObj.getString("image_url")
        val orderQty = productoObj.getInt("qty")
        val pickerUser = productoObj.getString("picker_user")
        val adminComments = productoObj.getString("admin_comments")
        val notAvailable = productoObj.getInt("not_available")
        val notAvailableReason = productoObj.getString("not_available_reason")
        val newAisle = productoObj.getString("new_aisle")
        val comments = productoObj.getString("comments")
        val substitute = productoObj.getString("substitute")
        val waitingSubstitute = productoObj.getInt("waiting_substitute")

        return Producto(
                sku, upcStr, upc, description, aisle, pickQty, imageUrl, checkQty, orderQty,
                pickerUser, adminComments, notAvailable, notAvailableReason, newAisle, comments,
                substitute, waitingSubstitute
        )
    }

    //    Method for products lists
    private fun updateProduct(product: Producto, list: ArrayList<Producto>): Int {
        var index = -1
        for (idx in 0 until list.size) {
            if (list[idx].sku == product.sku) {
                list[idx] = product
                index = idx
                break
            }
        }

        return index
    }

    private fun deleteProduct(product: Producto, list: ArrayList<Producto>): Int {
        val index = list.indexOf(product)
        list.removeAt(index)

        return index
    }


}