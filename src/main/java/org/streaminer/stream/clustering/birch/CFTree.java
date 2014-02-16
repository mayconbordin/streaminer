/*
 *  This file is part of JBIRCH.
 *
 *  JBIRCH is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JBIRCH is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with JBIRCH.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 *  CFTree.java
 *  Copyright (C) 2009 Roberto Perdisci (roberto.perdisci@gmail.com)
 */


package org.streaminer.stream.clustering.birch;


import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streaminer.util.SizeOf;

/**
 * This is an implementation of the BIRCH clustering algorithm described in:
 * 
 * T. Zhang, R. Ramakrishnan, and M. Livny.
 * "BIRCH: A New Data Clustering Algorithm and Its Applications"
 * Data Mining and Knowledge Discovery, 1997.
 * 
 * @author Roberto Perdisci (roberto.perdisci@gmail.com)
 * @version 0.1
 *
 */
public class CFTree {
    private static final Logger LOG = LoggerFactory.getLogger(CFTree.class);
	
    /**
     * Used when computing if the tree is reaching memory limit
     */
    private static final double MEM_LIM_FRAC = 10;

    /**
     * Centroid Distance D0
     */
    public static final int D0_DIST = 0;

    /**
     * Centroid distance D1
     */
    public static final int D1_DIST = 1;

    /**
     * Cluster Distance D2
     */
    public static final int D2_DIST = 2;

    /**
     * Cluster Distance D3
     */
    public static final int D3_DIST = 3;

    /**
     * Cluster Distance D4
     */
    public static final int D4_DIST = 4;

    /**
     * The root node of the CFTree
     */
    private CFNode root;

    /**
     * dummy node that points to the list of leaves. used for fast retrieval of final subclusters
     */
    private CFNode leafListStart = null;

    /**
     * keeps count of the instances inserted into the tree
     */
    private int instanceIndex = 0;

    /**
     * if true, the tree is automatically rebuilt every time the memory limit is reached
     */
    private boolean automaticRebuild = false;

    /**
     * the memory limit used when automatic rebuilding is active
     */
    private long memLimit = (long)Math.pow(1024, 3); // default = 1GB

    /**
     * used when automatic rebuilding is active
     */
    private long periodicMemLimitCheck = 100000; // checks if memeory limit is exceeded every 100,000 insertions

    /**
     * 
     * @param maxNodeEntries parameter B
     * @param distThreshold parameter T
     * @param distFunction must be one of CFTree.D0_DIST,...,CFTree.D4_DIST, otherwise it will default to D0_DIST 
     * @param applyMergingRefinement if true, activates merging refinement after each node split
     */
    public CFTree(int maxNodeEntries, double distThreshold, int distFunction, boolean applyMergingRefinement) {
        if(distFunction < D0_DIST || distFunction > D4_DIST)
            distFunction = D0_DIST;

        root = new CFNode(maxNodeEntries,distThreshold,distFunction,applyMergingRefinement,true);
        leafListStart = new CFNode(0,0,distFunction,applyMergingRefinement,true); // this is a dummy node that points to the fist leaf
        leafListStart.setNextLeaf(root); // at this point root is the only node and therefore also the only leaf
    }

    /**
     * 
     * @return the current memory limit used to trigger automatic rebuilding
     */
    public long getMemoryLimit() {
        return memLimit;
    }

    /**
     * Gets the start of the list of leaf nodes (remember: the first node is a dummy node)
     * 
     * @return
     */
    public CFNode getLeafListStart() {
        return this.leafListStart;
    }

    /**
     * 
     * @param limit memory limit in bytes
     */
    public void setMemoryLimit(long limit) {
        this.memLimit = limit;
    }

    /**
     * 
     * @param limit memory limit in Mbytes
     */
    public void setMemoryLimitMB(long limit) {
        this.memLimit = limit*1024*1024;
    }

    /**
     * 
     * @param auto if true, and memory limit is reached, the tree is automatically rebuilt with larger threshold
     */
    public void setAutomaticRebuild(boolean auto) {
        this.automaticRebuild = auto;
    }

    /**
     * 
     * @param period the number of insert operations after which we check whether the tree has reached the memory limit
     */
    public void setPeriodicMemLimitCheck(long period) {
        this.periodicMemLimitCheck = period;
    }

    /**
     * Inserts a single pattern vector into the CFTree
     * 
     * @param x the pattern vector to be inserted in the tree
     * @return true if insertion was successful
     */
    public boolean insertEntry(double[] x) {
        instanceIndex++;

        if (automaticRebuild && (instanceIndex % periodicMemLimitCheck)==0) {
            // rebuilds the tree if we reached or exceeded memory limits
            rebuildIfAboveMemLimit();
        }

        return insertEntry(x,instanceIndex);
    }

