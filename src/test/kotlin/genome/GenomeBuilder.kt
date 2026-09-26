package genome

class GenomeBuilder {
    private val nodes = mutableListOf<NodeGene>()
    private val connections = mutableListOf<ConnectionGene>()

    fun node(id: Int, layerDepth: Double, type: NodeType): NodeGene {
        val newNode = NodeGene(id, layerDepth, type)
        nodes.add(newNode)
        return newNode
    }

    fun connection(innovationNumber: Int, fromNode: NodeGene, toNode: NodeGene, weight: Float, enabled: Boolean = true) {
        connections.add(ConnectionGene(innovationNumber, fromNode, toNode, weight, enabled))
    }

    fun build(): Genome {
        return Genome(nodes, connections)
    }
}

fun genome(block: GenomeBuilder.() -> Unit): Genome {
    return GenomeBuilder().apply(block).build()
}