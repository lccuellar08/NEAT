package neat

import genome.ConnectionGene
import genome.NodeGene
import genome.NodeType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class InnovationTrackerTest {
    private val innovationTracker = InnovationTracker

    @BeforeTest
    fun setUp() {
        innovationTracker.clearAll()
    }

    @AfterTest
    fun tearDown() {
        innovationTracker.clearAll()
    }


    // Test Connection Tracker
    @Test
    fun testCreateInnovationNumber() {
        val input1 = NodeGene(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
        val output1 = NodeGene(2, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)

        // First connection starts with an innovation number of 0
        val connection1 = ConnectionGene(
            innovationTracker.getOrCreateInnovationNumber(input1, output1),
            input1, output1, 1f, true
        )

        assert(connection1.innovationNumber == 0)
    }

    @Test
    fun testIncrementInnovationNumber() {
        val input1 = NodeGene(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
        val output1 = NodeGene(2, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)
        val hidden1 = NodeGene(3, NodeGene.calculateHiddenDepth(input1, output1), NodeType.HIDDEN)

        // First connection starts with an innovation number of 0
        val connection1 = ConnectionGene(
            innovationTracker.getOrCreateInnovationNumber(input1, output1),
            input1, output1, 1f, true
        )

        // Second connection should be connection1 + 1
        val connection2 = ConnectionGene(
            innovationTracker.getOrCreateInnovationNumber(input1, hidden1),
            input1, hidden1, 1f, true
        )

        assertEquals(connection1.innovationNumber + 1,
            connection2.innovationNumber)
    }

    @Test
    fun testRetrieveSameInnovationNumber() {
        val input1 = NodeGene(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
        val output1 = NodeGene(2, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)

        // First connection starts with an innovation number of 0
        val connection1 = ConnectionGene(
            innovationTracker.getOrCreateInnovationNumber(input1, output1),
            input1, output1, 1f, true
        )

        // Same connection with same nodeIDs, same weights
        val input1Copy1 = NodeGene(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
        val output1Copy1 = NodeGene(2, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)
        val connection2 = ConnectionGene(
            innovationTracker.getOrCreateInnovationNumber(input1Copy1, output1Copy1),
            input1Copy1, output1Copy1, 1f, true
        )

        assertEquals(connection1.innovationNumber, connection2.innovationNumber)

        // Same connection with same nodeIDs, different weights
        val input1Copy2 = NodeGene(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
        val output1Copy2 = NodeGene(2, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)
        val connection3 = ConnectionGene(
            innovationTracker.getOrCreateInnovationNumber(input1Copy2, output1Copy2),
            input1Copy2, output1Copy2, 2f, false
        )

        assertEquals(connection1.innovationNumber, connection3.innovationNumber)
    }

    // Test Node ID tracker
    @Test
    fun testGetNewNodeID() {
        val initialID = innovationTracker.getNewNodeID()
        val newNodeID = innovationTracker.getNewNodeID()

        assertNotEquals(initialID, newNodeID)
        assert(newNodeID > initialID)
    }

    @Test
    fun testNodeIDExists() {
        val nodeID = innovationTracker.getNewNodeID()
        assert(innovationTracker.nodeIDExists(nodeID))
    }

    @Test
    fun testNodeIDDoesNotExist() {
        val nodeID = innovationTracker.getNewNodeID()
        assertFalse(innovationTracker.nodeIDExists(nodeID + 1))
    }


    // Test Add Node to Connection
    @Test
    fun testAddNodeToConnection() {
        // Create Node 1 -> Node 2
        val inputNode = NodeGene(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
        val outputNode = NodeGene(2, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)

        // Connect them
        val innovationID = innovationTracker.getOrCreateInnovationNumber(inputNode, outputNode)
        val connectionGene = ConnectionGene(innovationID, inputNode, outputNode, 1f, true)

        // Create a new node between them
        val newNodeID = innovationTracker.getNewNodeIDBetweenConnection(connectionGene)

        // Node exists
        assert(innovationTracker.nodeIDExists(newNodeID))

        // If we create a new node, it should be different
        val anotherNewNodeID = innovationTracker.getNewNodeID()
        assertNotEquals(newNodeID, anotherNewNodeID)

        // If we try to create a new node in the same connection, shall return the same node ID
        val sameNewNodeID = innovationTracker.getNewNodeIDBetweenConnection(connectionGene)
        assertEquals(newNodeID, sameNewNodeID)
    }
}