package genome

import kotlin.test.Test

class GenomeBuilder {
    private val nodes = mutableListOf<NodeGene>()
    private val connections = mutableListOf<ConnectionGene>()

    fun node(id: Int, layerDepth: Double, type: NodeType): NodeGene {
        val newNode = NodeGene(id, layerDepth, type)
        nodes.add(newNode)
        return newNode
    }

    fun connection(innovationNumber: Int, fromNode: NodeGene, toNode: NodeGene, weight: Double, enabled: Boolean = true) {
        connections.add(ConnectionGene(innovationNumber, fromNode, toNode, weight, enabled))
    }

    fun build(): Genome {
        return Genome(nodes, connections)
    }
}

fun genome(block: GenomeBuilder.() -> Unit): Genome {
    return GenomeBuilder().apply(block).build()
}

class GenomeTest {
    private val testGenome = genome {
        val node1 = node(1, NodeGene.INPUT_DEPTH, NodeType.INPUT)
        val node2 = node(2, NodeGene.OUTPUT_DEPTH, NodeType.OUTPUT)
        connection(1, node1, node2, 0.5)
    }

    @Test
    fun testGenome() {
        println(testGenome)
        assert(true)
    }
}