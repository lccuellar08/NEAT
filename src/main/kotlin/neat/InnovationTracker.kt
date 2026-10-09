package neat

import genome.ConnectionGene
import genome.InnovationNumber
import genome.NodeGene
import genome.NodeID

object InnovationTracker {

    // Map of the Connection Genes to their innovation number
    private val seenConnections = mutableMapOf<ConnectionGene, InnovationNumber>()

    // Map of the Connection Genes to the Node ID of a node in the middle of the connection
    private val splitConnections = mutableMapOf<ConnectionGene, NodeID>()

    // Set of all Node IDs that have been seen
    private val seenNodeIDs = mutableSetOf<NodeID>()

    // Given two NodeGenes, we want the InnovationNumber associated with the ConnectionGene that connects
    // nodeGene1 -> nodeGene2
    // If it doesn't exist yet, create a new InnovationNumber and store it in the map
    fun getOrCreateInnovationNumber(nodeGene1: NodeGene, nodeGene2: NodeGene): InnovationNumber {
        val dummyConnection = createDummyConnection(nodeGene1, nodeGene2)
        val innovationNumber = seenConnections[dummyConnection]
        if(innovationNumber != null)
            return innovationNumber

        val newInnovationNumber = seenConnections.size
        seenConnections[dummyConnection] = newInnovationNumber
        return newInnovationNumber
    }

    // Given a ConnectionGene, we want the NodeID associated with the NodeGene
    // That is mutated between the NodeGenes of this connection
    // i.e If we had a ConnectionGene where n1 -> n2
    // And we need the NodeID of the connection between the two:
    // n1 -> n3 -> n2, we return 3
    fun getNewNodeIDBetweenConnection(connectionGene: ConnectionGene): NodeID {
        val seenHiddenNodeID = splitConnections[connectionGene]
        if(seenHiddenNodeID != null)
            return seenHiddenNodeID

        val newHiddenNodeID = getNewNodeID()
        splitConnections[connectionGene] = newHiddenNodeID

        return newHiddenNodeID
    }

    // We get the next available NodeID
    fun getNewNodeID(): NodeID {
        val newNodeID = seenNodeIDs.size
        seenNodeIDs.add(newNodeID)
        return newNodeID
    }

    fun nodeIDExists(nodeID: NodeID): Boolean {
        return seenNodeIDs.contains(nodeID)
    }

    fun clearSeenConnections() {
        seenConnections.clear()
    }

    fun clearSeenNodeIDs() {
        seenNodeIDs.clear()
    }

    fun clearSeenConnectionMutations() {
        splitConnections.clear()
    }

    fun clearAll() {
        clearSeenNodeIDs()
        clearSeenConnections()
        clearSeenConnectionMutations()
    }

    private fun createDummyConnection(nodeGene1: NodeGene, nodeGene2: NodeGene): ConnectionGene {
        return ConnectionGene(0, nodeGene1, nodeGene2, 0f, false)
    }
}