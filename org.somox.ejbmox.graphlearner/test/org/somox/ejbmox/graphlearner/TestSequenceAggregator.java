package org.somox.ejbmox.graphlearner;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.SPGraph;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.node.LeafNode;

public class TestSequenceAggregator {

	private GraphLearner learner;

	@BeforeClass
	public static void setup() {
		// log4j basic setup
		BasicConfigurator.configure();
	}

	@Before
	public void beforeTest() {
		learner = new GraphLearner();
	}

	@Test
	public void addingExistingPathMustPreserveGraphStructure_singleNode() {
		learner.integratePath(Path.fromNodes(new LeafNode("A")));
		learner.integratePath(Path.fromNodes(new LeafNode("A")));
		Assert.assertEquals("A", learner.getGraph().toString());
	}

	@Test
	public void addingExistingPathMustPreserveGraphStructure_series() {
		learner.integratePath(Path.fromNodes(new LeafNode("A"), new LeafNode("B"), new LeafNode("C")));
		learner.integratePath(Path.fromNodes(new LeafNode("A"), new LeafNode("B"), new LeafNode("C")));
		Assert.assertEquals("ABC", learner.getGraph().toString());
	}
	
	@Test
	public void addingExistingPathMustPreserveGraphStructure_parallel() {
		learner.integratePath(Path.fromNodes(new LeafNode("A"), new LeafNode("B")));
		learner.integratePath(Path.fromNodes(new LeafNode("A"), new LeafNode("C")));
		
		learner.integratePath(Path.fromNodes(new LeafNode("A"), new LeafNode("B")));
		learner.integratePath(Path.fromNodes(new LeafNode("A"), new LeafNode("C")));
		
		Assert.assertEquals("A[B|C]", learner.getGraph().toString());
	}
	
	
	
//	@Test
//	public void parallel_depthOne_widthTwo() {
//		learner.addPath(Path.fromNodes(new LeafNode("A"), new LeafNode("B"), new LeafNode("C")));
//		learner.addPath(Path.fromNodes(new LeafNode("A"), new LeafNode("B")));
//		Assert.assertEquals("A[B|C]", learner.getGraph().toString());
//	}

}
