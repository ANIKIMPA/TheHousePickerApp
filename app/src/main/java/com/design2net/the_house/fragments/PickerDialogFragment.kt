package com.design2net.the_house.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.design2net.the_house.R
import com.design2net.the_house.adapters.AislesRecyclerViewAdapter
import com.design2net.the_house.adapters.PickedItemsRecyclerViewAdapter
import com.design2net.the_house.adapters.PickerRecyclerViewAdapter
import com.design2net.the_house.interfaces.ApiResponseListener
import com.design2net.the_house.models.Producto
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_picker.*
import kotlinx.android.synthetic.main.dialog_picked.view.*
import kotlinx.android.synthetic.main.dialog_picked.view.progress_bar
import kotlinx.android.synthetic.main.dialog_product_details.view.*
import kotlinx.android.synthetic.main.dialog_product_details.view.imgViewProduct
import kotlinx.android.synthetic.main.scan_with_qty_dialog.*
import kotlinx.android.synthetic.main.scan_with_qty_dialog.view.*
import kotlinx.android.synthetic.main.search_aisle.view.*
import org.jetbrains.anko.uiThread
import org.json.JSONObject


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PickerDialogFragment : BaseDialogFragment(), ApiResponseListener {

    private lateinit var client: OkHttpRequest
    private lateinit var mOrderNumber: String
    private lateinit var mSku: String
    internal lateinit var mUpcs: Array<String>
    private lateinit var pickedItems: ArrayList<Producto>
    private var mAisles = ArrayList<String>()
    private var mAislesAll = ArrayList<String>()
    private lateinit var mDescription: String
    private lateinit var mAdminComments: String
    private lateinit var mAisle: String
    private var newUpdatedAisle: String = ""
    private var mPickerUser: String = ""
    private var mComments: String = ""
    private var mSubstitute: String = ""
    private lateinit var mImageUrl: String
    private var mOrderQty = 0
    private var mAislePos = 0
    private var mPosition: Int = 0
    private lateinit var mDialogView: View
    private var scanWithQtyDialog: Dialog? = null
    private var typedText = ""
    private var dialogOpen = ""
    private var scannedUpc = ""
    private var mTotalQty = 0

    private var mListener: ProductPickedListener? = null

    // Override the Fragment.onAttach() method to instantiate the ProductsInBagDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the ProductsInBagDialogListener so we can send events to the host
            mListener = context as ProductPickedListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement ProductPickedListener"))
        }
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        initializeValues()
        val inflater = requireActivity().layoutInflater
        mDialogView = inflater.inflate(R.layout.dialog_picked, null)

        client = OkHttpRequest(getString(R.string.url_picker), this)
        client.makePostRequest(RequestCode.GET_AISLES.code, "get-aisles", hashMapOf("location" to mOrderNumber.take(4)))
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            updateData()
            with(mDialogView.recyclerPickedUpc) {
                layoutManager = LinearLayoutManager(context)
                adapter = PickedItemsRecyclerViewAdapter(pickedItems)
            }

            onBarcodeScanned(scannedUpc)

            Picasso.get()
                    .load(mImageUrl)
                    .into(mDialogView.imgViewProduct)

            with(mDialogView) {
                imgViewProduct.setOnClickListener { zoomImage() }
                btnRemoverPicked.setOnClickListener { showRemoverDialog() }
                btnNoDisponible.setOnClickListener { showNoDisponibleDialog(0) }
                btnScanWithQty.setOnClickListener {
                    scanWithQtyDialog!!.show()
                    showKeyBoard()
                }
            }

            builder.setView(mDialogView)
                    .setPositiveButton(R.string.completar) {_, _ ->
                        requestCompletedProduct()
                    }
                    .setNegativeButton(R.string.cerrar) { dialog, _ ->
                        dialog.cancel()
                        mListener!!.onPickerDialogFragmentClose()
                    }

            hideKeyboard(mDialogView.edtNotas)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    private fun initializeValues() {
        mAislePos = 0
        typedText = ""
        dialogOpen = ""
        mTotalQty = arguments!!.getInt("pick")
        scanWithQtyDialog = scanWithQtyDialog()
        mOrderNumber = arguments!!.getString("orderNumber")
        mSku = arguments!!.getString("sku")
        mUpcs = arguments!!.getStringArray("upcs")
        mImageUrl = arguments!!.getString("productImage")
        mDescription = arguments!!.getString("description")
        mAdminComments = arguments!!.getString("adminComments")
        mAisle = arguments!!.getString("aisle")
        newUpdatedAisle = arguments!!.getString("new_aisle")
        mPickerUser = arguments!!.getString("picker_user")
        mOrderQty = arguments!!.getInt("ord")
        mPosition = arguments!!.getInt("position")
        mComments = arguments!!.getString("comments")
        mSubstitute = arguments!!.getString("substitute")
        scannedUpc = arguments!!.getString("scannedUpc")
        pickedItems = ArrayList()
    }

    @SuppressLint("SetTextI18n")
    private fun updateData() {
        with(mDialogView) {
            txtViewSku.text = "Producto $mSku"
            txtViewTitle.text = mDescription
            txtViewAisle.text = mAisle
            txtViewPickedOrdenado.text = "${mTotalQty}/$mOrderQty"
            edtNotas.setText(mAdminComments)
        }
    }

    internal fun onBarcodeScanned(barcode: String) {
        if (barcode in mUpcs) {
            if (dialogOpen == "removerDialog") {
                if(mTotalQty > 0) requestUpdateProduct(barcode, -1)
                else errorToast(requireContext(),"Cantidad de este artículo es 0", Toast.LENGTH_SHORT).show()
            }
            else {
                if (scanWithQtyDialog!!.isShowing) {
                    val qtyToScan = scanWithQtyDialog!!.edtQtyToScan.text.toString()
                    when {
                        qtyToScan.isEmpty() -> errorToast(requireContext(),"Debes especificar cantidad válida!", Toast.LENGTH_SHORT).show()
                        qtyToScan.toInt() == 0 -> errorToast(requireContext(),"Entre una cantidad diferente de 0", Toast.LENGTH_SHORT).show()
                        qtyToScan.toInt() < mTotalQty * -1 -> errorToast(requireContext(),"Entre una cantidad mayor o igual a ${mTotalQty * -1}", Toast.LENGTH_SHORT).show()
                        qtyToScan.toInt() + mTotalQty > mOrderQty -> errorToast(requireContext(),"No se pueden añadir más productos de la cantidad máxima ordenada. Para añadir más productos debe cambiar la cantidad ordenada en el Store-Admin", Toast.LENGTH_LONG).show()
                        else -> requestUpdateProduct(barcode, qtyToScan.toInt())
                    }
                }
                else {
                    if (mTotalQty < mOrderQty)
                        requestUpdateProduct(barcode, 1)
                    else {
                        errorToast(requireContext(),"No se pueden añadir más productos de la cantidad máxima ordenada. Para añadir más productos debe cambiar la cantidad ordenada en el Store-Admin", Toast.LENGTH_LONG).show()
                        requestPickedUpcs()
                    }
                }

                scanWithQtyDialog?.edtQtyToScan?.let { hideKeyboard(it) }
                scanWithQtyDialog?.dismiss()
            }
        } else {
            requestPickedUpcs()
        }
    }

    private fun requestPickedUpcs() {
        val params = hashMapOf("order_number" to mOrderNumber, "sku" to mSku)
        client.makePostRequest(RequestCode.PICK_PRODUCTS.code, "get-picked-upcs", params)
    }

    private fun requestUpdateProduct(barcode: String, pickedQty: Int) {
        mAdminComments = mDialogView.edtNotas.text.toString()

        val params = hashMapOf("order_number" to mOrderNumber, "sku" to mSku, "not_available_reason" to "", "pick_qty" to pickedQty.toString(),
                "comments" to mAdminComments, "original_aisle" to mAisle, "new_aisle" to newUpdatedAisle, "upc" to barcode)

        if((mTotalQty + pickedQty) >= mOrderQty) {
            client.makePostRequest(RequestCode.COMPLETE_PRODUCT.code, "update-product", params)
            scanWithQtyDialog?.dismiss()
            this.dismiss()
            mListener!!.onPickerDialogFragmentClose()
        } else {
            client.makePostRequest(RequestCode.PICK_PRODUCTS.code, "update-product", params)
        }
    }

    private fun requestCompletedProduct() {
        mAdminComments = mDialogView.edtNotas.text.toString()

        if (mTotalQty < mOrderQty)
            showNoDisponibleDialog(mTotalQty)
        else {
            val params = hashMapOf("order_number" to mOrderNumber, "sku" to mSku, "not_available_reason" to "", "final_pick_qty" to mTotalQty.toString(),
                    "comments" to mAdminComments, "original_aisle" to mAisle, "new_aisle" to newUpdatedAisle)

            client.makePostRequest(RequestCode.COMPLETE_PRODUCT.code, "complete-product", params)
            this.dismiss()
            mListener!!.onPickerDialogFragmentClose()
        }
    }

    private fun showNoDisponibleDialog(pickedQty: Int) {
        activity?.let {
            val arr = resources.getStringArray(R.array.not_available_reasons)
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.razon_no_disponible)
                    .setItems(R.array.not_available_reasons) { dialog, which ->
                        val notAvailableReason = arr[which]
                        val params = hashMapOf("order_number" to mOrderNumber, "sku" to mSku, "not_available_reason" to notAvailableReason,
                                "final_pick_qty" to pickedQty.toString(), "comments" to mAdminComments, "original_aisle" to mAisle, "new_aisle" to newUpdatedAisle)
                        client.makePostRequest(RequestCode.COMPLETE_PRODUCT.code, "complete-product", params)
                        dialog.dismiss()
                        this.dismiss()
                        mListener!!.onPickerDialogFragmentClose()
                    }
            builder.create()
            builder.show()
        }
    }

    private fun showRemoverDialog() {
        dialogOpen = "removerDialog"
        activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Escanee los artículos que desea remover")
                    .setMessage("Escanee los UPC previamente añadidos de este producto para removerlos. Cuando termine de escanear los productos que desea remover presione el botón de \"Cerrar\".")
                    .setNegativeButton(R.string.cerrar) { dialog, _ ->
                        dialogOpen = ""
                        dialog.cancel()
                        mListener!!.onPickerDialogFragmentClose()
                    }
            builder.create()
            builder.show()
        }
    }

    private fun zoomImage() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(mImageUrl.replace("/sm", "")), "image/*")
        activity!!.startActivity(intent)
    }

    override fun onApiResponse(requestCode: Int, response: JSONObject, item: Any?) {
        when(requestCode) {
            RequestCode.PICK_PRODUCTS.code -> processPickedProductsResponse(response)
            RequestCode.GET_AISLES.code -> processGetAislesResponse(response)
            RequestCode.RESET_PRODUCT.code -> mListener!!.onProductReseted(response)
        }
    }

    private fun processPickedProductsResponse(response: JSONObject) {
        val jsonContent = response.getJSONObject("content")
        val pickedProducts = jsonContent.getJSONArray("items")
        mTotalQty = jsonContent.getInt("total_qty")

        val newItems = ArrayList<Producto>()
        for (idx in 0 until pickedProducts.length()) {
            val productoObj = pickedProducts.getJSONObject(idx)

            val producto = Producto(mSku)
            producto.upcStr = productoObj.getString("upcref")
            producto.pickQty = productoObj.getInt("picker_qty")
            producto.pickerUser = productoObj.getString("picker_user")
            producto.dateTimePicked = productoObj.getString("picker_time")
            newItems.add(producto)
        }

        activity!!.runOnUiThread {
            pickedItems.clear()
            mDialogView.recyclerPickedUpc.adapter!!.notifyDataSetChanged()
            pickedItems.addAll(newItems)
//            mDialogView.recyclerPickedUpc.recycledViewPool.clear()
            mDialogView.recyclerPickedUpc.adapter!!.notifyDataSetChanged()
            updateData()
        }
    }

    private fun processGetAislesResponse(myResponse: JSONObject) {
        val jsonAisles = myResponse.getJSONArray("aisles")

        for (i in 0 until jsonAisles.length()) {
            val aisle = jsonAisles.getString(i)

            if (aisle == mAisle)
                mAislePos = i

            mAisles.add(aisle)
            mAislesAll.add(aisle)
        }
    }

    @SuppressLint("InflateParams")
    private fun showDialogSearchAisles(): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val mAislesView = inflater.inflate(R.layout.search_aisle, null)

            val aislesAdapter = AislesRecyclerViewAdapter(mAisles)

            mAislesView.edtSearchAisle.setText(typedText)
            mAislesView.edtSearchAisle.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    mAisles.clear()

                    for (aisle in mAislesAll)
                        if (aisle.contains(s.toString(), true))
                            mAisles.add(aisle)

                    aislesAdapter.notifyDataSetChanged()
                    typedText = s.toString()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            builder.setView(mAislesView)
                    .setTitle("Select Aisle")
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                        mListener!!.onPickerDialogFragmentClose()
                    }

            builder.create()

            activity!!.runOnUiThread {
                with(mAislesView.recyclerViewAisles) {
                    layoutManager = LinearLayoutManager(context)
                    adapter = aislesAdapter
                }
            }
            builder.show()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun scanWithQtyDialog(): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val mView = inflater.inflate(R.layout.scan_with_qty_dialog, null)

            mView.edtQtyToScan.setSelectAllOnFocus(true)

            builder.setView(mView)
                    .setTitle("Escanee para añadir.")
                    .setMessage("Escriba la cantidad de artículos que desea añadir del UPC a esta orden. Luego escanee el producto para añadirlo. Para remover puede colocar cantidad en negativo (-). La ventana se cerrará automáticamente al escanear.")
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        hideKeyboard(mView.edtQtyToScan)
                        dialog.cancel()
                    }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface ProductPickedListener {
        fun onPickerDialogFragmentClose()
        fun onProductReseted(response: JSONObject)
        fun onbtnOfrecerSustitutoClicked(position: Int)
    }
}

