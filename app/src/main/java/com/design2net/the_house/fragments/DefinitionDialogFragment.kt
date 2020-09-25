package com.design2net.the_house.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.design2net.the_house.R
import com.design2net.the_house.adapters.DefinitionDialogAdapter
import com.design2net.the_house.models.DefinitionsBin
import com.design2net.the_house.interfaces.ApiResponseListener
import com.design2net.the_house.network.OkHttpRequest
import com.design2net.the_house.network.RequestCode
import kotlinx.android.synthetic.main.dialog_definition.view.recyclerViewDefinitions
import org.json.JSONObject

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class DefinitionDialogFragment : BaseDialogFragment(), DefinitionDialogAdapter.DefinitionsBinListener, ApiResponseListener {

    private lateinit var client: OkHttpRequest
    private var columnCount = 3
    private lateinit var mDialogView: View
    private lateinit var mOrderNumber: String
    private val mDefinitions = ArrayList<DefinitionsBin>()

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            mDialogView = inflater.inflate(R.layout.dialog_definition, null)
            client = OkHttpRequest(getString(R.string.url_checker), this)

            mOrderNumber = arguments!!.getString("order_number")

            initRecyclerView()

            val map = hashMapOf("order_number" to mOrderNumber)
            client.makePostRequest(RequestCode.GET_DEFINITIONS.code, "get-definitions", map)

            builder.setView(mDialogView)
                    .setTitle(R.string.select_bin)
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        dialog.cancel()
                    }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun initRecyclerView() {
        with(mDialogView.recyclerViewDefinitions) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = DefinitionDialogAdapter(mDefinitions, this@DefinitionDialogFragment)
        }
    }

    override fun onApiResponse(requestCode: Int, response: JSONObject, item: Any?) {
        when (requestCode) {
            RequestCode.GET_DEFINITIONS.code -> processDefinitionsResponse(response)
        }
    }

    private fun processDefinitionsResponse(response: JSONObject) {
        activity!!.runOnUiThread {
            val jsonDefinitions = response.getJSONArray("definitions")

            for (i in 0 until jsonDefinitions.length()) {
                val productoObj = jsonDefinitions.getJSONObject(i)

                val binId = productoObj.getString("bin_id")
                val name = productoObj.getString("name")
                val color = productoObj.getString("color")
                val done = productoObj.getInt("done")

                mDefinitions.add(DefinitionsBin(binId, name, color, done))
            }

            mDialogView.recyclerViewDefinitions.adapter!!.notifyDataSetChanged()
        }
    }

    override fun onBinSelected(position: Int) {
        val mDefinitionBin = mDefinitions[position]

        val bundle = Bundle()
        bundle.putString("order_number", mOrderNumber)
        bundle.putString("name", mDefinitionBin.name)
        bundle.putString("bin_id", mDefinitionBin.bin_id)
        bundle.putString("color", mDefinitionBin.color)
        bundle.putInt("done", mDefinitionBin.done)

        val dialogFragment = BinDialogFragment()
        dialogFragment.arguments = bundle
        dialogFragment.show(fragmentManager, "BinDialogFragment")

        showKeyBoard()
        this.dismiss()
    }
}