    /**
     * Insert a pattern vector with a specific associated pattern vector index.
     * This method does not use periodic memory limit checks.
     * 
     * @param x the pattern vector to be inserted in the tree
     * @param index a specific index associated to the pattern vector x
     * @return true if insertion was successful
     */
    public boolean insertEntry(double[] x, int index) {
        CFEntry e = new CFEntry(x, index);
        return insertEntry(e);
    }

    /**
     * Inserts an entire CFEntry into the tree. Used for tree rebuilding.
     * 
     * @param e the CFEntry to insert
     * @return true if insertion happened without problems
     */
    private boolean insertEntry(CFEntry e) {
        boolean dontSplit = root.insertEntry(e);
        if (!dontSplit) {
            // if dontSplit is false, it means there was not enough space to insert the new entry in the tree, 
            // therefore wee need to split the root to make more room
            splitRoot();

            if (automaticRebuild) {
                // rebuilds the tree if we reached or exceeded memory limits
                rebuildIfAboveMemLimit();
            }
        }

        return true; // after root is split, we are sure x was inserted correctly in the tree, and we return true
    }

    /**
     * Every time we split the root, we check whether the memory limit imposed on the tree
     * has been reached. In this case, we automatically increase the distance threshold and
     * rebuild the tree. 
     * 
     * It is worth noting that since we only check memory consumption only during root split,
     * and not for all node splits (for performance reasons), we cannot guarantee that
     * the memory limit will not be exceeded. The tree may grow significantly between a 
     * root split and the next.
     * Furthermore, the computation of memory consumption using the SizeOf class is only approximate.
     * 
     * Notice also that if the threshold grows to the point that all the entries fall into one entry
     * of the root (i.e., the root is the only node in the tree, and has only one sub-cluster)
     * the automatic rebuild cannot decrease the memory consumption (because increasing the threshold
     * has not effect on reducing the size of the tree), and if Java runs out of memory
     * the program will terminate.
     * 
     * @return true if rebuilt
     */
    private boolean rebuildIfAboveMemLimit() {
        if (hasReachedMemoryLimit(this, memLimit)) {
            LOG.info("Size of Tree is reaching or has exceeded the memory limit");
            LOG.info("Rebuilding the Tree...");
            LOG.info("Current Threshold = " + root.getDistThreshold());
            
            double newThreshold = computeNewThreshold(leafListStart, root.getDistFunction(), root.getDistThreshold());
            LOG.info("New Threshold = " + newThreshold);

            CFTree newTree = this.rebuildTree(root.getMaxNodeEntries(), newThreshold, root.getDistFunction(), root.applyMergingRefinement(), false);
            copyTree(newTree);

            return true;
        }

        return false;
    }

    /**
     * Splits the root to accommodate a new entry. The height of the tree grows by one.
     */
    private void splitRoot() {
        // the split happens by finding the two entries in this node that are the most far apart
        // we then use these two entries as a "pivot" to redistribute the old entries into two new nodes

        CFEntryPair p = root.findFarthestEntryPair(root.getEntries());

        CFEntry newEntry1 = new CFEntry();
        CFNode newNode1 = new CFNode(root.getMaxNodeEntries(),root.getDistThreshold(),root.getDistFunction(),root.applyMergingRefinement(),root.isLeaf());
        newEntry1.setChild(newNode1);

        CFEntry newEntry2 = new CFEntry();
        CFNode newNode2 = new CFNode(root.getMaxNodeEntries(),root.getDistThreshold(),root.getDistFunction(),root.applyMergingRefinement(),root.isLeaf());
        newEntry2.setChild(newNode2);

        // the new root that hosts the new entries
        CFNode newRoot = new CFNode(root.getMaxNodeEntries(),root.getDistThreshold(),root.getDistFunction(),root.applyMergingRefinement(),false);
        newRoot.addToEntryList(newEntry1);
        newRoot.addToEntryList(newEntry2);

        // this updates the pointers to the list of leaves
        if(root.isLeaf()) { // if root was a leaf
            leafListStart.setNextLeaf(newNode1);
            newNode1.setPreviousLeaf(leafListStart);
            newNode1.setNextLeaf(newNode2);
            newNode2.setPreviousLeaf(newNode1);
        }

        // redistributes the entries in the root between newEntry1 and newEntry2
        // according to the distance to p.e1 and p.e2
        root.redistributeEntries(root.getEntries(),p,newEntry1,newEntry2);

        // updates the root
        root = newRoot;

        // frees some memory by deleting the nodes in the tree that had to be split
        System.gc();
    }

    /**
     * Overwrites the structure of this tree (all nodes, entreis, and leaf list) with the structure of newTree.
     * 
     * @param newTree the tree to be copied
     */
    private void copyTree(CFTree newTree) {
        this.root = newTree.root;
        this.leafListStart = newTree.leafListStart;
    }

