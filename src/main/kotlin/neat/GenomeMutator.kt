package neat

import genome.ConnectionGene
import genome.Genome
import genome.NodeGene
import genome.NodeType
import kotlin.random.Random

class GenomeMutator private constructor(
    private val innovationTracker: InnovationTracker,
    private val inputSize: Int,
    private val outputSize: Int,) {

    private val initialTopology: Genome by lazy { createInitialGenome(inputSize, outputSize) }

    companion object {
        @Volatile
        private var INSTANCE: GenomeMutator? = null

        fun getInstance(innovationTracker: InnovationTracker,
                        inputSize: Int,
                        outputSize: Int): GenomeMutator {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GenomeMutator(innovationTracker, inputSize, outputSize).also { INSTANCE = it}
            }
        }
    }

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
}