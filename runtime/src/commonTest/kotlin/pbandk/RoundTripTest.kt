package pbandk

import pbandk.testpb.Foo
import pbandk.testpb.foo
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class RoundTripTest {
    @Test
    fun testFoo() {
        val foo = foo { `val` = "Hello world!" }
        val bytes = foo.encodeToByteArray()
        assertContentEquals((byteArrayOf(10, 12) + "Hello world!".map { it.code.toByte() }.toByteArray()), bytes)
        assertEquals(foo, Foo.decodeFromByteArray(bytes))
    }
}