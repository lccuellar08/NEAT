package genome

data class ConnectionGene(val innovationNumber: Int, val inNodeGene: NodeGene, val outNodeGene: NodeGene,
    val weight: Float, val enabled: Boolean){

    override fun toString(): String {
        return "\n$innovationNumber (${"%.2f".format(weight)}): ${inNodeGene.nodeID} -${if(enabled) "-" else "x" }> ${outNodeGene.nodeID}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if(javaClass != other?.javaClass) return false

        other as ConnectionGene

        return this.inNodeGene.nodeID == other.inNodeGene.nodeID &&
                this.outNodeGene.nodeID == other.outNodeGene.nodeID
    }

    override fun hashCode(): Int {
        var result = innovationNumber
        result = 31 * result + inNodeGene.hashCode()
        result = 31 * result + outNodeGene.hashCode()
        return result
    }
}