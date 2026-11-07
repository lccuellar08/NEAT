package neat

import genome.ConnectionGene
import genome.Genome
import genome.NodeGene
import genome.NodeType
import kotlin.random.Random

class GenomeMutator (
    private val innovationTracker: InnovationTracker,
    private val inputSize: Int,
    private val outputSize: Int) {

    private val initialTopology: Genome by lazy { createInitialGenome(inputSize, outputSize) }

    private fun createInitialGenome(inputSize: Int, outputSize: Int): Genome {
        // Create list of inputNodes
        val inputNodes = (0 until inputSize).map {
            val nodeID = innovationTracker.getNewNodeID()
            return@map NodeGene(nodeID, NodeGene.INPUT_DEPTH, NodeType.INPUT)
        }

        // Create list of output nodes
        val outputNodes = (0 until outputSize).map {
            val nodeID = innovationTracker.getNewNodeID()
            return@map NodeGene(nodeID, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)
        }

        // Create bias node
        val biasNode = NodeGene(innovationTracker.getNewNodeID(), NodeGene.INPUT_DEPTH, NodeType.BIAS)

        // Connect all input nodes to output nodes
        val connectionGenes = inputNodes.map {inputNode ->
            return@map outputNodes.map{outputNode ->
                val innovationNumber = innovationTracker.getOrCreateInnovationNumber(inputNode, outputNode)
                return@map ConnectionGene(innovationNumber, inputNode, outputNode, 1f, true)
            }
        }.flatten()

        // Connect bias node to all output nodes
        val biasConnectionGenes = outputNodes.map {outputNode ->
            val innovationNumber = innovationTracker.getOrCreateInnovationNumber(biasNode, outputNode)
            return@map ConnectionGene(innovationNumber, biasNode, outputNode, 1f, true)
        }

        // Create genome with all nodes and all connections
        return Genome(
            inputNodes + outputNodes + listOf(biasNode),
            connectionGenes + biasConnectionGenes)
    }

    private fun randomWeight(): Float {
        return Random.nextFloat()
    }

    fun createInitialGenome(): Genome {
        val connections = initialTopology.connectionGenes.map { it.copy(weight = randomWeight()) }
        return Genome(initialTopology.nodeGenes, connections)
    }

    /***
     * Given a node with depth h, find a random node with depth > h
     * That is not currently connected
     */
    fun addConnectionGene(genome: Genome): Genome {
        val candidates = genome.nodeGenes
            .filter { it.nodeType != NodeType.OUTPUT }
            .flatMap { fromNode ->
                genome.nodeGenes
                    .filter { it.layerDepth > fromNode.layerDepth && !genome.containsConnectionGene(fromNode, it) }
                    .map { toNode -> fromNode to toNode }
            }

        val (fromNode, toNode) = candidates.randomOrNull() ?: return genome

        val newConnectionGene = ConnectionGene(
            innovationTracker.getOrCreateInnovationNumber(fromNode, toNode), fromNode, toNode, randomWeight(), true
        )
        return genome.copy(connectionGenes = genome.connectionGenes + newConnectionGene)
    }

    /**
     * Add a new NodeGene between a random enabled connection
     * Creates a new genome, doesn't modify the original genome
     */
    fun addNodeGene(genome: Genome): Genome {
        // Pick a random enabled connection
        val randomConnection = genome.connectionGenes
            .filter{ it.enabled }
            .randomOrNull() ?: return genome

        // TODO: same connection split twice in one genome -> duplicate node ID (see tech debt)
        val newNodeID = innovationTracker.getNewNodeIDBetweenConnection(randomConnection)
        val newNodeGene = NodeGene(
            newNodeID,
            NodeGene.calculateHiddenDepth(randomConnection.inNodeGene, randomConnection.outNodeGene),
            NodeType.HIDDEN
        )

        val firstNewConnectionGene = ConnectionGene(
            innovationTracker.getOrCreateInnovationNumber(randomConnection.inNodeGene, newNodeGene),
            randomConnection.inNodeGene,
            newNodeGene,
            1f,
            true
        )
        val secondNewConnectionGene = ConnectionGene(
            innovationTracker.getOrCreateInnovationNumber(newNodeGene, randomConnection.outNodeGene),
            newNodeGene,
            randomConnection.outNodeGene,
            randomConnection.weight,
            true
        )

        return Genome(
            genome.nodeGenes + newNodeGene,
            genome.connectionGenes.filter{it.innovationNumber != randomConnection.innovationNumber}
                    + firstNewConnectionGene + secondNewConnectionGene
                    + randomConnection.copy(enabled=false))
    }

    fun clearInnovationTracker() {
        this.innovationTracker.clearAll()
    }
}