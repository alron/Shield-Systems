package me.passivepicasso.shieldsystems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockMatrixNode {
    public enum Axis {
        X, Y, Z
    }

    // X, Y, Z
    private final HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>> matrixNodes;

    private final int                                                                   x, y, z;

    private final String                                                                world;

    private final Block                                                                 block;

    private BlockMatrixNode                                                             nextX;
    private BlockMatrixNode                                                             previousX;
    private BlockMatrixNode                                                             nextY;
    private BlockMatrixNode                                                             previousY;
    private BlockMatrixNode                                                             nextZ;
    private BlockMatrixNode                                                             previousZ;

    private boolean                                                                     isComplete;
    private Set<Material>                                                               filter;

    public BlockMatrixNode( Block block, HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>> matrixNodes ) {
        this.matrixNodes = matrixNodes;
        isComplete = false;
        world = block.getWorld().getName();
        this.block = block;
        x = block.getX();
        y = block.getY();
        z = block.getZ();
        if (matrixNodes.containsKey(x + 1) && matrixNodes.get(x + 1).containsKey(y) && matrixNodes.get(x + 1).get(y).containsKey(z)) {
            BlockMatrixNode nextX = matrixNodes.get(x + 1).get(y).get(z);
            setSouth(nextX);
            nextX.setNorth(this);
        }
        if (matrixNodes.containsKey(x - 1) && matrixNodes.get(x - 1).containsKey(y) && matrixNodes.get(x - 1).get(y).containsKey(z)) {
            BlockMatrixNode previousX = matrixNodes.get(x - 1).get(y).get(z);
            setNorth(previousX);
            previousX.setNorth(this);
        }
        if (matrixNodes.containsKey(x) && matrixNodes.get(x).containsKey(y + 1) && matrixNodes.get(x).get(y + 1).containsKey(z)) {
            BlockMatrixNode nextY = matrixNodes.get(x).get(y + 1).get(z);
            setUp(nextY);
            nextY.setDown(this);
        }
        if (matrixNodes.containsKey(x) && matrixNodes.get(x).containsKey(y - 1) && matrixNodes.get(x).get(y - 1).containsKey(z)) {
            BlockMatrixNode previousY = matrixNodes.get(x).get(y - 1).get(z);
            setDown(previousY);
            previousY.setUp(this);
        }
        if (matrixNodes.containsKey(x) && matrixNodes.get(x).containsKey(y) && matrixNodes.get(x).get(y).containsKey(z + 1)) {
            BlockMatrixNode nextZ = matrixNodes.get(x).get(y).get(z + 1);
            setWest(nextZ);
            nextZ.setEast(this);
        }
        if (matrixNodes.containsKey(x) && matrixNodes.get(x).containsKey(y) && matrixNodes.get(x).get(y).containsKey(z - 1)) {
            BlockMatrixNode previousZ = matrixNodes.get(x).get(y).get(z - 1);
            setEast(previousZ);
            previousZ.setWest(this);
        }

        if (matrixNodes.containsKey(x)) {
            if (matrixNodes.get(x).containsKey(y)) {
                if (matrixNodes.get(x).get(y).containsKey(z)) {
                    return;
                } else {
                    matrixNodes.get(x).get(y).put(z, this);
                }
            } else {
                matrixNodes.get(x).put(y, new HashMap<Integer, BlockMatrixNode>());
                matrixNodes.get(x).get(y).put(z, this);
            }
        } else {
            matrixNodes.put(x, new HashMap<Integer, HashMap<Integer, BlockMatrixNode>>());
            matrixNodes.get(x).put(y, new HashMap<Integer, BlockMatrixNode>());
            matrixNodes.get(x).get(y).put(z, this);
        }
    }

    public BlockMatrixNode( Block block, HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>> matrixNodes, Set<Material> include ) {
        this(block, matrixNodes);
        this.filter = include;
    }

    public void completeStructure() {
        isComplete = true;
    }

    @Override
    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BlockMatrixNode other = (BlockMatrixNode) obj;
        if (x != other.x) {
            return false;
        }
        if (y != other.y) {
            return false;
        }
        if (z != other.z) {
            return false;
        }
        return true;
    }

    public boolean equalsX( int x ) {
        return this.x == x;
    }

    public boolean equalsY( int y ) {
        return this.y == y;
    }

    public boolean equalsZ( int z ) {
        return this.z == z;
    }

    public Block getBlock() {
        if (block == null) {
        }
        return block;
    }

    public HashSet<Block> getBlockMatrix() {
        HashSet<Block> blocks = new HashSet<Block>();
        for (Integer x : matrixNodes.keySet()) {
            for (Integer y : matrixNodes.get(x).keySet()) {
                for (Integer z : matrixNodes.get(x).get(y).keySet()) {
                    blocks.add(matrixNodes.get(x).get(y).get(z).getBlock());
                }
            }
        }
        return blocks;
    }

    public HashSet<BlockMatrixNode> getBlockMatrixNodes() {
        HashSet<BlockMatrixNode> matrix = new HashSet<BlockMatrixNode>();
        for (Integer x : matrixNodes.keySet()) {
            for (Integer y : matrixNodes.get(x).keySet()) {
                for (Integer z : matrixNodes.get(x).get(y).keySet()) {
                    matrix.add(matrixNodes.get(x).get(y).get(z));
                }
            }
        }
        return matrix;
    }

    /**
     * 
     * @param plane
     * @param a
     *            either X or Y
     * @param b
     *            either Y or Z
     * @return set of
     */
    public Set<BlockMatrixNode> getBlockPlane( Axis axis, int value ) {
        HashSet<BlockMatrixNode> nodes = new HashSet<BlockMatrixNode>();
        switch (axis) {
            case X:
                for (Integer y : matrixNodes.get(value).keySet()) {
                    for (Integer z : matrixNodes.get(value).get(y).keySet()) {
                        nodes.add(matrixNodes.get(value).get(y).get(z));
                    }
                }
                break;
            case Y:
                for (Integer x : matrixNodes.keySet()) {
                    if (matrixNodes.get(x).containsKey(value)) {
                        for (Integer z : matrixNodes.get(x).get(value).keySet()) {
                            nodes.add(matrixNodes.get(x).get(y).get(z));
                        }
                    }
                }
                break;
            case Z:
                for (Integer x : matrixNodes.keySet()) {
                    for (Integer y : matrixNodes.get(x).keySet()) {
                        if (matrixNodes.get(x).get(y).containsKey(value)) {
                            nodes.add(matrixNodes.get(x).get(y).get(value));
                        }
                    }
                }
                break;
        }
        return nodes;
    }

    public BlockMatrixNode getDown() {
        Block block = this.block.getRelative(0, -1, 0);
        if (filter != null) {
            if (filter.contains(block.getType())) {
                if ((previousY == null) && !isComplete) {
                    previousY = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                    previousY.setUp(this);
                }
            }
        } else {
            if ((previousY == null) && !isComplete) {
                previousY = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                previousY.setUp(this);
            }
        }
        return previousY;
    }

    public BlockMatrixNode getEast() {
        Block block = this.block.getRelative(0, 0, -1);
        if (filter != null) {
            if (filter.contains(block.getType())) {
                if ((previousZ == null) && !isComplete) {
                    previousZ = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                    previousZ.setWest(this);
                }
            }
        } else {
            if ((previousZ == null) && !isComplete) {
                previousZ = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                previousZ.setWest(this);
            }
        }
        return previousZ;
    }

    /**
     * get
     * 
     * @param block
     * @return
     */
    public BlockMatrixNode getMatrixNode( Block block ) {
        if (matrixNodes.containsKey(block.getX())) {
            if (matrixNodes.get(block.getX()).containsKey(block.getY())) {
                if (matrixNodes.get(block.getX()).get(block.getY()).containsKey(block.getZ())) {
                    return matrixNodes.get(block.getX()).get(block.getY()).get(block.getZ());
                }
            }
        }
        return null;
    }

    public BlockMatrixNode getNorth() {
        Block block = this.block.getRelative(-1, 0, 0);
        if (filter != null) {
            if (filter.contains(block.getType())) {
                if ((previousX == null) && !isComplete) {
                    previousX = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                    previousX.setSouth(this);
                }
            }
        } else {
            if ((previousX == null) && !isComplete) {
                previousX = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                previousX.setSouth(this);
            }
        }
        return previousX;
    }

    public BlockMatrixNode getSouth() {
        Block block = this.block.getRelative(1, 0, 0);
        if (filter != null) {
            if (filter.contains(block.getType())) {
                if ((nextX == null) && !isComplete) {
                    nextX = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                    nextX.setNorth(this);
                }
            }
        } else {
            if ((nextX == null) && !isComplete) {
                nextX = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                nextX.setNorth(this);
            }
        }
        return nextX;
    }

    public BlockMatrixNode getUp() {
        Block block = this.block.getRelative(0, 1, 0);
        if (filter != null) {
            if (filter.contains(block.getType())) {
                if ((nextY == null) && !isComplete) {
                    nextY = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                    nextY.setDown(this);
                }
            }
        } else {
            if ((nextY == null) && !isComplete) {
                nextY = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                nextY.setDown(this);
            }
        }
        return nextY;
    }

    public BlockMatrixNode getWest() {
        Block block = this.block.getRelative(0, 0, 1);
        if (filter != null) {
            if (filter.contains(block.getType())) {
                if ((nextZ == null) && !isComplete) {
                    nextZ = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                    nextZ.setEast(this);
                }
            }
        } else {
            if ((nextZ == null) && !isComplete) {
                nextZ = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                nextZ.setEast(this);
            }
        }
        return nextZ;
    }

    public String getWorld() {
        return world;
    }

    public boolean hasDown() {
        return nextX != null;
    }

    public boolean hasEast() {
        return nextX != null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    public boolean hasNorth() {
        return nextX != null;
    }

    public boolean hasSouth() {
        return nextX != null;
    }

    public boolean hasUp() {
        return nextY != null;
    }

    public boolean hasWest() {
        return nextZ != null;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setDown( BlockMatrixNode previousY ) {
        if (filter != null) {
            if (filter.contains(nextX.getBlock().getType())) {
                this.previousY = previousY;
            }
        }
    }

    public void setEast( BlockMatrixNode previousZ ) {
        if (filter != null) {
            if (filter.contains(nextX.getBlock().getType())) {
                this.previousZ = previousZ;
            }
        }
    }

    public void setNorth( BlockMatrixNode previousX ) {
        if (filter != null) {
            if (filter.contains(previousX.getBlock().getType())) {
                this.previousX = previousX;
            }
        }
    }

    public void setSouth( BlockMatrixNode nextX ) {
        if (filter != null) {
            if (filter.contains(nextX.getBlock().getType())) {
                this.nextX = nextX;
            }
        }
    }

    public void setUp( BlockMatrixNode nextY ) {
        if (filter != null) {
            if (filter.contains(nextY.getBlock().getType())) {
                this.nextY = nextY;
            }
        }
    }

    public void setWest( BlockMatrixNode nextZ ) {
        if (filter != null) {
            if (filter.contains(nextZ.getBlock().getType())) {
                this.nextZ = nextZ;
            }
        }
    }

    private BlockMatrixNode addOrGetNode( BlockMatrixNode node ) {
        if (matrixNodes.containsKey(node.x)) {
            HashMap<Integer, HashMap<Integer, BlockMatrixNode>> xNode = matrixNodes.get(node.x);
            if (xNode.containsKey(node.y)) {
                HashMap<Integer, BlockMatrixNode> xyNode = xNode.get(node.y);
                if (xyNode.containsKey(node.z)) {
                    return xyNode.get(node.z);
                } else {
                    xyNode.put(node.z, node);
                }
            } else {
                xNode.put(node.y, new HashMap<Integer, BlockMatrixNode>());
                HashMap<Integer, BlockMatrixNode> xyNode = xNode.get(node.y);
                xyNode.put(node.z, node);
            }
        } else {
            matrixNodes.put(node.x, new HashMap<Integer, HashMap<Integer, BlockMatrixNode>>());
            HashMap<Integer, HashMap<Integer, BlockMatrixNode>> xNode = matrixNodes.get(node.x);
            xNode.put(node.y, new HashMap<Integer, BlockMatrixNode>());
            HashMap<Integer, BlockMatrixNode> xyNode = xNode.get(node.y);
            xyNode.put(node.z, node);
        }
        return node;
    }
}