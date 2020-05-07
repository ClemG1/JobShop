package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;
import java.util.Arrays;

public class ResourceOrderOld extends Encoding{
		
	ResourceTuple matrixOfTask[][];
	int currentTask[]; //use to remember the current task for each job
	int currentResource[]; //use to know how many jobs has used this resource (column of the matrix)
	
	public ResourceOrderOld(Instance instance) {
		super(instance);
	
		this.matrixOfTask = new ResourceTuple [instance.numMachines][instance.numJobs];
		this.currentTask = new int [instance.numJobs];
		this.currentResource = new int [instance.numMachines];
		Arrays.fill(this.currentTask, 0);
		Arrays.fill(this.currentResource, 0);
	}
	
	/**
	 * initialise the matrix based on the order given 
	 * @param orderTab
	 */
	public void fromOrder(int orderTab[]) {
		int currentJob;
		int numberTask;
		int numberResource;
		int machine;
		for (int k = 0; k < (instance.numJobs * instance.numTasks); k++) {
			currentJob = orderTab[k];
			numberTask = this.currentTask[currentJob];
			machine = instance.machine(currentJob, numberTask);
			numberResource = this.currentResource[machine];
			this.matrixOfTask[machine][numberResource] = new ResourceTuple(currentJob, numberTask);
			this.currentTask[currentJob]++;
			this.currentResource[machine]++;
		}
	}
	
	/**
	 * find the next task scheduled based on their started time
	 * @param schedule
	 * @return a tab with job/task
	 */
	private int[] findNextTask (Schedule schedule, int[][] tasksTreated) {
		int[] results = new int[2]; //0: job | 1: task
		int min = Integer.MAX_VALUE;
		int job = -1;
		int task = -1;
		
		for (int i = 0; i < instance.numJobs; i++) {
			for (int j = 0; j < instance.numMachines; j++) {
				if((tasksTreated[i][j] == 0) && (schedule.startTime(i, j) < min)) {
					min = schedule.startTime(i, j);
					job = i;
					task = j;
				}
			}
		}
		
		results[0] = job;
		results[1] = task;
		return results;
	}
	
	/**
	 * Initialise the matrix of a ResourceOrder from a schedule given
	 * @param schedule
	 */
	public void fromSchedule(Schedule schedule) {
		//use to remember if the task has already been treated : 0 = no / 1 = yes
		int[][] tasksTreated = new int[instance.numJobs][instance.numTasks];
		
		//use to remember how many task has been done on a machine
		int[] machineTask = new int[instance.numMachines];
		Arrays.fill(machineTask,0);
		
		//initialise tasksTreated
		for (int i = 0; i < instance.numJobs; i++) {
			for (int j = 0; j < instance.numTasks; j++) {
				tasksTreated[i][j] = 0;
			}
		}
		
		//main loop
		int tasksRemaining = instance.numJobs * instance.numTasks;
		while(tasksRemaining > 0) {
			int[] nextTask = findNextTask(schedule, tasksTreated);
			int machine = instance.machine(nextTask[0], nextTask[1]);
			this.matrixOfTask[machine][machineTask[machine]] = new ResourceTuple(nextTask[0],nextTask[1]);
			machineTask[machine]++;
			tasksTreated[nextTask[0]][nextTask[1]]++;
			tasksRemaining--;
		}
		
	}
	
	/**
	 * determine if the array contains the tuple
	 * @param taskScheduled
	 * @param tuple
	 * @return true or false
	 */
	private boolean hasBeenScheduled (ResourceTuple[] taskScheduled, ResourceTuple tuple) {
		boolean found = false;
		
		int k = 0;
		while((k < taskScheduled.length) && (!found)) {
			if(taskScheduled[k].equals(tuple)) {
				found = true;
			}
			k++;
		}
		
		return found;
	}
	
	/**
	 * find with which machine/resource the tuple match 
	 * @param matrixOfTask
	 * @param tuple
	 * @return the number of the machine
	 */
	private int findMachine (ResourceTuple[][] matrixOfTask,ResourceTuple tuple) {
		int row = -1;
		boolean found = false;
		
		int i = 0;
		int j = 0;
		while((i < instance.numMachines) && (!found)) {
			while((j < instance.numJobs) && (!found)) {
				found = matrixOfTask[i][j].equals(tuple);
				j++;
			}
			j = 0;
			i++;
		}
		
		row = i - 1; //to counter the i++ at the end of the while
		return row;
	}
	
