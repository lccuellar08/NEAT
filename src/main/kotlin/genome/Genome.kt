package genome

data class Genome(val nodeGenes: List<NodeGene>, val connectionGenes: List<ConnectionGene>) {

    fun feedForward(input: FloatArray): FloatArray {
        val nodeMap: MutableMap<NodeGene, Float> = mutableMapOf()

        // Pass input values into input nodes
        nodeGenes
            .filter {it.nodeType == NodeType.INPUT}
            .sortedBy{it.nodeID}
            .forEachIndexed {index, node ->
                nodeMap[node] = input[index]
            }

        // Pass input values into input nodes
        val biasNode = nodeGenes.single { it.nodeType == NodeType.BIAS }
        nodeMap[biasNode] = 1f


        // Feed forward through layers
        nodeGenes
            .groupBy { it.layerDepth }
            .entries
            .sortedBy { it.key }
            .forEach {(_, nodesInLayer) ->
                nodesInLayer.forEach {node ->
                    val thisNodeValue = node.activation(nodeMap[node] ?: 1f)
                    val enabledConnectionsFromNode = connectionGenes.filter{it.inNodeGene == node && it.enabled}

                    enabledConnectionsFromNode.forEach {connectionGene ->
                        val oldConnectionValue = (nodeMap[connectionGene.outNodeGene] ?: 0f)
                        nodeMap[connectionGene.outNodeGene] = oldConnectionValue + (thisNodeValue * connectionGene.weight)
                    }
                }
            }

        // Create output array from the value in the output nodes
        val output = nodeGenes
            .filter { it.nodeType == NodeType.OUTPUT }
            .sortedBy { it.nodeID }
            .map { node ->
                return@map node.activation(nodeMap[node] ?: 1f)
            }
            .toFloatArray()

        return output
    }

    fun containsConnectionGene(inNodeGene: NodeGene, outNodeGene: NodeGene): Boolean {
        val dummyConnectionGene = ConnectionGene(
            -1, inNodeGene, outNodeGene, 0f, true
        )

        return connectionGenes.contains(dummyConnectionGene)
    }

    fun isFullyConnected(): Boolean {
        this
            .nodeGenes
            .groupBy { it.layerDepth } // Go through each layer
            .entries
            .sortedBy { it.key }
            .forEach {( layerDepth,nodesInLayer) ->
                val nodesAfterThisLayer = this.nodeGenes.filter {it.layerDepth > layerDepth}
                nodesInLayer.forEach {inNode ->
                    nodesAfterThisLayer.forEach {outNode ->
                        if(! this.containsConnectionGene(inNode, outNode)) {
                            return false
                        }
                    }
                }
            }

        return true
    }
}