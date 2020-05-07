package jobshop.solvers;

import jobshop.Solver;
import jobshop.Result;
import jobshop.Schedule;

import java.util.Arrays;

import jobshop.Instance;
import jobshop.encodings.ResourceOrderOld;

public class GloutonSolver implements Solver{
	
	/*
	 * this the list of the priority available:
	 * SPT (default)
	 * LPT
	 * SRPT
	 * LRPT
	 */
	String priority = "SPT";
	
	public GloutonSolver(String priority) {
		//security to be sure that the priority is available, launch by default otherwise
		if(priority.equals("SPT") || priority.equals("LPT") || priority.equals("SRPT") || priority.equals("LRPT")) {
			this.priority = priority;
		}
	}
	
	@Override
	public Result solve(Instance instance, long deadline) {
		ResourceOrderOld sol = new ResourceOrderOld(instance);
		
		int[] orderTab = defineOrder(instance);
		sol.fromOrder(orderTab);
		
		Schedule schedule = sol.toSchedule();
		
		return new Result(instance, schedule, Result.ExitCause.Timeout);
	}
	
	/**
	 * Calculate the remaining time for a job from a given task
	 * @param instance
	 * @param job
	 * @param nextTask
	 * @return the remaining time
	 */
	private int remainingProcessingTime (Instance instance, int job, int nextTask) {
		
		int remainingTime = 0;
		
		for (int k = nextTask; k < instance.numTasks; k++) {
			remainingTime += instance.duration(job, k);
		}
		
		return remainingTime;
	}
	
	/**
	 * Find the job with the shortest remaining processing time
	 * @param instance
	 * @param nextTasks
	 * @return the job
	 */
	private int findSRPT(Instance instance, int[] nextTasks, int[] jobTimes, int[] machineTimes) {
		int minRemainingTime = Integer.MAX_VALUE;
		int minStartingTime = Integer.MAX_VALUE;
		int job = 0;
		
		for(int k = 0; k < instance.numJobs; k++) {
			if(nextTasks[k] < instance.numTasks) {
				int machine = instance.machine(k, nextTasks[k]);
				int startingTime = Math.max(jobTimes[k], machineTimes[machine]);
				int remainingProcessingTime = remainingProcessingTime(instance, k, nextTasks[k]);
				if(startingTime < minStartingTime) {
					job = k;
					minStartingTime = startingTime;
					minRemainingTime = remainingProcessingTime;
				}
				else if ((startingTime == minStartingTime) && (remainingProcessingTime < minRemainingTime)) {
					job = k;
					minStartingTime = startingTime;
					minRemainingTime = remainingProcessingTime;
				}
			}
		}
		
		return job;
	}
	
	/**
	 * Find the job with the longest remaining processing time
	 * @param instance
	 * @param nextTasks
	 * @return the job
	 */
	private int findLRPT(Instance instance, int[] nextTasks, int[] jobTimes, int[] machineTimes) {
		int maxRemainingTime = Integer.MIN_VALUE;
		int minStartingTime = Integer.MAX_VALUE;
		int job = 0;
		
		for(int k = 0; k < instance.numJobs; k++) {
			if(nextTasks[k] < instance.numTasks) {
				int machine = instance.machine(k, nextTasks[k]);
				int startingTime = Math.max(jobTimes[k], machineTimes[machine]);
				int remainingProcessingTime = remainingProcessingTime(instance, k, nextTasks[k]);
				if(startingTime < minStartingTime) {
					job = k;
					minStartingTime = startingTime;
					maxRemainingTime = remainingProcessingTime;
				}
				else if ((startingTime == minStartingTime) && (remainingProcessingTime > maxRemainingTime)) {
					job = k;
					minStartingTime = startingTime;
					maxRemainingTime = remainingProcessingTime;
				}
			}
		}
		
		return job;
	}
	
