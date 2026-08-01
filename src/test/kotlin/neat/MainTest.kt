package neat

import kotlin.test.Test
import kotlin.test.assertEquals

class MainTest {
    @Test
    fun `greeting returns hello world`() {
        assertEquals("Hello world!", greeting())
    }
}
