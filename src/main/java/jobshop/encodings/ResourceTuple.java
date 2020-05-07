package jobshop.encodings;

public class ResourceTuple implements Comparable<ResourceTuple> {

	int job;
	
	int task;
	
	public ResourceTuple (int job, int task) {
		this.job = job;
		this.task = task;
	}
	
	public int getJob() {
		return this.job;
	}
	
	public int getTask() {
		return this.task;
	}
	
	@Override
	public int compareTo(ResourceTuple tuple) throws NullPointerException, IllegalArgumentException{
		if(this.job != tuple.getJob()) {
			throw new IllegalArgumentException("You can only compare task between the same jobs");
		}
		else {
			int equal;
			
			if(this.task == tuple.getTask()) {
				equal = 0;
			}
			else if (this.task < tuple.getTask()) {
				equal = -1;
			}
			else {
				equal = 1;
			}
			return equal;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		
		// If the object is compared with itself then return true   
        if (obj == this) { 
            return true; 
        }
        
        /* Check if obj is an instance of ResourceTuple or not 
        "null instanceof [type]" also returns false */
        if (!(obj instanceof ResourceTuple)) { 
        	return false; 
        }
        
        // typecast obj to ResourceTuple so that we can compare data members  
        ResourceTuple tuple = (ResourceTuple) obj; 
		
		return ((this.job == tuple.getJob()) && (this.task == tuple.getTask()));
	}
	
	@Override
	public String toString() {
		return "(" + this.job + "," + this.task + ")";
	}
	
}
