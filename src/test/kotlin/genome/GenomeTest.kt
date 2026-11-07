package genome

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class GenomeTest {
    private val testGenome = genome {
        val node1 = node(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
        val node2 = node(2, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)
        connection(1, node1, node2, 0.5f)
    }

    private val xorGenome = genome{
        val biasNode = node(0, NodeGene.INPUT_DEPTH, NodeType.BIAS)

        val inputNode1 = node(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
        val inputNode2 = node(2, NodeGene.INPUT_DEPTH, NodeType.INPUT)

        val outputNode1 = node(3, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)

        // Hidden node, depth is calculated between the inputs and outputs
        val hiddenNode1 = node(4,NodeGene.calculateHiddenDepth(inputNode1, outputNode1), NodeType.OUTPUT)

        // Input nodes to hidden
        connection(1, inputNode1, hiddenNode1, 2f)
        connection(2, inputNode2, hiddenNode1, 2f)

        // Bias to hidden
        connection(3, biasNode, hiddenNode1, -3f)

        // Input nodes to output
        connection(4, inputNode1, outputNode1, 4f)
        connection(5, inputNode2, outputNode1, 4f)

        // Bias to output
        connection(6, biasNode, outputNode1, -2f)

        // Hidden to output
        connection(7, hiddenNode1, outputNode1, -10f)
    }

    @Test
    fun testGenome() {
        val nodeGenes = testGenome.nodeGenes
        val connectionGenes = testGenome.connectionGenes

        val inputNode = nodeGenes.first { it.nodeType == NodeType.INPUT }
        val outputNode = nodeGenes.first { it.nodeType == NodeType.OUTPUT }
        val firstConnection = connectionGenes.first()

        assertEquals(inputNode, firstConnection.inNodeGene)
        assertEquals(outputNode, firstConnection.outNodeGene)
    }

    @Test
    fun testXORGenome() {
        // Test truth table for xor

        // 0 0 -> 0
        val resultZeroZero = xorGenome.feedForward(floatArrayOf(0f, 0f)).first()
        assert(resultZeroZero < 0.5f) {"0,0 result should be <= 0.5 but got $resultZeroZero"}

        // 0 1 -> 1
        val resultZeroOne = xorGenome.feedForward(floatArrayOf(0f, 1f)).first()
        assert(resultZeroOne >= 0.5f) {"0,1 result should be >= 0.5 but got $resultZeroOne"}

        // 1 0 -> 1
        val resultOneZero = xorGenome.feedForward(floatArrayOf(1f, 0f)).first()
        assert(resultOneZero >= 0.5f) {"1,0 result should be >= 0.5 but got $resultOneZero"}

        // 1 1 -> 0
        val resultOneOne = xorGenome.feedForward(floatArrayOf(1f, 1f)).first()
        assert(resultOneOne < 0.5f) {"1,1 result should be < 0.5 but got $resultOneOne"}
    }

    @Test
    fun testSmallGenomeFullyConnected() {
        val connectedGenome = genome {
            val inputNode1 = node(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
            val outputNode1 = node(2, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)
            val hiddenNode1 = node(3, NodeGene.calculateHiddenDepth(inputNode1, outputNode1), NodeType.HIDDEN)

            connection(1, inputNode1, outputNode1, 1f)
            connection(2, inputNode1, hiddenNode1, 1f)
            connection(3, hiddenNode1, outputNode1, 1f)
        }

        assert(connectedGenome.isFullyConnected())
    }

    @Test
    fun testLargeGenomeNotFullyConnected() {
        val connectedGenome = genome {
            val inputNode1 = node(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
            val outputNode1 = node(2, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)
            val hiddenNode1 = node(3, NodeGene.calculateHiddenDepth(inputNode1, outputNode1), NodeType.HIDDEN)
            val hiddenNode2 = node(4, NodeGene.calculateHiddenDepth(inputNode1, outputNode1), NodeType.HIDDEN)

            connection(1, inputNode1, outputNode1, 1f)
            connection(2, inputNode1, hiddenNode1, 1f)
            connection(3, hiddenNode1, outputNode1, 1f)
            connection(4, inputNode1, hiddenNode2, 1f)
            connection(5, hiddenNode2, outputNode1, 1f)
        }

        assert(connectedGenome.isFullyConnected())
    }

    @Test
    fun testGenomeNotFullyConnected() {
        val notConnectedGenome = genome {
            val inputNode1 = node(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
            val outputNode1 = node(2, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)
            val hiddenNode1 = node(3, NodeGene.calculateHiddenDepth(inputNode1, outputNode1), NodeType.HIDDEN)
            val hiddenNode2 = node(4, NodeGene.calculateHiddenDepth(inputNode1, outputNode1), NodeType.HIDDEN)

            connection(1, inputNode1, outputNode1, 1f)
            connection(2, inputNode1, hiddenNode1, 1f)
            connection(3, hiddenNode1, outputNode1, 1f)
            connection(4, inputNode1, hiddenNode2, 1f)
            // Missing hidden2 -> output1
        }

        assertFalse(notConnectedGenome.isFullyConnected())
    }

    @Test
    fun testContainsConnectionGene() {
        val inNodeGene = NodeGene(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
        val outNodeGene = NodeGene(2, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)
        assert(testGenome.containsConnectionGene(inNodeGene, outNodeGene))
    }
}