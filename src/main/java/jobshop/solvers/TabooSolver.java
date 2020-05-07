package jobshop.solvers;

import java.util.List;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.encodings.ResourceOrder;
import jobshop.solvers.GloutonSolver;

import java.util.Arrays;

public class TabooSolver extends DescentSolver {
	
	@Override
	public Result solve (Instance instance, long deadline) {
		
		//determine the initial order using Glouton()
    	GloutonSolver gloutonSol = new GloutonSolver("SPT");
    	Result gloutonResult = gloutonSol.solve(instance, deadline);
    	Schedule initSchedule = gloutonResult.schedule;
    	ResourceOrder initOrder = new ResourceOrder(initSchedule);
    	
    	//create and initialise taboo matrix
    	int[][] tabouMatrix = new int[instance.numJobs*instance.numTasks][instance.numJobs*instance.numTasks];
		for (int k = 0; k < (instance.numJobs * instance.numTasks); k++) {
			Arrays.fill(tabouMatrix[k], 0);
		}
    	
    	//initialise
    	int minMakespan = initSchedule.makespan(); //best solution
    	ResourceOrder bestOrder = initOrder;
    	int currentMakespan = initSchedule.makespan(); //current solution
    	ResourceOrder currentOrder = initOrder; //current solution
    	int iter = 0; //number of iteration
    	int iterLimit = 3000; //number max of iteration
    	int tabooTime = 5; //time when the neighbour will be ignored
    	
    	//main loop
    	while ((iter < iterLimit) && (deadline - System.currentTimeMillis() > 1)) { //while we have a better solution
    		iter++;
    		//searching for the best neighbour
    		int bestNeighbourMakespan = Integer.MAX_VALUE;
    		int bestIndex1 = -1;
    		int bestIndex2 = -1;
    		List<Block> blocks = blocksOfCriticalPath(currentOrder);
    		for (Block block : blocks) {
    			List<Swap> swaps = neighbors(block);
    			for (Swap swap : swaps) {
    				int currentIndex1 = taskToIndex(swap.t1, swap.machine, instance);
    				int currentIndex2 = taskToIndex(swap.t2, swap.machine, instance);
    				int tabooTimeOk = tabouMatrix[currentIndex1][currentIndex2];
    				if(tabooTimeOk < iter) {
	    				ResourceOrder neighborOrder = currentOrder.copy();
	    				swap.applyOn(neighborOrder);
	    				Schedule neighborSchedule = neighborOrder.toSchedule();
	    				int neighbourMakespan = neighborSchedule.makespan();
	    				//we found a better neighbour
	    				if (neighbourMakespan < bestNeighbourMakespan) {
	    					currentMakespan = neighbourMakespan;
	    					currentOrder = neighborOrder;
	    					bestNeighbourMakespan = neighbourMakespan;
	    					bestIndex1 = currentIndex1;
	    					bestIndex2 = currentIndex2;
	    				}
    				}
    			}
    		}
    		//update taboo matrix
    		if ((bestIndex1 != -1) && (bestIndex2 != -1)) { //only if we found a neighbour
    			tabouMatrix[bestIndex2][bestIndex1] = iter + tabooTime;
    		}
    		//we found a new best solution
			if (currentMakespan < minMakespan) {
				minMakespan = currentMakespan;
				bestOrder = currentOrder;
			}
    	}
    	
    	return new Result(instance, bestOrder.toSchedule(), Result.ExitCause.Timeout);
	}
	
	/**
	 * find the index in tabooMatrix for a given task number and job
	 * @param task
	 * @param instance
	 * @return the index
	 */
	private int taskToIndex (int task, int machine, Instance instance) {
		int index = -1;
		
		index = machine * instance.numTasks;
		index += task;
		
		return index;
	}

}