    /**
     * Computes a new threshold based on the average distance of the closest subclusters in each leaf node
     * 
     * @param leafListStart the pointer to the start of the list (the first node is assumed to be a place-holder dummy node)
     * @param distFunction 
     * @param currentThreshold
     * @return the new threshold
     */
    public double computeNewThreshold(CFNode leafListStart,int distFunction, double currentThreshold) {
        double avgDist = 0;
        int n = 0;

        CFNode l = leafListStart.getNextLeaf();
        while (l!=null) {
            if (!l.isDummy()) {
                CFEntryPair p = l.findClosestEntryPair(l.getEntries());
                if(p!=null) {
                    avgDist += p.e1.distance(p.e2, distFunction);
                    n++;

                    /* This is a possible alternative: Overall avg distance between leaf entries
                    CFEntry[] v = l.getEntries().toArray(new CFEntry[0]);
                    for(int i=0; i < v.length-1; i++) {
                            for(int j=i+1; j < v.length; j++) {
                                    avgDist += v[i].distance(v[j], distFunction);
                                    n++;
                            }
                    }*/
                }
            }
            l = l.getNextLeaf();
        }

        double newThreshold = 0;
        if (n>0)
            newThreshold = avgDist/n;

        if (newThreshold <= currentThreshold) { // this guarantees that newThreshold always increases compared to currentThreshold
            newThreshold = 2*currentThreshold;
        }

        return newThreshold;
    }

    /**
     * True if CFTree's memory occupation exceeds or is almost equal to the memory limit
     * 
     * @param tree the tree to be tested
     * @param limit the memory limit
     * @return true if memory limit has been reached
     */
    private boolean hasReachedMemoryLimit(CFTree tree, long limit) {
        long memory = computeMemorySize(tree);

        LOG.info("Tree Size = " + SizeOf.humanReadable(memory));
        return (memory >= (limit - limit/(double)MEM_LIM_FRAC));
    }

    /**
     * Computes the memory usage of a CFTree
     * 
     * @param t a CFTree
     * @return memory usage in bytes
     */
    private long computeMemorySize(CFTree t) {
        long memSize = 0;
        try {
            memSize = SizeOf.deepSizeOf(t);
        } catch(Exception e) {
            LOG.error("Error when computing memory size", e);
        }
        
        return memSize;
    }

    /**
     * This implementation of the rebuilding algorithm is different from
     * the one described in Section 4.5 of the paper. However the effect
     * is practically the same. Namely, given a tree t_i build using
     * threshold T_i, if we set a new threshold T_(i+1) and call
     * rebuildTree (assuming maxEntries stays the same) we will obtain
     * a more compact tree. 
     * 
     * Since the CFTree is sensitive to the order of the data, there
     * may be cases in which, if we set the T_(i+1) so that non of the
     * sub-clusters (i.e., the leaf entries) can be merged (e.g., T_(i+1)=-1)
     * we might actually obtain a new tree t_(i+1) containing more nodes
     * than t_i. However, the obtained sub-clusters in t_(i+1) will be 
     * identical to the sub-clusters in t_i.
     * 
     * In practice, though, if T_(i+1) > T_(i), the tree t_(i+1) will
     * usually be smaller than t_i.
     * Although the Reducibility Theorem in Section 4.5 may not hold
     * anymore, in practice this will not be a big problem, since 
     * even in those cases in which t_(i+1)>t_i, the growth should
     * be very small.
     * 
     * The advantage is that relaxing the constraint that the size
     * of t_(i+1) must be less than t_i makes the implementation
     * of the rebuilding algorithm much easier.
     * 
     * @param newMaxEntries the new number of entries per node
     * @param newThreshold the new threshold
     * @param applyMergingRefinement if true, merging refinement will be applied after every split
     * @param discardOldTree if true, the old tree will be discarded (to free memory)
     * 
     * @return the new (usually more compact) CFTree
     */
    public CFTree rebuildTree(int newMaxEntries, double newThreshold, int distFunction, boolean applyMergingRefinement, boolean discardOldTree) {
        CFTree newTree = new CFTree(newMaxEntries, newThreshold, distFunction, applyMergingRefinement);
        newTree.instanceIndex = this.instanceIndex;
        newTree.memLimit = this.memLimit;

        CFNode oldLeavesList = this.leafListStart.getNextLeaf(); // remember: the node this.leafListStart is a dummy node (place holder for beginning of leaf list)

        if (discardOldTree) {
            this.root = null;
            System.gc(); // removes the old tree. Only the old leaves will be kept
        }

        CFNode leaf = oldLeavesList;
        while (leaf!=null) {
            if (!leaf.isDummy()) {
                for (CFEntry e : leaf.getEntries()) {
                    CFEntry newE = e;
                    if (!discardOldTree) // we need to make a deep copy of e
                        newE = new CFEntry(e);

                    newTree.insertEntry(newE);
                }
            }

            leaf = leaf.getNextLeaf();
        }

        if (discardOldTree) {
            this.leafListStart = null; 
            System.gc(); // removes the old list of leaves
        }

        return newTree;
    }


