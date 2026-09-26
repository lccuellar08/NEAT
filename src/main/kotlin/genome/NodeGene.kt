package genome

import kotlin.math.exp

enum class NodeType { INPUT, HIDDEN, BIAS, OUTPUT }
data class NodeGene(val nodeID: Int, val layerDepth: Double, val nodeType: NodeType) {

    companion object {
        val INPUT_DEPTH = 0.0
        val OUTPUT_DEPTH = 10.0

        fun calculateHiddenDepth(node1: NodeGene, node2: NodeGene): Double = (node2.layerDepth + node1.layerDepth) / 2.0
    }

    // φ(x) = 1/(1+e^(−4.9x))
    private fun sigmoid(x: Float): Float {
        return 1f / (1f + exp(-4.9f * x))
    }

    fun activation(input: Float): Float {
        return when(nodeType) {
            NodeType.INPUT -> input
            NodeType.BIAS -> input
            else -> sigmoid(input)
        }
    }

    override fun toString(): String {
        return "[$nodeID]: $nodeType"
    }
}