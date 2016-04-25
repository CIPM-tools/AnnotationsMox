package org.somox.ejbmox.graphlearner;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.util.PathBuilder;

public class TestSpecialCases {

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
	public void inverseOrderWithCommonPrefix() {
		learner.integratePath(PathBuilder.path("A", "B", "C"));
		learner.integratePath(PathBuilder.path("A", "C", "B"));
		Assert.assertEquals("A[B|]C[B|]", learner.getGraph().toString()); // or A[BC|CB]?
	}
	
	@Test
	public void inverseOrderWithoutCommonPrefix() {
		learner.integratePath(PathBuilder.path("B", "C"));
		learner.integratePath(PathBuilder.path("C", "B"));
		Assert.assertEquals("[B|]C[B|]", learner.getGraph().toString()); // or [BC|CB]?
	}
	
	@Test
	public void insertHeadOneNode() {
		learner.integratePath(PathBuilder.path("B", "C"));
		learner.integratePath(PathBuilder.path("A", "B", "C"));
		Assert.assertEquals("[A|]BC", learner.getGraph().toString());
	}
	
	@Test
	public void insertHeadTwoNodes() {
		learner.integratePath(PathBuilder.path("C", "D"));
		learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
		Assert.assertEquals("[AB|]CD", learner.getGraph().toString()); // TODO or [A|][B|]CD?
	}
	
	@Test
	public void insertTailOneNode() {
		learner.integratePath(PathBuilder.path("A", "B"));
		learner.integratePath(PathBuilder.path("A", "B", "C"));
		Assert.assertEquals("AB[C|]", learner.getGraph().toString());
	}
	
	@Test
	public void insertTailTwoNodes() {
		learner.integratePath(PathBuilder.path("A", "B"));
		learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
		Assert.assertEquals("AB[CD|]", learner.getGraph().toString());
	}
	
	@Test
	public void repeatedNodes() {
		learner.integratePath(PathBuilder.path("A", "B", "A", "B"));
		learner.integratePath(PathBuilder.path("A", "B", "A", "B"));
		Assert.assertEquals("ABAB", learner.getGraph().toString());
	}
	
}
