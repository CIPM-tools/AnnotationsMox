package org.somox.ejbmox.graphlearner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.util.PathBuilder;

public class TestAllPaths {

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
	public void testA() {
		learner.integratePath(PathBuilder.path("A", "B", "C"));
		learner.integratePath(PathBuilder.path("A", "C"));
		
		List<Path> expected = new ArrayList<>();
		expected.add(PathBuilder.path("A", "B", "C"));
		expected.add(PathBuilder.path("A", "C"));
		
		List<Path> actual = learner.getGraph().allPaths();
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testB() {
		learner.integratePath(PathBuilder.path("A", "B"));
		learner.integratePath(PathBuilder.path("A"));
		
		List<Path> expected = new ArrayList<>();
		expected.add(PathBuilder.path("A", "B"));
		expected.add(PathBuilder.path("A"));
		
		List<Path> actual = learner.getGraph().allPaths();
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testC() {
		learner.integratePath(PathBuilder.path("A", "B", "C"));
		learner.integratePath(PathBuilder.path("A", "C"));
		learner.integratePath(PathBuilder.path("C"));
		
		Set<Path> expected = new HashSet<>();
		expected.add(PathBuilder.path("A", "B", "C"));
		expected.add(PathBuilder.path("A", "C"));
		expected.add(PathBuilder.path("B", "C"));
		expected.add(PathBuilder.path("C"));
		
		Set<Path> actual = new HashSet<>(learner.getGraph().allPaths());
		
		Assert.assertEquals(expected, actual);
	}
	
}
