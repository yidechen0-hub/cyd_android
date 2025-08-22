package com.cyd.cyd_android.serialization

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

// 复杂嵌套数据结构
data class ComplexData(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("isValid") val isValid: Boolean,
    @SerializedName("byteData") val byteData: ByteArray,
    @SerializedName("stringList") val stringList: List<String>,
    @SerializedName("nestedData") val nestedData: List<NestedData>
) : Parcelable {
    data class NestedData(
        @SerializedName("nestedId") val nestedId: Int,
        @SerializedName("nestedName") val nestedName: String,
        @SerializedName("values") val values: List<Double>,
        @SerializedName("subNested") val subNested: SubNestedData
    ) : Parcelable {
        data class SubNestedData(
            @SerializedName("flag") val flag: Boolean,
            @SerializedName("code") val code: String,
            @SerializedName("numbers") val numbers: List<Int>
        ) : Parcelable {
            constructor(parcel: Parcel) : this(
                parcel.readByte() != 0.toByte(),
                parcel.readString() ?: "",
                parcel.createIntArray()?.toList() ?: emptyList()
            )

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeByte(if (flag) 1 else 0)
                parcel.writeString(code)
                parcel.writeIntArray(numbers.toIntArray())
            }

            override fun describeContents(): Int = 0

            companion object CREATOR : Parcelable.Creator<SubNestedData> {
                override fun createFromParcel(parcel: Parcel): SubNestedData {
                    return SubNestedData(parcel)
                }

                override fun newArray(size: Int): Array<SubNestedData?> {
                    return arrayOfNulls(size)
                }
            }
        }

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString() ?: "",
            parcel.createDoubleArray()?.toList() ?: emptyList(),
            parcel.readParcelable(SubNestedData::class.java.classLoader)!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(nestedId)
            parcel.writeString(nestedName)
            parcel.writeDoubleArray(values.toDoubleArray())
            parcel.writeParcelable(subNested, flags)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<NestedData> {
            override fun createFromParcel(parcel: Parcel): NestedData {
                return NestedData(parcel)
            }

            override fun newArray(size: Int): Array<NestedData?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.createByteArray() ?: byteArrayOf(),
        parcel.createStringArrayList() ?: emptyList(),
        parcel.createTypedArrayList(NestedData.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (isValid) 1 else 0)
        parcel.writeByteArray(byteData)
        parcel.writeStringList(stringList)
        parcel.writeTypedList(nestedData)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ComplexData) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (timestamp != other.timestamp) return false
        if (isValid != other.isValid) return false
        if (!byteData.contentEquals(other.byteData)) return false
        if (stringList != other.stringList) return false
        if (nestedData != other.nestedData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + isValid.hashCode()
        result = 31 * result + byteData.contentHashCode()
        result = 31 * result + stringList.hashCode()
        result = 31 * result + nestedData.hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<ComplexData> {
        override fun createFromParcel(parcel: Parcel): ComplexData {
            return ComplexData(parcel)
        }

        override fun newArray(size: Int): Array<ComplexData?> {
            return arrayOfNulls(size)
        }
    }
}
