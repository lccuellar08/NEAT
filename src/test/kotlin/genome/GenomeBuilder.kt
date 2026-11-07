package genome

class GenomeBuilder(base: Genome? = null) {
    private val nodes = base?.nodeGenes?.toMutableList() ?: mutableListOf<NodeGene>()
    private val connections = base?.connectionGenes?.toMutableList() ?: mutableListOf<ConnectionGene>()

    fun node(id: NodeID, layerDepth: Double, type: NodeType): NodeGene {
        val newNode = NodeGene(id, layerDepth, type)
        nodes.add(newNode)
        return newNode
    }

    fun connection(innovationNumber: InnovationNumber, fromNode: NodeGene, toNode: NodeGene, weight: Float, enabled: Boolean = true) {
        connections.add(ConnectionGene(innovationNumber, fromNode, toNode, weight, enabled))
    }

    fun build(): Genome {
        return Genome(nodes.toList(), connections.toList())
    }

    fun getNodes(): List<NodeGene> {
        return this.nodes.toList()
    }

    fun getConnections(): List<ConnectionGene> {
        return this.connections.toList()
    }
}

fun genome(block: GenomeBuilder.() -> Unit): Genome {
    return GenomeBuilder().apply(block).build()
}

fun Genome.edit(block: GenomeBuilder.() -> Unit): Genome {
    return GenomeBuilder(this).apply(block).build()
}