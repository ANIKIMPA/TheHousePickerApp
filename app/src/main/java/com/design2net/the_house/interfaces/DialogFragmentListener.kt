package com.design2net.the_house.interfaces

import com.design2net.the_house.models.Bin

interface DialogFragmentListener {
    fun onSaveCompleted()
    fun onBinCreated(bin: Bin)
    fun onDeleteBagClick(position: Int)
    fun onDeleteBinClick(position: Int)
}