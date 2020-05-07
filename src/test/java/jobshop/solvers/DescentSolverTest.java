package jobshop.solvers;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;

import java.util.List;

import jobshop.Instance;
import jobshop.Schedule;
import jobshop.solvers.DescentSolver.Block;
import jobshop.solvers.DescentSolver.Swap;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.ResourceOrderOld;
import jobshop.encodings.Task;

public class DescentSolverTest {
	
	@Test
	public void TestBlocksOfCriticalPath() throws IOException {
		
		System.out.println("-----Test BlocksOfCriticalPath()-----");
		
		Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));
		
		//blocks index : 2 -> 4
		int[] orderTab = {0,2,0,1,0,2,2,1,1};//{1,2,2,1,2,0,0,0,1};
		ResourceOrderOld resourceOrderOld = new ResourceOrderOld(instance);
		resourceOrderOld.fromOrder(orderTab);
		Schedule schedule = resourceOrderOld.toSchedule();
		
		ResourceOrder resourceOrder = new ResourceOrder(schedule);
		
		System.out.println("Resource Order : \n");
		System.out.println(resourceOrder);
		
		DescentSolver solver = new DescentSolver();
		List<Task> taskList = schedule.criticalPath();
		List<Block> blockList = solver.blocksOfCriticalPath(resourceOrder);
		
		System.out.println("Critical Path : ");
		
		for (Task task : taskList) {
			System.out.println(task);
		}
		
		System.out.println("Blocks : ");
		
		for (Block block : blockList) {
			System.out.println(block);
		}
	}

	@Test
	public void TestNeighbors() throws IOException {
		
		System.out.println("-----Test neighbors()-----");
		
		Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));
		
		int[] orderTab = {0,2,0,1,0,2,2,1,1};
		ResourceOrderOld resourceOrderOld = new ResourceOrderOld(instance);
		resourceOrderOld.fromOrder(orderTab);
		Schedule schedule = resourceOrderOld.toSchedule();
		
		ResourceOrder resourceOrder = new ResourceOrder(schedule);
		
		DescentSolver solver = new DescentSolver();
		List<Block> blockList = solver.blocksOfCriticalPath(resourceOrder);
		
		for (Block block : blockList) {
			List<Swap> swapList = solver.neighbors(block);
			System.out.println("Swap : ");
			for(Swap swap : swapList) {
				System.out.println(swap);
			}
		}
		
	}
	
	@Test
	public void TestApplyOn() throws IOException {
		
		System.out.println("-----Test applyOn()-----");
		
		Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));
		
		int[] orderTab = {0,2,0,1,0,2,2,1,1};
		ResourceOrderOld resourceOrderOld = new ResourceOrderOld(instance);
		resourceOrderOld.fromOrder(orderTab);
		Schedule schedule = resourceOrderOld.toSchedule();
		
		ResourceOrder resourceOrder = new ResourceOrder(schedule);
		
		DescentSolver solver = new DescentSolver();
		List<Block> blockList = solver.blocksOfCriticalPath(resourceOrder);
		
		for (Block block : blockList) {
			List<Swap> swapList = solver.neighbors(block);
			for(Swap swap : swapList) {
				ResourceOrder newOrder = resourceOrder.copy();
				swap.applyOn(newOrder);
				System.out.println("Previous order : \n" + resourceOrder);
				System.out.println("New order : \n" + newOrder);
			}
		}
		
	}
	
}