	/**
	 * test if all the previous tasks has been scheduled for a resource an tuple given
	 * @param matrixOfTasks
	 * @param taskScheduled
	 * @param resource
	 * @param tuple
	 * @return true or false
	 */
	private boolean checkResourceSchedule(ResourceTuple[][] matrixOfTasks, ResourceTuple[] taskScheduled , int resource, ResourceTuple tuple) {
		boolean result = true;
		
		int column = -1;
		for(int j = 0; j < instance.numJobs; j++) {
			if(matrixOfTasks[resource][j].equals(tuple)) {
				column = j;
			}
		}

		if (column !=  0) {
			while ((column <= 0) && result) {
				result = hasBeenScheduled(taskScheduled, matrixOfTasks[resource][column]);
				column--;
			}
		}
		
		return result;
	}
	
	/**
	 * create a schedule from a resource order
	 * @return a schedule representation
	 */
	public Schedule toSchedule() {
		try {
			//use to remember whose tasks has been scheduled
			ResourceTuple taskScheduled[] = new ResourceTuple [instance.numTasks * instance.numJobs];
			Arrays.fill(taskScheduled, new ResourceTuple(-1,-1));
			
			//use to remember whose tasks hasn't been scheduled yet
			ResourceTuple taskWaiting[] = new ResourceTuple [instance.numTasks * instance.numJobs];
			
			// time at which each machine is going to be freed
	        int[] nextFreeTimeResource = new int[instance.numMachines];
	        Arrays.fill(nextFreeTimeResource, 0);
	        
	        // for each task, its start time
	        int[][] startTimes = new int[instance.numJobs][instance.numTasks];
			
			//initialise taskWaiting with all the tasks
			int k = 0;
			for (int i = 0; i < instance.numMachines; i++) {
				for (int j = 0; j < instance.numJobs; j++) {
					taskWaiting[k] = this.matrixOfTask[i][j];
					k++;
				}
			}
			
			//main loop
			/* conditions to scheduled a task :
			 * 1: all the previous tasks on this job has been scheduled
			 * 2: all the previous tasks on this machine has been scheduled  
			 */
			int tasksRemaining = (instance.numTasks * instance.numJobs);
			while (tasksRemaining > 0) {
				int taskNumber = 0;
				while (taskNumber < (instance.numTasks * instance.numJobs)) {
					if(!taskWaiting[taskNumber].equals(new ResourceTuple(-1,-1))) { //the task hasn't been scheduled yet
						
						ResourceTuple currentTuple = taskWaiting[taskNumber];
						
						//check condition 1
						boolean condition1;
						if((currentTuple.getTask() == 0)) { //first task to do
							condition1 = true;
						}
						else {
							int taskToCheck = currentTuple.getTask() - 1;
							int jobToCheck = currentTuple.getJob();
							condition1 = true;
							while(condition1 && (taskToCheck >= 0)) { //check if the previous tasks have been done
								condition1 = hasBeenScheduled(taskScheduled,new ResourceTuple(jobToCheck,taskToCheck));
								taskToCheck--;
							}
						}
						
						//check condition 2
						int machine = -1;
						boolean condition2 = false;
						if(condition1) { //no need to check if the first one is false
							machine = findMachine(this.matrixOfTask,currentTuple);
							condition2 = checkResourceSchedule(this.matrixOfTask, taskScheduled, machine, currentTuple);
						}
						
						//Scheduled the task
						if(condition2) { //we already know that the first one is true
					        
							//get the value from the previous task on this job
							int jobTiming;
							if (currentTuple.getTask() == 0) { //it's the first task for this job
								jobTiming = 0;
							}
							else {
								jobTiming = startTimes[currentTuple.getJob()][currentTuple.getTask()-1] + instance.duration(currentTuple.getJob(), currentTuple.getTask()-1);
							}
							
							//get the value from the previous task on the machine
							int machineTiming = nextFreeTimeResource[machine];
							
							//keep the later
							int startingTime = Math.max(jobTiming, machineTiming);
							
							//update all the values
							taskScheduled[taskNumber] = currentTuple;
							taskWaiting[taskNumber] = new ResourceTuple(-1,-1);
							startTimes[currentTuple.getJob()][currentTuple.getTask()]= startingTime;
							nextFreeTimeResource[machine] = startingTime + instance.duration(currentTuple.getJob(), currentTuple.getTask());
							tasksRemaining--;
						}
					}
					taskNumber++;
				}
			}
			
			return new Schedule(instance, startTimes);
		}
		catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		String display = "";
		
		for (int i = 0; i < instance.numMachines; i++) {
			display =  display + "Resource " + i + " | ";
			for (int j = 0; j < instance.numJobs; j++) {
				display = display + this.matrixOfTask[i][j] + " | ";
			}
			display = display + "\n";
		}
		
		return display;
	}
	
}
