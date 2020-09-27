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
import android.text.TextWatcher
import android.view.View
import com.design2net.the_house.R
import com.design2net.the_house.adapters.AislesRecyclerViewAdapter
import com.design2net.the_house.interfaces.ApiResponseListener
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_picked.view.*
import kotlinx.android.synthetic.main.dialog_product_details.view.imgViewProduct
import kotlinx.android.synthetic.main.dialog_product_details.view.txtViewOrd
import kotlinx.android.synthetic.main.dialog_product_details.view.txtViewUpcProduct
import kotlinx.android.synthetic.main.search_aisle.view.*
import org.json.JSONObject


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PickerDialogFragment : BaseDialogFragment(), ApiResponseListener {

    private lateinit var client: OkHttpRequest
    private lateinit var mOrderNumber: String
    private lateinit var mSku: String
    private lateinit var mUpcs: Array<String>
    private var mAisles = ArrayList<String>()
    private var mAislesAll = ArrayList<String>()
    private var mPickedUpcs = ArrayList<String>()
    private lateinit var mDescription: String
    private lateinit var mAdminComments: String
    private lateinit var mAisle: String
    private var newUpdatedAisle: String = ""
    private var mPickerUser: String = ""
    private var mComments: String = ""
    private var mSubstitute: String = ""
    private lateinit var mImageUrl: String
    private var mOrderQty = 0
    private var mPickQty = 0
    private var mAislePos = 0
    private var mPosition: Int = 0
    private lateinit var mDialogView: View
    private lateinit var dialogSerachAisles: Dialog
    private var typedText = ""

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
        mOrderNumber = arguments!!.getString("orderNumber")
        mSku = arguments!!.getString("sku")
        mUpcs = arguments!!.getStringArray("upcs")
        mImageUrl = arguments!!.getString("productImage")
        mDescription = arguments!!.getString("description")
        mAdminComments = arguments!!.getString("adminComments")
        mAisle = arguments!!.getString("aisle")
        newUpdatedAisle = arguments!!.getString("new_aisle")
        mPickerUser = arguments!!.getString("picker_user")
        mPickQty = arguments!!.getInt("pick")
        mOrderQty = arguments!!.getInt("ord")
        mPosition = arguments!!.getInt("position")
        mComments = arguments!!.getString("comments")
        mSubstitute = arguments!!.getString("substitute")

        val inflater = requireActivity().layoutInflater
        mDialogView = inflater.inflate(R.layout.dialog_picked, null)

        client = OkHttpRequest(getString(R.string.url_picker), this)
        client.makePostRequest(RequestCode.GET_AISLES.code, "get-aisles", hashMapOf("location" to mOrderNumber.take(4)))

        return activity?.let {
            val builder = AlertDialog.Builder(it)

            with(mDialogView) {
                txtViewSku.text = "Producto $mSku"
                txtViewTitle.text = mDescription
                txtViewAisle.text = mAisle
                txtViewPickedOrdenado.text = "$mPickQty/$mOrderQty"
                edtNotas.setText(mAdminComments)
                with(recyclerPickedUpc) {
                    layoutManager = LinearLayoutManager(context)
                }
            }


            Picasso.get()
                    .load(mImageUrl)
                    .into(mDialogView.imgViewProduct)

            with(mDialogView) {
                imgViewProduct.setOnClickListener { zoomImage() }
                btnRemoverPicked.setOnClickListener { }
                btnScanWithQty.setOnClickListener {
                    requestUpdateProduct()
                }
            }

            builder.setView(mDialogView)
                    .setNegativeButton(R.string.cerrar) { dialog, _ ->
                        dialog.cancel()
                    }

            hideKeyboard(mDialogView.edtNotas)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    private fun resetProduct() {
        val params = hashMapOf("order_number" to mOrderNumber, "sku" to mSku, "order_qty" to mOrderQty.toString(), "original_aisle" to mAisle)
        client.makePostRequest(RequestCode.RESET_PRODUCT.code, "reset-product", params)
        dialog.dismiss()
    }

    private fun requestUpdateProduct() {
        mAdminComments = mDialogView.edtNotas.text.toString()

        if (mPickQty < mOrderQty)
            showNoDisponibleDialog(mPickQty)
        else {
            val params = hashMapOf("order_number" to mOrderNumber, "sku" to mSku, "not_available_reason" to "", "pick_qty" to mPickQty.toString(),
                    "comments" to mAdminComments, "original_aisle" to mAisle, "new_aisle" to newUpdatedAisle)

            client.makePostRequest(RequestCode.UPDATE_PRODUCT.code, "update-product", params)
            dismiss()
        }

    }

    private fun showNoDisponibleDialog(pickedQty: Int) {
        activity?.let {
            val arr = resources.getStringArray(R.array.not_available_reasons)
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.razon_no_disponible)
                    .setItems(R.array.not_available_reasons) { dialog, which ->
                        val notAvailableReason = arr[which]
                        var notAvailable = 0
                        val params = hashMapOf("order_number" to mOrderNumber, "sku" to mSku, "not_available_reason" to notAvailableReason,
                                "pick_qty" to pickedQty.toString(), "comments" to mAdminComments, "original_aisle" to mAisle, "new_aisle" to newUpdatedAisle)
                        client.makePostRequest(RequestCode.NOT_AVAILABLE.code, "update-product", params)
                        if (pickedQty <= 0) {
                            notAvailable = 1
                        }
                        dialog.dismiss()
                        this.dismiss()
                        mListener!!.onProductPicked(mPosition, pickedQty, mAdminComments, notAvailable, newUpdatedAisle)
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
            RequestCode.UPDATE_PRODUCT.code -> mListener!!.onProductPicked(mPosition, mPickQty, mAdminComments, 0, newUpdatedAisle)
            RequestCode.GET_AISLES.code -> processGetAislesResponse(response)
            RequestCode.RESET_PRODUCT.code -> mListener!!.onProductReseted(response)
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

    interface ProductPickedListener {
        fun onProductPicked(position: Int, pickedQty: Int, comment: String, notAvailable: Int, newAisle: String)
        fun onProductReseted(response: JSONObject)
        fun onbtnOfrecerSustitutoClicked(position: Int)
    }
}

