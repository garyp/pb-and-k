package pbandk.internal.binary

import pbandk.ByteArr
import pbandk.FieldDescriptor
import pbandk.Message
import pbandk.MessageMap

internal const val MAX_VARINT_SIZE = 10

internal interface Sizer {
    fun tagSize(fieldNum: Int): Int
    fun doubleSize(value: Double): Int
    fun floatSize(value: Float): Int
    fun int32Size(value: Int): Int
    fun int64Size(value: Long): Int
    fun uInt32Size(value: Int): Int
    fun uInt64Size(value: Long): Int
    fun sInt32Size(value: Int): Int
    fun sInt64Size(value: Long): Int
    fun fixed32Size(value: Int): Int
    fun fixed64Size(value: Long): Int
    fun sFixed32Size(value: Int): Int
    fun sFixed64Size(value: Long): Int
    fun boolSize(value: Boolean): Int
    fun stringSize(value: String): Int
    fun bytesSize(value: ByteArr): Int
    fun <T : Message.Enum> enumSize(value: T): Int
    fun <T : Message> lengthPrefixedMessageSize(value: T): Int
    fun <T : Message> delimitedMessageSize(fieldNum: Int, value: T): Int
    fun <T : Message> rawMessageSize(message: T): Int
    fun <T> repeatedSize(fieldNum: Int, list: List<T>, valueType: FieldDescriptor.Type, packed: Boolean): Int
    fun <T> packedRepeatedSize(list: List<T>, sizeFn: (T) -> Int): Int
    fun mapSize(map: Map<*, *>, entryCompanion: MessageMap.Entry.Companion<*, *>): Int
}

internal expect val PlatformSizer: Sizer