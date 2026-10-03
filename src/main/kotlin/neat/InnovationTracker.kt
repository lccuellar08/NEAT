package neat

import genome.ConnectionGene
import genome.NodeGene

object InnovationTracker {
    private val seenConnections = mutableMapOf<ConnectionGene, Int>()
    private val seenNodeIDs = mutableSetOf<Int>()

    fun getOrCreateInnovationNumber(nodeGene1: NodeGene, nodeGene2: NodeGene): Int {
        val dummyConnection = createDummyConnection(nodeGene1, nodeGene2)
        val innovationNumber = seenConnections[dummyConnection]
        if(innovationNumber != null)
            return innovationNumber

        val newInnovationNumber = seenConnections.size
        seenConnections[dummyConnection] = newInnovationNumber
        return newInnovationNumber
    }

    private fun createDummyConnection(nodeGene1: NodeGene, nodeGene2: NodeGene): ConnectionGene {
        return ConnectionGene(0, nodeGene1, nodeGene2, 0f, false)
    }

    fun getNewNodeID(): Int {
        val newNodeID = seenNodeIDs.size
        seenNodeIDs.add(newNodeID)
        return newNodeID
    }

    fun nodeIDExists(nodeID: Int): Boolean {
        return seenNodeIDs.contains(nodeID)
    }

    fun clearSeenConnections() {
        seenConnections.clear()
    }

    fun clearSeenNodeIDs() {
        seenNodeIDs.clear()
    }

    fun clearAll() {
        clearSeenNodeIDs()
        clearSeenConnections()
    }
}