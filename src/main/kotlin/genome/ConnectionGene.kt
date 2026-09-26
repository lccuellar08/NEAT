package genome

data class ConnectionGene(val innovationNumber: Int, val inNodeGene: NodeGene, val outNodeGene: NodeGene,
    val weight: Float, val enabled: Boolean) {

    override fun toString(): String {
        return "\n$innovationNumber (${"%.2f".format(weight)}): ${inNodeGene.nodeID} -${if(enabled) "-" else "x" }> ${outNodeGene.nodeID}"
    }
}