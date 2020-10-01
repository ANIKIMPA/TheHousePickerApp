package com.design2net.the_house.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.design2net.the_house.R
import com.design2net.the_house.adapters.BagDetailsAdapter
import com.design2net.the_house.models.Bin

import com.design2net.the_house.models.Producto
import com.design2net.the_house.interfaces.ApiResponseListener
import com.design2net.the_house.interfaces.DialogFragmentListener
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode
import kotlinx.android.synthetic.main.dialog_bag_details.view.*
import kotlinx.android.synthetic.main.dialog_bag_details.view.recyclerViewProductsInBag
import org.json.JSONArray
import org.json.JSONObject


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class BagDialogFragment : BaseDialogFragment(), BagDetailsAdapter.OnProductListener, ApiResponseListener {

    private lateinit var client: OkHttpRequest
    private val mProductos = ArrayList<Producto>()
    private val mProductosToEdit = ArrayList<Producto>()
    private val mBines = ArrayList<Bin>()
    private lateinit var mBagId: String
    private var mBinNumber: Int = 0
    private var mBinNumberEdited = false
    private lateinit var mOrderNumber: String
    private var mTotalProducts: Int = 0
    private lateinit var mDialogView: View

    private lateinit var listener: DialogFragmentListener

    // Override the Fragment.onAttach() method to instantiate the ProductsInBagDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the ProductsInBagDialogListener so we can send events to the host
            listener = context as DialogFragmentListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement ProductsInBagDialogListener"))
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        client = OkHttpRequest(getString(R.string.url_checker), this)

        mOrderNumber = arguments!!.getString("orderNumber")
        mBagId = arguments!!.getString("bagId")
        mBinNumber = arguments!!.getInt("binNumber")
        val position = arguments!!.getInt("position")

        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        mDialogView = inflater.inflate(R.layout.dialog_bag_details, null)

        when(arguments!!.getString("bin_id")) {
            "A" -> mDialogView.imgViewBin.setImageResource(R.drawable.ic_bin_azul_abierto_44dp)
            "V" -> mDialogView.imgViewBin.setImageResource(R.drawable.ic_bin_verde_abierto_44dp)
            "F" -> mDialogView.imgViewBin.setImageResource(R.drawable.ic_freezer_abierto_44dp)
            "IS" -> mDialogView.imgViewBin.setImageResource(R.drawable.ic_articulos_limpieza_abierto_44dp)
            "N" -> mDialogView.imgViewBin.setImageResource(R.drawable.ic_fridge_abierto_44dp)
        }
        initRecyclerView()
        requestBag()

        return activity?.let {
            val builder = AlertDialog.Builder(it)

            mDialogView.btnDeleteBag.setOnClickListener {
                listener.onDeleteBagClick(position)
                dialog.dismiss()
            }

            mDialogView.btnCloseBag.setOnClickListener { dialog.cancel() }

            mDialogView.btnSaveBag.setOnClickListener {
                saveChanges()
            }
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(mDialogView)
                    .setTitle("BAG #$mBagId")
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onApiResponse(requestCode: Int, response: JSONObject, item: Any?) {
        when(requestCode) {
            RequestCode.GET_BAG.code -> processGetBagResponse(response)
            RequestCode.UPDATE_BAG.code -> listener.onSaveCompleted()
        }
    }

    private fun requestBag() {
        mDialogView.progress_products_in_bag.visibility = View.VISIBLE

        val params = hashMapOf("order_number" to mOrderNumber, "bag_id" to mBagId)
        client.makePostRequest(RequestCode.GET_BAG.code, "get-bag", params)
    }

    private fun processGetBagResponse(myResponse: JSONObject) {
        val jsonBag = myResponse.getJSONArray("products")
        val jsonBines = myResponse.getJSONArray("bines")

        mProductos.clear()
        mProductosToEdit.clear()

        for (i in 0 until jsonBag.length()) {
            val productoObj = jsonBag.getJSONObject(i)

            val sku = productoObj.getString("sku")
            val upcStr = productoObj.getString("upc")
            val upc = upcStr.split(",")
            val description = productoObj.getString("description")
            val qty = productoObj.getInt("qty")
            val pickerQty = productoObj.getInt("picker_qty")
            val imageUrl = productoObj.getString("image")
            val checkQty = productoObj.getInt("check_qty")
            mProductos.add(Producto(sku, upcStr, upc as ArrayList<String>, description, pickerQty, imageUrl, checkQty, qty))

            val totalQty = productoObj.getInt("total_qty")
            mTotalProducts = totalQty
        }

        for (i in 0 until jsonBines.length()) {
            val binObj = jsonBines.getJSONObject(i)

            val binNumber = binObj.getInt("bin_number")
            val binId = binObj.getString("bin_id")
            val description = binObj.getString("description")

            mBines.add(0, Bin(binNumber, binId, description))
        }

        activity!!.runOnUiThread{
            mDialogView.progress_products_in_bag.visibility = View.GONE
            initSpinner()
            uploadDataCompleted()
        }
    }

    private fun initRecyclerView() {
        with(mDialogView.recyclerViewProductsInBag) {
            layoutManager = LinearLayoutManager(context)
            adapter = BagDetailsAdapter(mProductos, this@BagDialogFragment)
        }
    }

    private fun uploadDataCompleted() {
        mDialogView.recyclerViewProductsInBag.adapter?.notifyDataSetChanged()
        mDialogView.txtViewTotal.text = mTotalProducts.toString()
    }

    override fun onTextChange(position: Int, value: Int) {
        mProductos[position].checkQty = value
        mProductosToEdit.add(mProductos[position])

        mTotalProducts = 0
        for (mProduct in mProductos)
            mTotalProducts += mProduct.checkQty

        mDialogView.txtViewTotal.text = mTotalProducts.toString()
    }

    private fun initSpinner() {
        var binPosition = 0
        for (i in 0 until mBines.size) {
            if (mBines[i].binNumber == mBinNumber)
                binPosition = i
        }

//        mDialogView.`@+id/imgViewBin`.text = mBines[binPosition].description
//        mDialogView.`@+id/imgViewBin`.setBackgroundColor(Color.parseColor(mBinId))

        val arrAdapter = object : ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, mBines.map { it.description }) {
            override fun isEnabled(position: Int): Boolean {
                return position != binPosition
            }

            override fun getDropDownView(position: Int, convertView: View?,
                                         parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                if (position == binPosition) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY)
                } else {
                    tv.setTextColor(Color.BLACK)
                }
                return view
            }
        }
        arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        with(mDialogView.spinnerBin) {
            adapter = arrAdapter
            setSelection(binPosition, true)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    mBinNumber = mBines[position].binNumber
                    mBinNumberEdited = true
                }
            }
        }
    }

    override fun onRemoveProductClick(position: Int) {
        mTotalProducts -= mProductos[position].checkQty

        mProductos[position].checkQty = 0
        mProductosToEdit.add(mProductos[position])

        mProductos.removeAt(position)
        mDialogView.recyclerViewProductsInBag.adapter!!.notifyItemRemoved(position)
        mDialogView.recyclerViewProductsInBag.adapter!!.notifyItemRangeChanged(position, mProductos.size)

        mDialogView.txtViewTotal.text = mTotalProducts.toString()
    }

    private fun saveChanges() {
        // Este metodo verifica si el usuario ha hecho cambios en la cantidad del producto en algun producto.
        // De ser cierto busca el valor y lo actualiza.
        val products = JSONArray()

        for (position in 0 until mProductosToEdit.size){
            val newCheckQty = mProductosToEdit[position].checkQty

            val product = JSONObject()
            product.put("sku", mProductosToEdit[position].sku)
            product.put("new_check_qty", newCheckQty.toString())
            products.put(product)
        }

        if (products.length() >= 1 || mBinNumberEdited) {
            val params = hashMapOf("order_number" to mOrderNumber, "bag_id" to mBagId, "bin_number" to mBinNumber.toString(),
                    "products" to products.toString())

            client.makePostRequest(RequestCode.UPDATE_BAG.code, "update-bag", params)
        }


            dialog.dismiss()
    }

    override fun onProductClick(position: Int) {
        saveChanges()

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
        dialog.show(activity!!.supportFragmentManager, "ProductDialogFragment")

        this.dismiss()
    }
}
