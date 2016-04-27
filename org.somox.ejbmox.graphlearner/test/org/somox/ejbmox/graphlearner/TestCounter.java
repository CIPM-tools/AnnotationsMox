package org.somox.ejbmox.graphlearner;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.util.PathBuilder;
import org.somox.ejbmox.graphlearner.util.PathUtils;

public class TestCounter {

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
	public void testOneNode() {
		learner.integratePath(PathBuilder.path("A"));
		learner.integratePath(PathBuilder.path("A"));
		Assert.assertArrayEquals(new int[] { 2 }, PathUtils.pathCounts(PathBuilder.path("A").toString(), learner));
	}

	@Test
	public void testTwoNodesSerial() {
		learner.integratePath(PathBuilder.path("A", "B"));
		learner.integratePath(PathBuilder.path("A", "B"));
		Assert.assertArrayEquals(new int[] { 2, 2 },
				PathUtils.pathCounts(PathBuilder.path("A", "B").toString(), learner));
	}

	@Test
	public void testTwoNodesParallel() {
		learner.integratePath(PathBuilder.path("A"));
		learner.integratePath(PathBuilder.path("A", "B"));
		Assert.assertArrayEquals(new int[] { 2, 1 },
				PathUtils.pathCounts(PathBuilder.path("A", "B").toString(), learner));
		Assert.assertArrayEquals(new int[] { 2 }, PathUtils.pathCounts(PathBuilder.path("A").toString(), learner));
	}
	
	@Test
	public void testComplex() {
		learner.integratePath(PathBuilder.path("A", "B"));
		learner.integratePath(PathBuilder.path("A", "B", "C"));
		learner.integratePath(PathBuilder.path("A", "X", "C"));
		learner.integratePath(PathBuilder.path("A", "X", "C", "A"));
		
		Assert.assertArrayEquals(new int[] { 4, 2 },
				PathUtils.pathCounts(PathBuilder.path("A", "B").toString(), learner));
		Assert.assertArrayEquals(new int[] { 4, 2, 3},
				PathUtils.pathCounts(PathBuilder.path("A", "X", "C").toString(), learner));
		Assert.assertArrayEquals(new int[] { 4, 2, 3, 1},
				PathUtils.pathCounts(PathBuilder.path("A", "X", "C", "A").toString(), learner));
	}

}