	/**
	 * Find the job with the shortest processing time
	 * @param instance
	 * @param nextTasks
	 * @return the job
	 */
	private int findSPT(Instance instance, int[] nextTasks, int[] jobTimes, int[] machineTimes) {
		int minTime = Integer.MAX_VALUE;
		int minStartingTime = Integer.MAX_VALUE;
		int job = 0;
		
		for(int k = 0; k < instance.numJobs; k++) {
			if(nextTasks[k] < instance.numTasks) {
				int machine = instance.machine(k, nextTasks[k]);
				int startingTime = Math.max(jobTimes[k], machineTimes[machine]);
				int processingTime = instance.duration(k, nextTasks[k]);
				if(startingTime < minStartingTime) {
					job = k;
					minStartingTime = startingTime;
					minTime = processingTime;
				}
				else if ((startingTime == minStartingTime) && (processingTime < minTime)) {
					job = k;
					minStartingTime = startingTime;
					minTime = processingTime;
				}
			}
		}
		
		return job;
	}
	
	/**
	 * Find the job with the longest processing time
	 * @param instance
	 * @param nextTasks
	 * @return the job
	 */
	private int findLPT(Instance instance, int[] nextTasks, int[] jobTimes, int[] machineTimes) {
		int maxTime = Integer.MIN_VALUE;
		int minStartingTime = Integer.MAX_VALUE;
		int job = 0;
		
		for(int k = 0; k < instance.numJobs; k++) {
			if(nextTasks[k] < instance.numTasks) {
				int machine = instance.machine(k, nextTasks[k]);
				int startingTime = Math.max(jobTimes[k], machineTimes[machine]);
				int processingTime = instance.duration(k, nextTasks[k]);
				if(startingTime < minStartingTime) {
					job = k;
					minStartingTime = startingTime;
					maxTime = processingTime;
				}
				else if ((startingTime == minStartingTime) && (processingTime > maxTime)) {
					job = k;
					minStartingTime = startingTime;
					maxTime = processingTime;
				}
			}
		}
		
		return job;
	}
	
	/**
	 * Determine an order based on the given priority
	 * @param instance
	 * @return the order
	 */
	private int[] defineOrder(Instance instance) {
		
		//the order to return
		int[] order = new int[instance.numJobs * instance.numTasks];
		
		//Use to remember the next task for each job
		int[] nextTasks = new int[instance.numJobs];
		
		//Use to remember the starting time of each job
		int[] jobTimes = new int[instance.numJobs];
		
		//Use to remember the starting time of each machine
		int[] machineTimes = new int[instance.numMachines];
		
		//initialisation
		Arrays.fill(nextTasks, 0);
		Arrays.fill(jobTimes, 0);
		Arrays.fill(machineTimes, 0);
		
		//main loop
		int tasksRemaining = instance.numJobs * instance.numTasks;
		int orderIndex = 0;
		while(tasksRemaining > 0) {
			int nextJob;
			if(this.priority.equals("LPT")) {
				nextJob = findLPT(instance, nextTasks, jobTimes, machineTimes);
			}
			else if (this.priority.equals("SRPT")) {
				nextJob = findSRPT(instance, nextTasks, jobTimes, machineTimes);
			}
			else if (this.priority.equals("LRPT")) {
				nextJob = findLRPT(instance, nextTasks, jobTimes, machineTimes);
			}
			else {
				nextJob = findSPT(instance, nextTasks, jobTimes, machineTimes);
			}
			order[orderIndex] = nextJob;
			int machine = instance.machine(nextJob, nextTasks[nextJob]);
			int time = Math.max(jobTimes[nextJob], machineTimes[machine]) + instance.duration(nextJob, nextTasks[nextJob]);
			jobTimes[nextJob] = time;
			machineTimes[machine] = time;
			nextTasks[nextJob]++;
			orderIndex++;
			tasksRemaining--;
		}
		
		return order;
	}
	
	/**
	 * Use it to test the order
	 * @param instance
	 * @param priority
	 * @return the order tested
	 */
	public int[] testOrder (Instance instance) {
		return this.defineOrder(instance);
	}

}
