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
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.design2net.the_house.R
import com.design2net.the_house.activity.BarcodeConverter
import com.design2net.the_house.adapters.SustitutoRecyclerViewAdapter
import com.design2net.the_house.interfaces.ApiResponseListener
import com.design2net.the_house.models.Producto
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_sustituto.view.*
import kotlinx.android.synthetic.main.dialog_sustituto.view.recyclerViewSustitutos
import org.json.JSONException
import org.json.JSONObject


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SustitutoDialogFragment : BaseDialogFragment(), ApiResponseListener, SustitutoRecyclerViewAdapter.SustitutoRecyclerViewListener {

    private lateinit var client: OkHttpRequest
    private lateinit var mOrderNumber: String
    private var mSku: String = ""
    private var mImageUrl: String = ""
    private var mDescription: String = ""
    private var mAdminComments: String = ""
    private var mPickerUser: String = ""
    private var mComments: String = ""
    private var mSubstitute: String = ""
    private lateinit var mDialogView: View
    private var mProductosSustitutos = ArrayList<Producto>()

    private var mListener: SustitutoListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the ProductsInBagDialogListener so we can send events to the host
            mListener = context as SustitutoListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement SustitutoListener"))
        }
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mOrderNumber = arguments!!.getString("orderNumber")
        mSku = arguments!!.getString("sku")

        val inflater = requireActivity().layoutInflater
        mDialogView = inflater.inflate(R.layout.dialog_sustituto, null)

        client = OkHttpRequest(getString(R.string.url_picker), this)
        requestAllSustitutos()

        return activity?.let {
            val builder = AlertDialog.Builder(it)

            with(mDialogView) {
                imgViewSustituirProducto.setOnClickListener { zoomImage() }
            }
            builder.setView(mDialogView)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    @SuppressLint("SetTextI18n")
    private fun initLayoutComponents() {
        if (mProductosSustitutos.isEmpty()) {
            with(mDialogView) {
                imgBtnSustitutoBarcode.visibility = View.VISIBLE
                edtSearchSustituto.visibility = View.VISIBLE
                edt_notas.visibility = View.VISIBLE
                btnEnviarSustitutos.visibility = View.VISIBLE
            }
        } else {
            with(mDialogView) {
                imgBtnSustitutoBarcode.visibility = View.GONE
                edtSearchSustituto.visibility = View.GONE
                edt_notas.visibility = View.GONE
                btnEnviarSustitutos.visibility = View.GONE
            }
        }

        with(mDialogView) {
            txtViewProductName.text = mDescription
            txtViewSustNotas.text = mComments
            txtViewSustMensaje.text = mSubstitute
            recyclerViewSustitutos.layoutManager = LinearLayoutManager(context)
            edt_notas.setText("El producto '${mDescription}' no está disponible.\nDeseamos ofrecerle algunos sustitutos.")
            recyclerViewSustitutos.adapter = SustitutoRecyclerViewAdapter(mProductosSustitutos, this@SustitutoDialogFragment)
            edtSearchSustituto.setOnEditorActionListener { textView, action, _ ->
                var handled = false
                if (action == EditorInfo.IME_ACTION_SEARCH) {
                    requestSustituto(textView.text.toString())
                    handled = true
                }

                handled
            }
            imgBtnSustitutoBarcode.setOnClickListener {
                requestSustituto(edtSearchSustituto.text.toString())
            }
            btnCancelarSustituto.setOnClickListener {
                dialog.dismiss()
            }
            btnEnviarSustitutos.setOnClickListener { requestEnviarSustitutosACliente() }
        }

        Picasso.get()
                .load(mImageUrl)
                .into(mDialogView.imgViewSustituirProducto)

        checkIfListHasProducts()
    }

    private fun requestAllSustitutos() {
        val params = hashMapOf(
                "order_number" to mOrderNumber,
                "sku" to mSku
        )

        client.makePostRequest(RequestCode.GET_SUSTITUTOS.code, "get-productos-sustitutos", params)
    }

    private fun requestEnviarSustitutosACliente() {
        if (mProductosSustitutos.isEmpty()) {
            Toast.makeText(context, "Debes agregar sustitutos", Toast.LENGTH_SHORT).show()
        } else {
            val params = hashMapOf(
                    "order_number" to mOrderNumber,
                    "sku" to mSku,
                    "not_available_reason" to "Out of Stock",
                    "message" to mDialogView.edt_notas.text.toString(),
                    "sustitutos" to mProductosSustitutos.joinToString(separator=",") { it.sku }
            )
            client.makePostRequest(RequestCode.ENVIAR_SUSTITUTO.code, "enviar-sustitutos", params)
            this.dismiss()
        }
    }

    internal fun requestSustituto(item: String) {
        val params = hashMapOf("item" to item)
        client.makePostRequest(RequestCode.GET_SUSTITUTO.code,
                "get-sustituto", params)

        mDialogView.edtSearchSustituto.setText("")
        hideKeyboard(mDialogView.edtSearchSustituto)
    }

    private fun zoomImage() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(mImageUrl.replace("/sm", "")), "image/*")
        activity!!.startActivity(intent)
    }

    override fun onApiResponse(requestCode: Int, response: JSONObject, item: Any?) {
        when(requestCode) {
            RequestCode.GET_SUSTITUTO.code -> processGetSustitutoResponse(response)
            RequestCode.ENVIAR_SUSTITUTO.code -> mListener!!.onSustitutosSent(response)
            RequestCode.GET_SUSTITUTOS.code -> processGetSustitutos(response)
        }
    }

    private fun processGetSustitutos (response: JSONObject) {
        activity!!.runOnUiThread {
            val productoObj = response.getJSONObject("producto")

            mSku = productoObj.getString("sku")
            mDescription = productoObj.getString("description")
            mImageUrl = productoObj.getString("image_url")
            mPickerUser = productoObj.getString("picker_user")
            mAdminComments = productoObj.getString("admin_comments")
            mComments = productoObj.getString("comments")
            mSubstitute = productoObj.getString("substitute")

            val sustitutosObj = response.getJSONArray("sustitutos")

            for (idx in 0 until sustitutosObj.length()) {
                val sustitutoObj = sustitutosObj.getJSONObject(idx)

                val sku = sustitutoObj.getString("sku")
                val upcStr = sustitutoObj.getString("upcs")
                val upc = upcStr.split(",")
                val description = sustitutoObj.getString("description")
                val imageUrl = sustitutoObj.getString("image_url")

                mProductosSustitutos.add(Producto(sku, upcStr, upc as ArrayList<String>, description, imageUrl))
            }

            initLayoutComponents()
            mDialogView.recyclerViewSustitutos.adapter!!.notifyDataSetChanged()
        }
    }

    private fun processGetSustitutoResponse(response: JSONObject) {
        activity!!.runOnUiThread {
            try {
                val jsonProductos = response.getJSONArray("productos")

                if (jsonProductos.length() <= 0) {
                    Toast.makeText(context, "Producto no encontrado.", Toast.LENGTH_SHORT).show()
                }

                for (idx in 0 until jsonProductos.length()) {
                    val productoObj = jsonProductos.getJSONObject(idx)

                    val sku = productoObj.getString("sku")
                    val upcStr = productoObj.getString("upcs")
                    val upc = upcStr.split(",")
                    val description = productoObj.getString("description")
                    val imageUrl = productoObj.getString("image_url")

                    mProductosSustitutos.add(Producto(sku, upcStr, upc, description, imageUrl))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("Niovan", "Sucedió un error: " + e.message)
            }

            updateData()
        }
    }

    private fun updateData() {
        checkIfListHasProducts()
        mDialogView.recyclerViewSustitutos.adapter!!.notifyDataSetChanged()
    }

    private fun checkIfListHasProducts() {
        if (mProductosSustitutos.isEmpty()) {
            mDialogView.txtViewNoSustitutoAdded.visibility = View.VISIBLE
            mDialogView.recyclerViewSustitutos.visibility = View.GONE
        } else {
            mDialogView.txtViewNoSustitutoAdded.visibility = View.GONE
            mDialogView.recyclerViewSustitutos.visibility = View.VISIBLE
        }
    }

    interface SustitutoListener {
        fun onSustitutosSent(response: JSONObject)
    }

    override fun onSustitutoDelete(position: Int) {
        mProductosSustitutos.removeAt(position)
        mDialogView.recyclerViewSustitutos.adapter!!.notifyItemRemoved(position)
        mDialogView.recyclerViewSustitutos.adapter!!.notifyItemRangeChanged(position, mProductosSustitutos.size)
    }
}