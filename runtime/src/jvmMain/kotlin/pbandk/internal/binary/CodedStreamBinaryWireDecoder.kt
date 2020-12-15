package pbandk.internal.binary

import com.google.protobuf.CodedInputStream
import pbandk.ByteArr
import pbandk.InvalidProtocolBufferException
import pbandk.Message

internal class CodedStreamBinaryWireDecoder(private val stream: CodedInputStream) : BinaryWireDecoder {
    override fun readTag(): Tag = Tag(stream.readTag())

    private fun readVarintRawBytes(): ByteArray {
        val result = ByteArray(MAX_VARINT_SIZE)
        for (i in 0 until MAX_VARINT_SIZE) {
            val b = stream.readRawByte()
            result[i] = b
            if (b >= 0) return result.copyOf(i + 1)
        }
        throw InvalidProtocolBufferException("Encountered a malformed varint.")
    }

    private fun readLengthDelimitedRawBytes(): ByteArray {
        val lengthBytes = readRawBytes(WireType.VARINT)
        val length = CodedInputStream.newInstance(lengthBytes).readRawVarint32()
        val data = stream.readRawBytes(length)
        return lengthBytes.plus(data)
    }

    private fun readGroupRawBytes(): ByteArray {
        // TODO: properly read in the bytes instead of just skipping them
        stream.skipMessage()
        if (Tag(stream.lastTag).wireType != WireType.END_GROUP) {
            throw InvalidProtocolBufferException("Encountered a malformed START_GROUP tag with no matching END_GROUP tag")
        }
        return byteArrayOf()
    }

    override fun readRawBytes(type: WireType): ByteArray = when (type) {
        WireType.VARINT -> readVarintRawBytes()
        WireType.FIXED64 -> stream.readRawBytes(8)
        WireType.LENGTH_DELIMITED -> readLengthDelimitedRawBytes()
        WireType.START_GROUP -> readGroupRawBytes()
        WireType.FIXED32 -> stream.readRawBytes(4)
        else -> throw InvalidProtocolBufferException("Unrecognized wire type: $type")
    }

    override fun readDouble(): Double = stream.readDouble()

    override fun readFloat(): Float = stream.readFloat()

    override fun readInt32(): Int = stream.readInt32()

    override fun readInt64(): Long = stream.readInt64()

    override fun readUInt32(): Int = stream.readUInt32()

    override fun readUInt64(): Long = stream.readUInt64()

    override fun readSInt32(): Int = stream.readSInt32()

    override fun readSInt64(): Long = stream.readSInt64()

    override fun readFixed32(): Int = stream.readFixed32()

    override fun readFixed64(): Long = stream.readFixed64()

    override fun readSFixed32(): Int = stream.readSFixed32()

    override fun readSFixed64(): Long = stream.readSFixed64()

    override fun readBool(): Boolean = stream.readBool()

    override fun readString(): String = stream.readStringRequireUtf8()

    override fun readBytes(): ByteArr = ByteArr(stream.readByteArray())

    override fun <T : Message.Enum> readEnum(enumCompanion: Message.Enum.Companion<T>): T =
        enumCompanion.fromValue(stream.readEnum())

    override fun <T : Message> readMessage(messageCompanion: Message.Companion<T>): T {
        val oldLimit = stream.pushLimit(stream.readRawVarint32())
        val message = messageCompanion.decodeWith(BinaryMessageDecoder(this))
        if (!stream.isAtEnd) {
            throw InvalidProtocolBufferException("Not at the end of the current message limit as expected")
        }
        stream.popLimit(oldLimit)
        return message
    }

    override fun <T : Any> readPackedRepeated(readFn: BinaryWireDecoder.() -> T): Sequence<T> {
        return sequence {
            val oldLimit = stream.pushLimit(stream.readRawVarint32())
            while (!stream.isAtEnd) yield(readFn())
            stream.popLimit(oldLimit)
        }
    }
}