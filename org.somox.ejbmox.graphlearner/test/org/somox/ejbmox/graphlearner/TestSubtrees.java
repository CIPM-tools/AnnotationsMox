package org.somox.ejbmox.graphlearner;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.util.PathBuilder;
import org.somox.ejbmox.graphlearner.visitor.AllLeavesVisitor;

public class TestSubtrees {

    private GraphLearner learner1;

    private Node aNode;
    private Node bNode;
    private Node cNode;
    private Node dNode;

    @BeforeClass
    public static void setup() {
        // log4j basic setup
        BasicConfigurator.configure();
    }

    @Before
    public void beforeTest() {
        learner1 = new GraphLearner();

        learner1.integratePath(PathBuilder.path("A", "B", "C"));
        learner1.integratePath(PathBuilder.path("A", "D"));

        // find leaf nodes
        AllLeavesVisitor visitor = new AllLeavesVisitor();
        learner1.getGraph().traverse(visitor);
        List<Node> leaves = visitor.getResult();

        for (Node lastNode : leaves) {
            if (lastNode.toString().equals("A")) {
                aNode = lastNode;
            }
            if (lastNode.toString().equals("B")) {
                bNode = lastNode;
            }
            if (lastNode.toString().equals("C")) {
                cNode = lastNode;
            }
            if (lastNode.toString().equals("D")) {
                dNode = lastNode;
            }
        }
    }

    @Test
    public void testA() {
        List<Node> found = Node.findSubtrees(aNode);
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(aNode, found.get(0));
    }

    @Test
    public void testB() {
        List<Node> found = Node.findSubtrees(bNode);
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(bNode, found.get(0));
    }

    @Test
    public void testC() {
        List<Node> found = Node.findSubtrees(cNode);
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(cNode, found.get(0));
    }
    
    @Test
    public void testD() {
        List<Node> found = Node.findSubtrees(dNode);
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(dNode, found.get(0));
    }

    @Test
    public void testAB() {
        List<Node> found = Node.findSubtrees(aNode, bNode);
        Assert.assertEquals(2, found.size());
        Assert.assertEquals(aNode, found.get(0));
        Assert.assertEquals(bNode.getParent().getParent(), found.get(1));
    }

    @Test
    public void testBC() {
        List<Node> found = Node.findSubtrees(bNode, cNode);
        Assert.assertEquals(2, found.size());
        Assert.assertEquals(bNode, found.get(0));
        Assert.assertEquals(cNode, found.get(1));
    }

    @Test
    public void testAC() {
        List<Node> found = Node.findSubtrees(aNode, cNode);
        Assert.assertEquals(2, found.size());
        Assert.assertEquals(aNode, found.get(0));
        Assert.assertEquals(cNode.getParent().getParent(), found.get(1));
    }
    
    @Test
    public void testAD() {
        List<Node> found = Node.findSubtrees(aNode, dNode);
        Assert.assertEquals(2, found.size());
        Assert.assertEquals(aNode, found.get(0));
        Assert.assertEquals(dNode.getParent(), found.get(1));
    }

    @Test
    public void testBD() {
        List<Node> found = Node.findSubtrees(bNode, dNode);
        Assert.assertEquals(2, found.size());
        Assert.assertEquals(bNode.getParent(), found.get(0));
        Assert.assertEquals(dNode, found.get(1));
    }

    @Test
    public void testABC() {
        List<Node> found = Node.findSubtrees(aNode, bNode, cNode);
        Assert.assertEquals(2, found.size());
        Assert.assertEquals(aNode, found.get(0));
        Assert.assertEquals(bNode.getParent().getParent(), found.get(1));
    }

}
