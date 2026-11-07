package neat

import genome.NodeGene
import genome.NodeType
import genome.edit
import genome.genome
import org.junit.jupiter.api.DisplayName
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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

        val genomeMutator = GenomeMutator(innovationTracker, inputSize, outputSize)
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
        val genomeMutator = GenomeMutator(innovationTracker, inputSize, outputSize)

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
        val genomeMutator = GenomeMutator(innovationTracker, inputSize, outputSize)

        val genomeA = genomeMutator.createInitialGenome()
        val genomeB = genomeMutator.createInitialGenome()

        val weightsA = genomeA.connectionGenes.map { it.weight }
        val weightsB = genomeB.connectionGenes.map { it.weight }

        assertNotEquals(weightsA, weightsB)
    }

    @Test
    @DisplayName("addConnectionGene adds a valid connectionGene to a not fully connected genome")
    fun testAddConnectionGeneNotConnected() {
        val inputSize = 2
        val outputSize = 2
        val genomeMutator = GenomeMutator(innovationTracker, inputSize, outputSize)

        // Create a genome with a hidden node only connected to a single input node gene
        val initialGenome = genomeMutator.createInitialGenome().edit {
            val inputNode = this.getNodes().first { it.nodeType == NodeType.INPUT }
            val hiddenNode = node(
                innovationTracker.getNewNodeID(), (NodeGene.INPUT_DEPTH + NodeGene.OUTPUT_DEPTH) / 2.0, NodeType.HIDDEN
            )
            connection(
                innovationTracker.getOrCreateInnovationNumber(inputNode, hiddenNode), inputNode, hiddenNode, 1f, true
            )
        }

        val mutatedGenome = genomeMutator.addConnectionGene(initialGenome)
        // mutatedGenome should have 1 more connection
        assertEquals(initialGenome.connectionGenes.size + 1, mutatedGenome.connectionGenes.size)

        // No duplicate connections are made
        assertEquals(mutatedGenome.connectionGenes.distinct().size, mutatedGenome.connectionGenes.size)

        // No nodes are added
        assertEquals(initialGenome.nodeGenes.size, mutatedGenome.nodeGenes.size)

        // Check new connection itself was created correctly
        val newConnection = (mutatedGenome.connectionGenes - initialGenome.connectionGenes.toSet()).single()

        assertNotEquals(NodeType.OUTPUT, newConnection.inNodeGene.nodeType)
        assertTrue(newConnection.inNodeGene.layerDepth < newConnection.outNodeGene.layerDepth)
        assertTrue(newConnection.enabled)
    }

    @Test
    @DisplayName("addConnectionGene does not change a fully connectedGenome")
    fun testAddConnectionGeneFullyConnected() {
        val inputSize = 2
        val outputSize = 2
        val genomeMutator = GenomeMutator(innovationTracker, inputSize, outputSize)

        // Create a genome with a hidden node, then fully connect it
        val fullyConnectedGenome = genomeMutator.createInitialGenome().edit {
            val biasNode = this.getNodes().first{it.nodeType == NodeType.BIAS }
            val hiddenNode = node(
                innovationTracker.getNewNodeID(), (NodeGene.INPUT_DEPTH + NodeGene.OUTPUT_DEPTH) / 2.0, NodeType.HIDDEN
            )

            // Connect all input nodes to the new hidden node
            this.getNodes().filter {it.nodeType == NodeType.INPUT }.forEach {inputNode ->
                connection(innovationTracker.getOrCreateInnovationNumber(inputNode, hiddenNode), inputNode, hiddenNode, 1f, true)
            }

            // Connect this hidden node to all output nodes
            this.getNodes().filter {it.nodeType == NodeType.OUTPUT}.forEach { outputNode ->
                connection(innovationTracker.getOrCreateInnovationNumber(hiddenNode, outputNode), hiddenNode, outputNode, 1f, true)
            }

            // Connect bias node to this node
            connection(innovationTracker.getOrCreateInnovationNumber(biasNode, hiddenNode), biasNode, hiddenNode, 1f, true)
        }

        val mutatedGenome = genomeMutator.addConnectionGene(fullyConnectedGenome)
        // mutatedGenome should have same connections
        assertEquals(fullyConnectedGenome.connectionGenes, mutatedGenome.connectionGenes)

        // No duplicate connections are made
        assertEquals(mutatedGenome.connectionGenes.distinct().size, mutatedGenome.connectionGenes.size)

        // No nodes are added
        assertEquals(fullyConnectedGenome.nodeGenes.size, mutatedGenome.nodeGenes.size)
    }

    @Test
    @DisplayName("addConnectionGene adds the only missing connection")
    fun testAddConnectionGeneOneMissingConnection() {
        val inputSize = 2
        val outputSize = 2
        val genomeMutator = GenomeMutator(innovationTracker, inputSize, outputSize)

        // Hidden node connected to every input and every output, but NOT to the bias node
        val initialGenome = genomeMutator.createInitialGenome().edit {
            val hiddenNode = node(
                innovationTracker.getNewNodeID(), (NodeGene.INPUT_DEPTH + NodeGene.OUTPUT_DEPTH) / 2.0, NodeType.HIDDEN
            )

            this.getNodes().filter { it.nodeType == NodeType.INPUT }.forEach { inputNode ->
                connection(innovationTracker.getOrCreateInnovationNumber(inputNode, hiddenNode), inputNode, hiddenNode, 1f, true)
            }

            this.getNodes().filter { it.nodeType == NodeType.OUTPUT }.forEach { outputNode ->
                connection(innovationTracker.getOrCreateInnovationNumber(hiddenNode, outputNode), hiddenNode, outputNode, 1f, true)
            }
        }

        val biasNode = initialGenome.nodeGenes.single { it.nodeType == NodeType.BIAS }
        val hiddenNode = initialGenome.nodeGenes.single { it.nodeType == NodeType.HIDDEN }

        // Precondition: bias -> hidden is the only missing connection
        assertFalse(initialGenome.containsConnectionGene(biasNode, hiddenNode))
        assertTrue(initialGenome.edit { connection(-1, biasNode, hiddenNode, 1f) }.isFullyConnected())

        val mutatedGenome = genomeMutator.addConnectionGene(initialGenome)
        val newConnection = (mutatedGenome.connectionGenes - initialGenome.connectionGenes.toSet()).single()

        assertEquals(biasNode.nodeID, newConnection.inNodeGene.nodeID)
        assertEquals(hiddenNode.nodeID, newConnection.outNodeGene.nodeID)
        assertTrue(newConnection.enabled)

        // The innovation number comes from the tracker for this exact pair
        assertEquals(
            innovationTracker.getOrCreateInnovationNumber(biasNode, hiddenNode),
            newConnection.innovationNumber
        )
    }
}