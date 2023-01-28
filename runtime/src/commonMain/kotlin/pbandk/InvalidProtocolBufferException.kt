package pbandk

import kotlin.js.JsExport

@JsExport
public class InvalidProtocolBufferException : RuntimeException {
    internal constructor(message: String) : super(message)
    internal constructor(message: String, cause: Throwable) : super(message, cause)

    internal companion object {
        internal fun truncatedMessage() = InvalidProtocolBufferException(
            "While parsing a protocol message, the input ended unexpectedly "
                    + "in the middle of a field.  This could mean either that the "
                    + "input has been truncated or that an embedded message "
                    + "misreported its own length."
        )

        internal fun negativeSize() = InvalidProtocolBufferException(
            "Encountered an embedded string or message which claimed to have negative size."
        )

        internal fun malformedVarint() = InvalidProtocolBufferException("Encountered a malformed varint.")

        internal fun invalidTag() = InvalidProtocolBufferException("Protocol message contained an invalid tag (zero).")

        internal fun invalidEndTag() = InvalidProtocolBufferException(
            "Protocol message end-group tag did not match expected tag."
        )

        internal fun invalidWireType() = InvalidProtocolBufferException("Protocol message tag had invalid wire type.")

        internal fun sizeLimitExceeded() = InvalidProtocolBufferException(
            "Protocol message was too large.  May be malicious.  "
                    + "Use a higher sizeLimit when reading the reading the input."
        )

        internal fun invalidJsonType() = InvalidProtocolBufferException("Protocol message JSON field had invalid type.")

    }
}
