package com.design2net.the_house.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.Selection
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.design2net.the_house.R
import com.design2net.the_house.adapters.BinDialogFragmentAdapter
import com.design2net.the_house.models.Bag
import com.design2net.the_house.models.Bin
import com.design2net.the_house.models.DefinitionsBin
import com.design2net.the_house.interfaces.ApiResponseListener
import com.design2net.the_house.interfaces.DialogFragmentListener
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode
import kotlinx.android.synthetic.main.dialog_bin.view.*
import kotlinx.android.synthetic.main.dialog_bin.view.edtBinDescription
import org.json.JSONArray
import org.json.JSONObject

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class BinDialogFragment : BaseDialogFragment(), BinDialogFragmentAdapter.BinDialogListener, TextWatcher, ApiResponseListener {

    private lateinit var client: OkHttpRequest
    private lateinit var mOrderNumber: String
    private val mBines = ArrayList<Bin>()
    private lateinit var mDefinition: DefinitionsBin
    private lateinit var mDialogView: View
    private var isEditing = false
    private var descriptionEdited = false
    private var mThisBinPosition: Int = 0
    private var mBags = ArrayList<Bag>()

    private lateinit var mListener: DialogFragmentListener

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        mDialogView = inflater.inflate(R.layout.dialog_bin, null)

        client = OkHttpRequest(getString(R.string.url_checker), this)

        mOrderNumber = arguments!!.getString("order_number")
        mDefinition = DefinitionsBin(arguments!!.getString("bin_id"), arguments!!.getString("name"), arguments!!.getString("color"), arguments!!.getInt("done"))
        isEditing = arguments?.getBoolean("editing") ?: false
        val position = arguments!!.getInt("position")

        mDialogView.edtBinDescription.setText("${mDefinition.bin_id}-")
        mDialogView.layoutParent.requestFocus()

        if (mDefinition.name == "Items Sueltos") {
            log("items_sueltos")
            mDialogView.edtBinDescription.inputType = TYPE_CLASS_TEXT
        }

        if (!isEditing)
            mDialogView.edtBinDescription.requestFocus()

        initRecyclerView()
        
        if (isEditing) {
            mDialogView.btnEliminar.visibility = View.VISIBLE
            client.makePostRequest(RequestCode.GET_BINES.code, "get-bines", hashMapOf("order_number" to mOrderNumber))
            mDialogView.edtBinDescription.setText(arguments?.getString("desc"))
        }
            mDialogView.edtBinDescription.addTextChangedListener(this)

        val gd = GradientDrawable()
        gd.setStroke(16, Color.parseColor(mDefinition.color))
        mDialogView.edtBinDescription.background = gd

        mDialogView.btnEliminar.setOnClickListener {
            mListener.onDeleteBinClick(position)
            dialog.dismiss()
        }

        mDialogView.btnCancelar.setOnClickListener {
            hideKeyboard(mDialogView.edtBinDescription)
            dialog.cancel()
        }
        mDialogView.btnSave.setOnClickListener {
            if (mDialogView.edtBinDescription.text.length >= 3) {
                hideKeyboard(mDialogView.edtBinDescription)
                if (isEditing) {
                    createBinesJsonArray()
                } else {
                    val params = hashMapOf("order_number" to mOrderNumber, "bin_id" to mDefinition.bin_id,
                            "desc" to mDialogView.edtBinDescription.text.toString())
                    client.makePostRequest(RequestCode.CREATE_BIN.code, "create-bin", params)
                }

                dialog.dismiss()
            } else {
                Toast.makeText(context, "Agrega una descripción", Toast.LENGTH_SHORT).show()
            }
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            builder.setView(mDialogView)
                    .setTitle(mDefinition.name)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun initRecyclerView() {
        with(mDialogView.recyclerViewBinDialog) {
            layoutManager = LinearLayoutManager(context)
            adapter = BinDialogFragmentAdapter(mBags, mBines, this@BinDialogFragment, context)
        }
    }

    override fun onApiResponse(requestCode: Int, response: JSONObject, item: Any?) {
        when(requestCode) {
            RequestCode.GET_BINES.code -> processGetBinesResponse(response)
            RequestCode.UPDATE_BIN.code -> mListener.onSaveCompleted()
            RequestCode.CREATE_BIN.code -> processCreateBinResponse(response)
        }
    }

    private fun processCreateBinResponse(myResponse: JSONObject) {
        val jsonBin = myResponse.getJSONObject("bin")

        val binNumber = jsonBin.getInt("bin_number")
        val binId = jsonBin.getString("bin_id")
        val description = jsonBin.getString("description")
        val name = jsonBin.getString("name")
        val color = jsonBin.getString("color")
        val done = jsonBin.getInt("done")

        val mBin = Bin(binNumber, binId, description, name, color, done)

        mListener.onBinCreated(mBin)
    }

    private fun processGetBinesResponse(myResponse: JSONObject) {
        val jsonBines = myResponse.getJSONArray("bines")
        val mThisBinNumber = arguments!!.getInt("binNumber")

        for (i in 0 until jsonBines.length()) {
            val jsonBin = jsonBines.getJSONObject(i)

            val binNumber = jsonBin.getInt("bin_number")
            val binId = jsonBin.getString("bin_id")
            val description = jsonBin.getString("description")
            val name = jsonBin.getString("name")
            val color = jsonBin.getString("color")
            val done = jsonBin.getInt("done")
            val bagsIds = jsonBin.getJSONArray("bags_ids")

            mBines.add(0, Bin(binNumber, binId, description, name, color, done))

            if (binNumber == mThisBinNumber) {
                for (x in 0 until bagsIds.length()) {
                    val bagId = bagsIds.getString(x)
                    mBags.add(0, Bag(bagId, 0, binNumber))
                }
            }
        }

        val mThisBin = Bin(mThisBinNumber, arguments!!.getString("bin_id"), arguments!!.getString("desc"))
        mThisBinPosition = mBines.indexOf(mThisBin)

        activity!!.runOnUiThread{
            if (mBags.size >= 1) {
                mDialogView.recyclerViewBinDialog.adapter?.notifyDataSetChanged()
                mDialogView.lyt_th.visibility = View.VISIBLE
            }
        }
    }

    private fun createBinesJsonArray() {
        val bags = JSONArray()

        for (pos in 0 until mBags.size) {
            if (mBags[pos].binNumber != mBines[mThisBinPosition].binNumber) {
                val bag = JSONObject()

                bag.put("bag_id", mBags[pos].id)
                bag.put("bin_number", mBags[pos].binNumber.toString())
                bags.put(bag)
            }
        }

        if (bags.length() >= 1 || descriptionEdited) {
            val params = hashMapOf("order_number" to mOrderNumber, "bags" to bags.toString(),
                    "description" to mDialogView.edtBinDescription.text.toString(),
                    "bin_number" to mBines[mThisBinPosition].binNumber.toString())
            client.makePostRequest(RequestCode.UPDATE_BIN.code, "update-bin", params)
        }

            dialog.dismiss()
    }

   override fun onAttach(context: Context) {
       super.onAttach(context)
       if (context is DialogFragmentListener) {
           mListener = context
       } else {
           throw RuntimeException("$context must implement DialogFragmentListener")
       }
   }

    override fun onSpinnerItemSelected(spinnerPosition: Int, bagPosition: Int) {
        mBags[bagPosition].binNumber = mBines[spinnerPosition].binNumber
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    @SuppressLint("SetTextI18n")
    override fun afterTextChanged(s: Editable?) {
        if (!s.toString().startsWith("${mDefinition.bin_id}-")) {
            val text = mDialogView.edtBinDescription.text
            mDialogView.edtBinDescription.setText("${mDefinition.bin_id}-$text")
            Selection.setSelection(mDialogView.edtBinDescription.text, mDialogView.edtBinDescription.text.length)
        }

        if (isEditing)
            mBines[mThisBinPosition].description = mDialogView.edtBinDescription.text.toString()
        descriptionEdited = true
    }

    override fun onBagClick(position: Int) {
        val bundle = Bundle()
        bundle.putString("orderNumber", mOrderNumber)
        bundle.putString("bagId", mBags[position].id)
        bundle.putInt("binNumber", mBines[mThisBinPosition].binNumber)
        bundle.putString("color", mDefinition.color)

        val dialogFragment = BagDialogFragment()
        dialogFragment.arguments = bundle
        dialogFragment.show(activity?.supportFragmentManager, "BagDialogFragment")

        dialog.dismiss()
    }
}
