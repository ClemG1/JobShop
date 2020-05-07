package jobshop.solvers;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;

import jobshop.Instance;

public class GloutonTests {
	
	@Test
	public void TestGlouton() throws IOException {
		
		System.out.println("-----Test Glouton Solver-----");
		
		Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
		
		System.out.println("-----Test SPT Order-----");
		
		GloutonSolver solver1 = new GloutonSolver("SPT");
		int[] order1 = solver1.testOrder(instance);
		for (int job : order1) {
			System.out.print(job + " ");
		}
		System.out.println();
		
		System.out.println("-----Test LPT Order-----");
		
		GloutonSolver solver2 = new GloutonSolver("LPT");
		int[] order2 = solver2.testOrder(instance);
		for (int job : order2) {
			System.out.print(job + " ");
		}
		System.out.println();
		
		System.out.println("-----Test SRPT Order-----");
		
		GloutonSolver solver3 = new GloutonSolver("SRPT");
		int[] order3 = solver3.testOrder(instance);
		for (int job : order3) {
			System.out.print(job + " ");
		}
		System.out.println();
		
		System.out.println("-----Test LRPT Order-----");
		
		GloutonSolver solver4 = new GloutonSolver("LRPT");
		int[] order4 = solver4.testOrder(instance);
		for (int job : order4) {
			System.out.print(job + " ");
		}
		System.out.println();
		
	}

}
