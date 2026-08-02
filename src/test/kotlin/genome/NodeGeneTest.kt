package genome

import neat.greeting
import kotlin.test.Test
import kotlin.test.assertEquals

class NodeGeneTest {

    @Test
    fun `greeting returns hello world`() {
        assertEquals("Hello world!", greeting())
    }
}