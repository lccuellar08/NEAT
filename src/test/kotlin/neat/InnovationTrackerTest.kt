package neat

import genome.ConnectionGene
import genome.NodeGene
import genome.NodeType
import neat.InnovationTracker
import kotlin.test.Test
import kotlin.test.assertEquals

class InnovationTrackerTest {
    private val innovationTracker = InnovationTracker

    @Test
    fun testCreateInnovationNumber() {
        innovationTracker.clearSeenConnections()

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
        innovationTracker.clearSeenConnections()

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
        innovationTracker.clearSeenConnections()

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
}