package com.design2net.the_house.models

class Bin (val binNumber: Int, val binId: String, var description: String){

    var name: String = ""
    var color: String = ""
    var done: Int = 0

    constructor(binNumber: Int, binId: String, description: String, name: String, color: String, done: Int) : this(binNumber, binId, description) {
        this.name = name
        this.color = color
        this.done = done
    }

    companion object {
        var selected: Bin? = null
    }

    override fun toString(): String {
        return "Bin(binNumber=$binNumber, binId='$binId', description='$description')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bin

        if (binNumber != other.binNumber) return false

        return true
    }

    override fun hashCode(): Int {
        return binNumber
    }
}