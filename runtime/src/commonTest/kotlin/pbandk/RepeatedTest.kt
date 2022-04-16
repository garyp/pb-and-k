package pbandk

import pbandk.testpb.Bar
import pbandk.testpb.bar
import pbandk.testpb.foo
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class RepeatedTest {
    @Test
    fun testRepeatedMessage() {
        // This used to fail because it assumed messages could be packed
        val bytes = byteArrayOf(10, 6, 10, 4, 102, 111, 111, 49, 10, 6, 10, 4, 102, 111, 111, 50)
        val expected = bar { foos = listOf(foo { `val` = "foo1" }, foo { `val` = "foo2" }) }

        assertContentEquals(expected.encodeToByteArray(), bytes)
        assertEquals(expected, Bar.decodeFromByteArray(bytes))
    }

    @Test
    fun testListWithSizeEquality() {
        val list1 = listOf(1, 2, 3)
        val list2 = ListWithSize.Builder<Int>().apply {
            add(1)
            add(2)
            add(3)
        }.fixed()
        // This used to fail because `ListWithSize` didn't properly delegate equality to the underlying list.
        // It only failed on Kotlin/JS and not on Kotlin/JVM because on Kotlin/JVM the `List` interface delegates to the
        // JVM version of `List`, which defines a correct version of the `equals()` method. Whereas the pure Kotlin
        // version of `List` does not include an `equals()` definition.
        assertEquals(list1, list2)
    }
}

