package com.design2net.the_house.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.design2net.the_house.fragments.ProductDialogFragment
import com.design2net.the_house.fragments.BagDialogFragment
import com.design2net.the_house.interfaces.DialogFragmentListener
import com.design2net.the_house.adapters.BagsRecyclerViewAdapter
import com.design2net.the_house.adapters.ProductosRecyclerViewAdapter
import com.design2net.the_house.R
import com.design2net.the_house.adapters.BinesRecyclerViewAdapter
import com.design2net.the_house.models.Bag
import com.design2net.the_house.models.Bin
import com.design2net.the_house.models.Producto
import com.design2net.the_house.fragments.BinDialogFragment
import com.design2net.the_house.fragments.DefinitionDialogFragment
import com.design2net.the_house.interfaces.ApiResponseListener
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode
import kotlinx.android.synthetic.main.activity_checker.*
import kotlinx.android.synthetic.main.activity_checker.btnCompleteOrder
import kotlinx.android.synthetic.main.activity_checker.refreshLayout
import kotlinx.android.synthetic.main.app_bar_checker.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.IndexOutOfBoundsException
import java.lang.StringBuilder
import java.util.ArrayList

class CheckerActivity : BarcodeScannerActivity(), ProductosRecyclerViewAdapter.OnProductoListener,
        BagsRecyclerViewAdapter.OnBagListener, DialogFragmentListener, BinesRecyclerViewAdapter.BinRecyclerViewListener, ApiResponseListener {

    private lateinit var client: OkHttpRequest
    private lateinit var mOrderNumber: String
    private var mOrderId = 0
    private lateinit var bagAdapter: BagsRecyclerViewAdapter
    private lateinit var productoAdapter: ProductosRecyclerViewAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val mProductos = ArrayList<Producto>()
    private val mAllProducts = ArrayList<Producto>()
    private var mBags = ArrayList<Bag>()
    private var mBagsAll = ArrayList<Bag>()
    private var mBines = ArrayList<Bin>()
    private var totalVerifiedQty = 0
    private var totalPickQty = 0
    private var totalOrderQty = 0
    private var mStatus = 0.0
    private var isCreatingTheActivity = false
    private var edtBuscarProductos: EditText? = null
    private var mBagType: String = ""
    private var correctProducts = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_bar_checker)

        client = OkHttpRequest(getString(R.string.url_checker), this)

        isCreatingTheActivity = true

        setSupportActionBar(toolbar_activity_productos)
        title = ""

        // Rescatando valores del intent y asigandolos a respectivos textViews.
        mOrderId = intent.getIntExtra("order_id", 0)
        mOrderNumber = intent.getStringExtra("order_number")
        txtViewOrderNumber.text = mOrderNumber
        txtViewCliente.text = intent.getStringExtra("nombre_cliente")
        mBagType = intent.getStringExtra("bag_type")
        mStatus = intent.getDoubleExtra("status", 2.5)
        txtViewHoraEntrega.text = intent.getStringExtra("hora_desde") + " - " + intent.getStringExtra("hora_hasta")

        initRecyclerView()
        requestProductos()

        if (isOrderVerified()) {
            btnCompleteOrder.visibility = View.GONE
            txtViewPickingCompleted.visibility = View.VISIBLE
        } else {
            txtViewPickingCompleted.visibility = View.GONE
            btnCompleteOrder.visibility = View.VISIBLE
        }

        refreshLayout.setOnRefreshListener {
            Handler().postDelayed({
                requestProductos()
                refreshLayout.isRefreshing = false }, 300)
        }
        toolbarLogo.setOnClickListener { onBackPressed() }
        btnAddBin.setOnClickListener { addBin() }
        btnAddBag.setOnClickListener { addBag() }
        btnCompleteOrder.setOnClickListener {
            onCompleteOrderClick()
        }
    }

    override fun onBackPressed() {
        var checkdeProducts = 0

        for (mProduct in mAllProducts)
            if (mProduct.isChecked)
                checkdeProducts++

        val resultIntent = Intent()
        resultIntent.putExtra("products_completed", checkdeProducts)

        setResult(Activity.RESULT_CANCELED, resultIntent)
        super.onBackPressed()
    }

    // Este metodo procesa las respuestas de los web services.
    override fun onApiResponse(requestCode: Int, response: JSONObject, item: Any?) {
        when (requestCode) {
            RequestCode.GET_PRODUCTS.code   -> processGetProductsResponse(response)
            RequestCode.COMPLETE_ORDER.code -> processCompleteOrderResponse()
            RequestCode.DELETE_BIN.code     -> processDeleteBinResponse()
            RequestCode.DELETE_BAG.code     -> processDeleteBagResponse()
            RequestCode.ADD_BAG.code        -> processAddBagResponse(item as Bag)
            RequestCode.ADD_ITEM.code       -> processAddItemResponse(item as Producto)
        }
    }

    private fun processAddItemResponse(mProducto: Producto) {
        runOnUiThread {
            Bag.selected?.addItem(mProducto)
            mProducto.check()
            productoAdapter.notifyDataSetChanged()
            recyclerViewProductos.scrollToPosition(mProductos.indexOf(mProducto))
            totalVerifiedQty++
            updateData()
            productoAdapter.notifyDataSetChanged()
            bagAdapter.notifyDataSetChanged()
        }
    }

    private fun processAddBagResponse(mBag: Bag) {
        runOnUiThread {
            mBag.type = mBagType
            mBags.add(0, mBag)
            mBagsAll.add(0, mBag)
            updateData()
            Bag.selected = mBag
            bagAdapter.notifyItemInserted(0)
            bagAdapter.notifyItemRangeChanged(1, mBags.size)
            recyclerViewBags.scrollToPosition(0)
        }
    }

    private fun processDeleteBagResponse() {
        runOnUiThread{
            isCreatingTheActivity = true
            updateSelectedBag()
            requestProductos()
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

    private fun processDeleteBinResponse() {
        runOnUiThread{
            isCreatingTheActivity = true
            updateSelectedBin()
            updateSelectedBag()
            requestProductos()
        }
    }

    private fun requestAddItem(mProdcto: Producto) {
        val params = hashMapOf("order_number" to mOrderNumber, "sku" to mProdcto.sku,
                "bag_id" to Bag.selected!!.id, "bin_number" to Bag.selected!!.binNumber.toString())

        client.makePostRequest(RequestCode.ADD_ITEM.code, "insert_to_bag", params, mProdcto)
    }

    private fun confirmProductDialog(mProdcto: Producto) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Producto confirmado   ${mProdcto.checkQty}/${mProdcto.pickQty}")
        builder.setMessage("¿Desea añadir uno más?")
                .setPositiveButton(R.string.anadir) { dialog, _ ->
                    requestAddItem(mProdcto)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    override fun onRetryClick() {
        isCreatingTheActivity = true
        requestProductos()
    }

    private fun requestProductos() {

        runOnUiThread {
            recyclerViewProductos.visibility = View.INVISIBLE
            progress_products?.visibility = View.VISIBLE
        }

        val params = hashMapOf("order_number" to mOrderNumber)
        log("order_number $mOrderNumber")
        client.makePostRequest(RequestCode.GET_PRODUCTS.code, "get-productos", params)
    }

    private fun processGetProductsResponse(myResponse: JSONObject) {
        runOnUiThread{
            try {
                val jsonProductos = myResponse.getJSONArray("productos")
                val jsonBags = myResponse.getJSONArray("bags")
                val totalVerified = myResponse.getInt("total_verified")
                val totalPick = myResponse.getInt("total_picked")
                val totalOrdered = myResponse.getInt("total_ordered")
                val jsonBines = myResponse.getJSONArray("bines")

                mProductos.clear()
                mAllProducts.clear()

                for (i in 0 until jsonProductos.length()) {
                    val productoObj = jsonProductos.getJSONObject(i)

                    val sku = productoObj.getString("sku")
                    val upcStr = productoObj.getString("upc")
                    val upc = upcStr.split(",")
                    val description = productoObj.getString("description")
                    val pickQty = productoObj.getInt("picker_qty")
                    val checkQty = productoObj.getInt("check_qty")
                    val imageUrl = productoObj.getString("image_url")
                    val orderQty = productoObj.getInt("qty")

                    mProductos.add(Producto(sku, upcStr, upc, description, pickQty, imageUrl, checkQty, orderQty))
                    mAllProducts.add(Producto(sku, upcStr, upc, description, pickQty, imageUrl, checkQty, orderQty))
                }

                totalVerifiedQty = totalVerified
                totalPickQty = totalPick
                totalOrderQty = totalOrdered

                mBines.clear()

                for (i in 0 until jsonBines.length()) {
                    val binObj = jsonBines.getJSONObject(i)

                    val binNumber = binObj.getInt("bin_number")
                    val binId = binObj.getString("bin_id")
                    val description = binObj.getString("description")
                    val name = binObj.getString("name")
                    val color = binObj.getString("color")
                    val done = binObj.getInt("done")

                    mBines.add(0, Bin(binNumber, binId, description, name, color, done))
                    log("name: $name")
                    log("color: $color")
                    log("done: $done")
                }

                updateSelectedBin()

                mBags.clear()
                mBagsAll.clear()

                for (i in 0 until jsonBags.length()) {
                    val bagObj = jsonBags.getJSONObject(i)

                    val bagId = bagObj.getString("bag_id")
                    val totalQty = bagObj.getDouble("total_qty")
                    val binNumber = bagObj.getInt("bin_number")

                    val mBag = Bag(bagId, totalQty.toInt(), binNumber)
                    mBag.type = mBagType

                    mBagsAll.add(0, mBag)

                    if (Bin.selected?.binNumber == binNumber) {
                        mBags.add(0, mBag)
                    }
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("Niovan", "Sucedió un error: " + e.message)
            }

            updateSelectedBag()
            edtBuscarProductos?.setText("")
            if (isCreatingTheActivity) {
                productoAdapter.notifyDataSetChanged()
                bagAdapter.notifyDataSetChanged()
                recyclerViewBines.adapter!!.notifyDataSetChanged()
            }
            updateData()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val mSearchItem = menu?.findItem(R.id.menuSearch)
        val btnAddItem = menu?.findItem(R.id.btnAddItem)

        val mSearchView = mSearchItem?.actionView as SearchView
        mSearchView.imeOptions = EditorInfo.IME_ACTION_DONE
        edtBuscarProductos = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)
        edtBuscarProductos?.hint = "Search Item"
        edtBuscarProductos?.setBackgroundResource(R.drawable.rectangulo_shape)

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                addItem(p0.toString())
                hideKeyboard(edtBuscarProductos!!)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                mProductos.clear()

                // Filtrar productos por descripcion o por upc.
                mAllProducts.forEach {
                    if (it.description.contains(p0.toString(), true) || it.upcs.contains(p0.toString()))
                        mProductos.add(it)
                }

                productoAdapter.notifyDataSetChanged()

                return true
            }
        })

        mSearchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                btnAddItem?.isVisible = true
                btnAddItem?.setOnMenuItemClickListener {
                    addItem(edtBuscarProductos?.text.toString())
                    hideKeyboard(edtBuscarProductos!!)
                    return@setOnMenuItemClickListener true
                }
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                btnAddItem?.isVisible = false
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun onCompleteOrderClick() {
        val str = StringBuilder()

        if (totalVerifiedQty < totalPickQty) {
            str.append("- Faltan productos por verificar")
        } else if (totalVerifiedQty > totalPickQty) {
            str.append("- Tienes más productos de los requeridos")
        }

        for (mBin in mBines) {
            if (mBin.done == 0) {
                var count = 0
                for (mBag in mBagsAll) {
                    if (mBag.binNumber == mBin.binNumber) {
                        count++
                    }
                }
                if (count >= 2)
                    str.append("\n- Tienes $count bags en ${mBin.name} ${mBin.description}")
                else if (count >= 1)
                    str.append("\n- Tienes $count bag en ${mBin.name} ${mBin.description}")
            }
        }

        this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Seguro deseas confirmar esta orden?")
                setMessage(str.toString())
                setPositiveButton(R.string.confirmar) { dialog, _ ->
                    client.makePostRequest(RequestCode.COMPLETE_ORDER.code, "complete-orden", hashMapOf("order_id" to mOrderId.toString()))
                    dialog.dismiss()
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            }
            // Create the AlertDialog
            builder.create()
            builder.show()
        }
    }

    private fun addBag() {
        if (mBines.size >= 1) {
            val mBag = Bag(Bin.selected!!.binNumber)
            val params = hashMapOf("order_number" to mOrderNumber, "bag_id" to mBag.id, "bin_number" to mBag.binNumber.toString())

            client.makePostRequest(RequestCode.ADD_BAG.code, "create-bag", params, mBag)
        } else {
            Toast.makeText(this, "Selecciona un bin", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBarcodeScanned(data: String) {
        if (mBags.size >= 1) {
            val converter = BarcodeConverter(data)
            var encontrado = false

            for (mProduct in mProductos) {
                if (mProduct.upcStr.contains(converter.barcode()) || mProduct.sku.contains(data) || mProduct.description.contains(data)) {
                    if (mProduct.isVerified)
                        confirmProductDialog(mProduct)
                    else
                        requestAddItem(mProduct)
                    encontrado = true
                }
            }

            if (!encontrado)
                Toast.makeText(this, "Barcode no está en lista", Toast.LENGTH_SHORT).show()
        }else
            Toast.makeText(this, "Agrega una bolsa", Toast.LENGTH_SHORT).show()
    }

    private fun addItem(input: String) {
        if (mBags.size >= 1) {
            if (isValidUpc(input)) {
                if (mProductos[0].isVerified)
                    confirmProductDialog(mProductos[0])
                else
                    requestAddItem(mProductos[0])
            }
        }else
            Toast.makeText(this, "Agrega una bolsa", Toast.LENGTH_SHORT).show()
    }

    private fun isValidUpc(s: String?): Boolean {
        if (s == null || s.isEmpty())
            return false

        else if (mProductos.size >= 1) {
            return true
        }

        Log.i("NIOVAN", "No se agrego producto")
        Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun initRecyclerView() {
        with(recyclerViewBines) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = BinesRecyclerViewAdapter(mBines, this@CheckerActivity)
        }

        layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL

        bagAdapter = BagsRecyclerViewAdapter(mBags, this)
        recyclerViewBags.layoutManager = layoutManager
        recyclerViewBags.adapter = bagAdapter

        productoAdapter = ProductosRecyclerViewAdapter(mProductos, this)
        recyclerViewProductos.layoutManager = LinearLayoutManager(this)
        recyclerViewProductos.adapter = productoAdapter
    }

    private fun updateSelectedBin() {
        if (mBines.contains(Bin.selected))
            return

        try {
            Bin.selected = mBines[0]
        } catch (e: IndexOutOfBoundsException) {
            Bin.selected = null
        }
    }

    private fun updateSelectedBag() {
        try {
            Bag.selected = mBags[0]
        } catch (e: IndexOutOfBoundsException) {
            Bag.selected = null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateData() {
        isCreatingTheActivity = false

        verifyIsOrderCompleted()
        if (isOrderCompleted()) {
            btnCompleteOrder.setBackgroundResource(R.drawable.ripple_verde_borde_negro_buttons)
            btnCompleteOrder.setTextColor(Color.BLACK)
        } else {
            btnCompleteOrder.setBackgroundResource(R.drawable.ripple_rectangulo_transparent_borde_negro)
            btnCompleteOrder.setTextColor(Color.DKGRAY)
        }

        if (mBagsAll.isEmpty()) {
            Bag.restartCount()
        }

        when {
            mBines.isEmpty() -> {
                txtViewBinMessage.visibility = View.VISIBLE
                txtViewBagMessage.visibility = View.GONE
            }
            mBags.isEmpty() -> {
                txtViewBinMessage.visibility = View.GONE
                txtViewBagMessage.text = getString(R.string.agregar_bolsa_message) + " " + mBagType
                txtViewBagMessage.visibility = View.VISIBLE
            }
            else -> txtViewBagMessage.visibility = View.GONE
        }

        txtViewTotalPick.text = "$totalPickQty"
        txtViewTotalVerificado.text = "$totalVerifiedQty"
        txtViewTotalOrderQty.text = "$totalOrderQty"

        recyclerViewProductos.visibility = View.VISIBLE
        progress_products.visibility = View.GONE
    }

    override fun onBagClick(position: Int) {
        Bag.selected = mBags[position]
        bagAdapter.notifyDataSetChanged()
    }

    override fun onProductoClick(position: Int) {
        val bundle = Bundle()
        bundle.putString("orderNumber", mOrderNumber)
        bundle.putString("sku", mProductos[position].sku)
        bundle.putStringArray("upc", mProductos[position].upcs.toTypedArray())
        bundle.putString("productImage", mProductos[position].imageUrl)
        bundle.putString("description", mProductos[position].description)
        bundle.putInt("ord", mProductos[position].orderQty)
        bundle.putInt("pick", mProductos[position].pickQty)
        bundle.putInt("vrf", mProductos[position].checkQty)

        val dialog = ProductDialogFragment()
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "ProductDialogFragment")
    }

    private fun addBin() {
        val bundle = Bundle()
        bundle.putString("order_number", mOrderNumber)

        val dialog = DefinitionDialogFragment()
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "DefinitionDialogFragment")
    }

    override fun onSaveCompleted() {
        Log.i("NIOVAN", "Saved data")
        isCreatingTheActivity = true
        runOnUiThread {
            requestProductos()
        }
    }

    private fun verifyIsOrderCompleted() {
        correctProducts = 0

        for (mProduct in mAllProducts) {
            if (mProduct.isCorrect)
                correctProducts++
        }
    }

    private fun isOrderCompleted(): Boolean {
        return correctProducts >= mAllProducts.size
    }

    private fun isOrderVerified(): Boolean {
        return mStatus >= 2.8
    }
    override fun onBagLongClick(position: Int) {
        Bag.selected = mBags[position]
        bagAdapter.notifyDataSetChanged()

        val bundle = Bundle()
        bundle.putString("orderNumber", mOrderNumber)
        bundle.putString("bagId", mBags[position].id)
        bundle.putInt("binNumber", Bin.selected!!.binNumber)
        bundle.putString("bin_id", Bin.selected!!.binId)
        bundle.putInt("position", position)

        val dialog = BagDialogFragment()
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "BagDialogFragment")
    }

    override fun onDeleteBagClick(position: Int) {
        this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage(R.string.eliminar_bolsa)
                setTitle("BAG #" + mBags[position].id)
                setPositiveButton(R.string.si) { dialog, _ ->
                    val params = hashMapOf("order_number" to mOrderNumber, "bag_id" to mBags[position].id)
                    client.makePostRequest(RequestCode.DELETE_BAG.code, "delete-bag", params)
                    mBags.removeAt(position)
                    bagAdapter.notifyItemRemoved(position)
                    bagAdapter.notifyItemRangeChanged(position, mBags.size)
                    dialog.dismiss()
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            }

            // Create the AlertDialog
            builder.create()
            builder.show()
        }
    }

    override fun onDeleteBinClick(position: Int) {
        val mBagsInBin = mBags.filter { it.binNumber == mBines[position].binNumber }
        val str = StringBuilder()
        str.append("Seguro deseas eliminar este bin?")
        if (mBagsInBin.isNotEmpty()) {
            str.append("\n\nTambien se eliminará:\n")
            for (i in 0 until mBagsInBin.size){
                str.append("BAG #${mBagsInBin[i].id}")

                if (i < mBagsInBin.size - 1)
                    str.append(", ")
            }
        }

        this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(mBines[position].description)
                setMessage(str.toString())
                setPositiveButton(R.string.si) { dialog, _ ->
                    recyclerViewBines.adapter?.notifyItemRemoved(position)
                    val params = hashMapOf("order_number" to mOrderNumber, "bin_number" to mBines[position].binNumber.toString())
                    client.makePostRequest(RequestCode.DELETE_BIN.code, "delete-bin", params)
                    dialog.dismiss()
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            }
                // Create the AlertDialog
                builder.create()
                builder.show()
            }
    }

    private fun showBinDialogForEdit(position: Int) {
        val bundle = Bundle()
        bundle.putString("order_number", mOrderNumber)
        bundle.putString("name", mBines[position].name)
        bundle.putString("bin_id", mBines[position].binId)
        bundle.putString("color", mBines[position].color)
        bundle.putInt("done", mBines[position].done)
        bundle.putString("desc", mBines[position].description)
        bundle.putInt("binNumber", mBines[position].binNumber)
        bundle.putBoolean("editing", true)
        bundle.putInt("position", position)

        val dialog = BinDialogFragment()
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "BinDialogFragment")
    }

    override fun onBinCreated(bin: Bin) {
        mBines.add(0, bin)
        Bin.selected = bin

        runOnUiThread {
            filterBagsInBin(Bin.selected!!)
            recyclerViewBines.adapter!!.notifyItemInserted(0)
            recyclerViewBines.adapter!!.notifyItemRangeChanged(1, mBines.size)
            recyclerViewBines.scrollToPosition(0)
            updateData()
        }
    }

    private fun filterBagsInBin(bin: Bin) {
        mBags.clear()

        for (mBag in mBagsAll) {
            if (mBag.binNumber == bin.binNumber)
                mBags.add(mBag)
        }
        try {
            Bag.selected = mBags[0]
        } catch (e: IndexOutOfBoundsException) {
            Bag.selected = null
        }
        bagAdapter.notifyDataSetChanged()
    }

    override fun onBinLongClick(position: Int) {
        Bin.selected = mBines[position]
        recyclerViewBines.adapter?.notifyDataSetChanged()

        filterBagsInBin(mBines[position])

        showBinDialogForEdit(position)
    }

    override fun onBinClick(position: Int) {
        Bin.selected = mBines[position]
        recyclerViewBines.adapter?.notifyDataSetChanged()

        filterBagsInBin(mBines[position])
        updateData()
    }
}
