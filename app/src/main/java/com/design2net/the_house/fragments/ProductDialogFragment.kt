package com.design2net.the_house.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.design2net.the_house.R
import com.design2net.the_house.adapters.ProductDetailsAdapter
import com.design2net.the_house.models.Bag
import com.design2net.the_house.interfaces.ApiResponseListener
import com.design2net.the_house.interfaces.DialogFragmentListener
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_product_details.view.*
import kotlinx.android.synthetic.main.dialog_product_details.view.txtViewUpcProduct
import org.json.JSONArray
import org.json.JSONObject


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ProductDialogFragment : BaseDialogFragment(), ProductDetailsAdapter.OnBagListener, ApiResponseListener {

    private lateinit var client: OkHttpRequest
    private lateinit var mOrderNumber: String
    private lateinit var mSku: String
    private lateinit var mUpc: Array<String>
    private lateinit var mDescription: String
    private lateinit var mImageUrl: String
    private var mOrderQty = 0
    private var mPickQty = 0
    private var mVerifiedQty = 0
    private lateinit var mDialogView: View
    private var mCurrentTotalQty: Int = 0
    private val mBags = ArrayList<Bag>()
    private var productDetailsAdapter = ProductDetailsAdapter(mBags, this)

    private lateinit var listener: DialogFragmentListener

    // Override the Fragment.onAttach() method to instantiate the ProductDetailsDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the ProductDetailsDialogListener so we can send events to the host
            listener = context as DialogFragmentListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement ProductDetailsDialogListener"))
        }
    }

        @SuppressLint("InflateParams", "SetTextI18n")
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mOrderNumber = arguments!!.getString("orderNumber")
        mSku = arguments!!.getString("sku")
        mUpc = arguments!!.getStringArray("upc")
        mImageUrl = arguments!!.getString("productImage")
        mDescription = arguments!!.getString("description")
        mPickQty = arguments!!.getInt("pick")
        mOrderQty = arguments!!.getInt("ord")
        mVerifiedQty = arguments!!.getInt("vrf")

        val inflater = requireActivity().layoutInflater
        mDialogView = inflater.inflate(R.layout.dialog_product_details, null)

        client = OkHttpRequest(getString(R.string.url_checker), this)

        mDialogView.progress_bar.visibility = View.VISIBLE

        val params = hashMapOf("order_number" to mOrderNumber, "sku" to mSku)
        client.makePostRequest(RequestCode.GET_BAGS.code, "get-bags", params)

        return activity?.let {
            val builder = AlertDialog.Builder(it)

            mDialogView.txtViewOrd.text = mOrderQty.toString()
            mDialogView.txtViewPickProduct.text = mPickQty.toString()
            mDialogView.txtViewVrf.text = mVerifiedQty.toString()
            mDialogView.txtViewUpcProduct.text = mUpc.joinToString(", ")

            mDialogView.recyclerViewProductDetails.layoutManager = LinearLayoutManager(context)
            mDialogView.recyclerViewProductDetails.adapter = productDetailsAdapter

            Picasso.get()
                    .load(mImageUrl)
                    .into(mDialogView.imgViewProduct)

            mDialogView.imgViewProduct.setOnClickListener { zoomImage() }

            builder.setView(mDialogView)
                    .setTitle(mDescription)
                    .setPositiveButton(R.string.guardar) { _, _ ->
                        saveChanges()
                    }
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun zoomImage() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(mImageUrl.replace("/sm", "")), "image/*")
        activity!!.startActivity(intent)
    }

    private fun saveChanges() {
        // Este metodo verifica si el usuario ha hecho cambios en la cantidad del producto en alguna bolsa.
        // De ser cierto busca el valor y lo actualiza.
        val bags = JSONArray()

        for (pos in 0 until mBags.size){
                val currentValue = mBags[pos].currentQty

                if (mBags[pos].checkQty != currentValue) {

                    val bag = JSONObject()
                    bag.put("bag_id", mBags[pos].id)
                    bag.put("check_qty", currentValue.toString())
                    bag.put("bin_number", mBags[pos].binNumber)
                    bags.put(bag)
                }
        }

        if (bags.length() >= 1) {
            val params = hashMapOf("order_number" to mOrderNumber, "sku" to mSku,
                    "total_check_qty" to mCurrentTotalQty.toString(), "pick_qty" to mPickQty.toString(),
                    "bags" to bags.toString())
            client.makePostRequest(RequestCode.ADD_PRODUCTS.code, "add_products", params)
        }

            dialog.dismiss()
    }

    override fun onApiResponse(requestCode: Int, response: JSONObject, item: Any?) {
        when(requestCode) {
            RequestCode.GET_BAGS.code     -> processGetBagsResponse(response)
            RequestCode.ADD_PRODUCTS.code -> listener.onSaveCompleted()
        }
    }

    private fun processGetBagsResponse(myResponse: JSONObject) {
        val jsonBags = myResponse.getJSONArray("bags")
        val jsonTotalQty = myResponse.getInt("total")

        mBags.clear()

        for (idx in 0 until jsonBags.length()) {
            val bagObj = jsonBags.getJSONObject(idx)

            val bagId = bagObj.getString("bag_id")
            val qty = bagObj.getInt("check_qty")
            val binNumber = bagObj.getInt("bin_number")

            mBags.add(0, Bag(bagId, qty, binNumber))
            Log.i("NIOVAN", "bagId: $bagId,   check_qty: $qty")
        }

        mCurrentTotalQty = jsonTotalQty

        activity!!.runOnUiThread {
            mDialogView.progress_bar.visibility = View.GONE
            refreshData()
        }
    }

    private fun refreshData() {
        mDialogView.txtViewTotalInBags.text = mCurrentTotalQty.toString()
        productDetailsAdapter.notifyDataSetChanged()

        if (mBags.size >= 1) {
            mDialogView.txtViewNoBagAdded.visibility = View.GONE
        } else {
            mDialogView.txtViewNoBagAdded.visibility = View.VISIBLE
        }
    }

    override fun onDeleteClick(position: Int) {
        Log.i("NIOVAN", "OnDeleteClick(position):")
        Log.i("NIOVAN", "bagId: " + mBags[position].id + ",    check_qty: "  + mBags[position].checkQty + "    current_qty: "  + mBags[position].currentQty)

        mCurrentTotalQty -= mBags[position].currentQty
        mDialogView.txtViewTotalInBags.text = mCurrentTotalQty.toString()

        mBags[position].currentQty = 0
        productDetailsAdapter.notifyItemChanged(position)
    }

    // Aumenta cantidad verificada del producto.
    override fun onNumericUpButtonClick(position: Int) {
        mCurrentTotalQty++
        mDialogView.txtViewTotalInBags.text = mCurrentTotalQty.toString()

        mBags[position].currentQty++
        productDetailsAdapter.notifyDataSetChanged()
    }

    // Disminuye cantidad verificada del producto.
    override fun onNumericDownButtonClick(position: Int) {
        mCurrentTotalQty--
        mDialogView.txtViewTotalInBags.text = mCurrentTotalQty.toString()

        mBags[position].currentQty--
        productDetailsAdapter.notifyDataSetChanged()
    }
}
