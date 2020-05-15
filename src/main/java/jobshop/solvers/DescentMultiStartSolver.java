package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.Schedule;

import jobshop.solvers.DescentSolverThread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DescentMultiStartSolver implements Solver{
	
	int threadsNumber;
	
	public DescentMultiStartSolver(int threadsNumber) {
		this.threadsNumber = threadsNumber;
	}

	@Override
	public Result solve(Instance instance, long deadline) {
		
		try {
			Schedule[] results = new Schedule[this.threadsNumber];
			
			ExecutorService pool = Executors.newCachedThreadPool();
			for (int k = 0; k < this.threadsNumber; k++) {
				Future <Schedule> futureCall = pool.submit(new DescentSolverThread(instance,deadline));
				Schedule result = futureCall.get();
				results[k] = result;
			}
			pool.shutdown();
			
			Schedule bestSchedule = results[0];
			int bestMakespan = Integer.MAX_VALUE;
			
			for (int k = 0; k < this.threadsNumber; k++) {
				int currentMakespan = results[k].makespan();
				if (currentMakespan < bestMakespan) {
					bestMakespan = currentMakespan;
					bestSchedule = results[k];
				}
			}
			
			return new Result(instance, bestSchedule, Result.ExitCause.Timeout);
		}
		catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return null;
		}
	}
	
}
