package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.Schedule;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.GloutonSolver;

import java.util.List;
import java.util.ArrayList;

public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
        
        @Override
        public String toString() {
        	return "machine = " + this.machine + " firstTask = " + this.firstTask + " lastTask = " + this.lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
            Task task1 = order.tasksByMachine[this.machine][this.t1];
            Task task2 = order.tasksByMachine[this.machine][this.t2];
            //swapping
            order.tasksByMachine[this.machine][this.t1] = task2;
            order.tasksByMachine[this.machine][this.t2] = task1;
        }
        
        @Override
        public String toString() {
        	return "machine = " + this.machine + " t1 = " + this.t1 + " t2 = " + this.t2;
        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {
        
    	//determine the initial order using Glouton()
    	GloutonSolver gloutonSol = new GloutonSolver("SPT");
    	Result gloutonResult = gloutonSol.solve(instance, deadline);
    	Schedule initSchedule = gloutonResult.schedule;
    	ResourceOrder initOrder = new ResourceOrder(initSchedule);
    	
    	int minMakespan = Integer.MAX_VALUE;
    	int makespan = initSchedule.makespan();
    	ResourceOrder order = initOrder;
    	Schedule schedule = initSchedule;
    	while (makespan < minMakespan) { //while we have a better solution
    		minMakespan = makespan;
    		int neighborMakespan = Integer.MAX_VALUE;
    		ResourceOrder bestOrder = order;
    		Schedule bestSchedule = schedule;
    		List<Block> blocks = blocksOfCriticalPath(order);
    		for (Block block : blocks) {
    			List<Swap> swaps = neighbors(block);
    			for (Swap swap : swaps) {
    				ResourceOrder neighborOrder = order.copy();
    				swap.applyOn(neighborOrder);
    				Schedule neighborSchedule = neighborOrder.toSchedule();
    				neighborMakespan = neighborSchedule.makespan();
    				if (neighborMakespan < makespan) {
    					makespan = neighborMakespan;
    					bestOrder = neighborOrder;
    					bestSchedule = neighborSchedule;
    				}
    			}
    		}
    		order = bestOrder;
    		schedule = bestSchedule;
    	}
    	
    	return new Result(instance, schedule, Result.ExitCause.Timeout);
    }

    /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {
        
    	List<Block> blockList = new ArrayList<Block>();
        
        //retrieve the critical path
        Schedule schedule = order.toSchedule();
        List<Task> path = schedule.criticalPath();
        
        //main loop on the critical path
        int previousMachine = -1;
        int blockRange = -1;
        int firstTaskIndex = -1;
        for (int k = 0; k < path.size(); k++) {
        	Task currentTask = path.get(k);
        	int currentMachine = order.machine(currentTask);
        	
        	if (currentMachine == previousMachine) { //we're still on the same machine
        		blockRange++;
        		//we finished on a block
        		if(k == (path.size()-1)) {
        			blockList.add(new Block(previousMachine,firstTaskIndex,(firstTaskIndex+blockRange-1)));
        		}
        	}
        	else { //we've changed of machine
        		if (blockRange > 1) { //we've identify a valid block
        			//create the block
        			blockList.add(new Block(previousMachine,firstTaskIndex,(firstTaskIndex+blockRange-1)));
        			firstTaskIndex = order.taskIndex(currentTask);
        			blockRange = 1;
        			previousMachine = currentMachine;
        		}
        		else {
        			firstTaskIndex = order.taskIndex(currentTask);
        			blockRange = 1;
        			previousMachine = currentMachine;
        		}
        	}
        }
        
        return blockList;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
    	
        List<Swap> swapList = new ArrayList<Swap>();
    	
    	//case 1 : block of size 2
    	if ((block.lastTask-block.firstTask) == 1) {
    		Swap swap = new Swap(block.machine,block.firstTask,block.lastTask);
    		swapList.add(swap);
    	}
    	//case 2 : block of size >= 3
    	else {
    		Swap swap1 = new Swap(block.machine,block.firstTask,block.firstTask+1);
    		Swap swap2 = new Swap(block.machine,block.lastTask-1,block.lastTask);
    		swapList.add(swap1);
    		swapList.add(swap2);
    	}
    	return swapList;
    }

}

