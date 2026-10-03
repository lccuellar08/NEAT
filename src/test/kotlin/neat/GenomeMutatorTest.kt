package neat

import genome.NodeType
import org.junit.jupiter.api.DisplayName
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GenomeMutatorTest {
    private val innovationTracker = InnovationTracker

    @BeforeTest
    fun setUp() {
        innovationTracker.clearAll()
    }

    @AfterTest
    fun tearDown() {
        innovationTracker.clearAll()
    }

    @Test
    @DisplayName("Test initial genome creation with 2 input nodes, 1 output node, and 1 bias node")
    fun testInitialGenomeBasicConfigurations() {
        val inputSize = 2
        val outputSize = 2

        val genomeMutator = GenomeMutator.getInstance(innovationTracker, inputSize, outputSize)
        val genome = genomeMutator.createInitialGenome()

        val allNodes = genome.nodeGenes
        val allConnections = genome.connectionGenes

        val inputNodes = allNodes.filter{it.nodeType == NodeType.INPUT}
        val outputNodes = allNodes.filter{it.nodeType == NodeType.OUTPUT}
        val biasNodes = allNodes.filter{it.nodeType == NodeType.BIAS}
        val hiddenNodes = allNodes.filter{it.nodeType == NodeType.HIDDEN}

        // Validate number of nodes created
        assertEquals(inputSize, inputNodes.size)
        assertEquals(outputSize, outputNodes.size)

        // Hardcoded, should only have 1 bias node
        assertEquals(1, biasNodes.size)

        // Initial genome should contain no hidden nodes
        assertEquals(0, hiddenNodes.size)

        // Connections should be N x M + 1 X M
        // N = input nodes
        // M = output nodes
        // 1 bias connection
        assertEquals(
            allConnections.size,
            inputSize * outputSize + 1 * outputSize
        )
    }

    @Test
    @DisplayName("Node IDs and innovation numbers stay stable across repeated calls to createInitialGenome")
    fun testInitialGenomeIsStableAcrossCalls() {
        val inputSize = 2
        val outputSize = 2
        val genomeMutator = GenomeMutator.getInstance(innovationTracker, inputSize, outputSize)

        val genomeA = genomeMutator.createInitialGenome()
        val genomeB = genomeMutator.createInitialGenome()

        assertEquals(
            genomeA.nodeGenes.sortedBy { it.nodeID },
            genomeB.nodeGenes.sortedBy { it.nodeID }
        )

        val innovationsA = genomeA.connectionGenes.associate {
            (it.inNodeGene.nodeID to it.outNodeGene.nodeID) to it.innovationNumber
        }
        val innovationsB = genomeB.connectionGenes.associate {
            (it.inNodeGene.nodeID to it.outNodeGene.nodeID) to it.innovationNumber
        }
        assertEquals(innovationsA, innovationsB)
    }

    @Test
    @DisplayName("createInitialGenome rolls fresh random weights on every call")
    fun testInitialGenomeRandomizesWeightsPerCall() {
        val inputSize = 2
        val outputSize = 2
        val genomeMutator = GenomeMutator.getInstance(innovationTracker, inputSize, outputSize)

        val genomeA = genomeMutator.createInitialGenome()
        val genomeB = genomeMutator.createInitialGenome()

        val weightsA = genomeA.connectionGenes.map { it.weight }
        val weightsB = genomeB.connectionGenes.map { it.weight }

        assertNotEquals(weightsA, weightsB)
    }
}