    /**
     * 
     * @return a list of subcluster, and for each subcluster a list of pattern vector indexes that belong to it
     */
    public ArrayList<ArrayList<Integer>> getSubclusterMembers() {
        ArrayList<ArrayList<Integer>> membersList = new ArrayList<ArrayList<Integer>>();

        CFNode l = leafListStart.getNextLeaf(); // the first leaf is dummy!
        while (l!=null) {
            if (!l.isDummy()) {
                for(CFEntry e : l.getEntries())
                    membersList.add(e.getIndexList());
            }
            l = l.getNextLeaf();
        }

        return membersList;
    }

    /**
     * Signals the fact that we finished inserting data.
     * The obtained subclusters will be assigned a positive, unique ID number
     */
    public void finishedInsertingData() {
        CFNode l = leafListStart.getNextLeaf(); // the first leaf is dummy!

        int id = 0;
        while (l!=null) {
            if (!l.isDummy()) {
                for(CFEntry e : l.getEntries()) {
                    id++;
                    e.setSubclusterID(id);
                }
            }
            l = l.getNextLeaf();
        }
    }

    /**
     * Retrieves the subcluster id of the closest leaf entry to e
     * 
     * @param e the entry to be mapped
     * @return a positive integer, if the leaf entries were enumerated using finishedInsertingData(), otherwise -1
     */
    public int mapToClosestSubcluster(double[] x) {
        CFEntry e = new CFEntry(x);

        return root.mapToClosestSubcluster(e);
    }

    /**
     * Computes an estimate of the cost of running an O(n^2) algorithm to split each subcluster in more fine-grained clusters
     * 
     * @return sqrt(sum_i[(n_i)^2]), where n_i is the number of members of the i-th subcluster
     */
    public double computeSumLambdaSquared() {
        double lambdaSS = 0;

        CFNode l = leafListStart.getNextLeaf();
        while (l!=null) {
            if (!l.isDummy()) {
                for(CFEntry e : l.getEntries()) {
                    lambdaSS += Math.pow(e.getIndexList().size(), 2);
                }
            }
            l = l.getNextLeaf();
        }

        return Math.sqrt(lambdaSS);
    }

    /**
     * prints the CFTree
     */
    public void printCFTree() {
        System.out.println(root);
    }

    /**
     * Counts the nodes of the tree (including leaves)
     * 
     * @return the number of nodes in the tree
     */
    public int countNodes() {
        int n = 1; // at least root has to be present
        n += root.countChildrenNodes();

        return n;
    }

    /**
     * Counts the number of CFEntries in the tree
     * 
     * @return the number of entries in the tree
     */
    public int countEntries() {
        int n = root.size(); // at least root has to be present
        n += root.countEntriesInChildrenNodes();

        return n;
    }

    /**
     * Counts the number of leaf entries (i.e., the number of sub-clusters in the tree)
     * 
     * @return the number of leaf entries (i.e., the number of sub-clusters)
     */
    public int countLeafEntries() {
        int i=0;
        CFNode l = leafListStart.getNextLeaf();
        while (l!=null) {
            if (!l.isDummy()) {
                i += l.size();
            }

            l = l.getNextLeaf();
        }

        return i;
    }

    /**
     * Prints the index of all the pattern vectors that fall into the leaf nodes.
     * This is only useful for debugging purposes.
     */
    public void printLeafIndexes() {
        ArrayList<Integer> indexes = new ArrayList<Integer>();

        CFNode l = leafListStart.getNextLeaf();
        while (l!=null) {
            if (!l.isDummy()) {
                System.out.println(l);
                for(CFEntry e : l.getEntries())
                    indexes.addAll(e.getIndexList());
            }
            l = l.getNextLeaf();
        }

        Integer[] v = indexes.toArray(new Integer[0]);
        Arrays.sort(v);

        System.out.println("Num of Indexes = " + v.length);
        System.out.println(Arrays.toString(v));
    }

    /**
     * Prints the index of the pattern vectors in each leaf entry (i.e., each subcluster)
     */
    public void printLeafEntries() {
        int i=0;
        CFNode l = leafListStart.getNextLeaf();
        while (l!=null) {
            if (!l.isDummy()) {
                for (CFEntry e : l.getEntries()) {
                    System.out.println("[[" + (++i) +"]]");
                    Integer[] v = e.getIndexList().toArray(new Integer[0]);
                    Arrays.sort(v);
                    System.out.println(Arrays.toString(v));
                }
            }

            l = l.getNextLeaf();
        }
    }
}