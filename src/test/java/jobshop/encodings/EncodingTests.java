package jobshop.encodings;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.solvers.BasicSolver;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class EncodingTests {

    @Test
    public void testJobNumbers() throws IOException {
    	System.out.println("-----Test Job Numbers-----");
    	
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
        
        System.out.println("-----Test toSchedule-----");

        // numéro de jobs : 1 2 2 1 1 2 (cf exercices)
        JobNumbers enc1 = new JobNumbers(instance);
        enc1.jobs[enc1.nextToSet++] = 0;
        enc1.jobs[enc1.nextToSet++] = 1;
        enc1.jobs[enc1.nextToSet++] = 1;
        enc1.jobs[enc1.nextToSet++] = 0;
        enc1.jobs[enc1.nextToSet++] = 0;
        enc1.jobs[enc1.nextToSet++] = 1;

        Schedule sched1 = enc1.toSchedule();
        System.out.println(sched1);
        assert sched1.isValid();
        assert sched1.makespan() == 12;



        // numéro de jobs : 1 1 2 2 1 2
        JobNumbers enc2 = new JobNumbers(instance);
        enc2.jobs[enc2.nextToSet++] = 0;
        enc2.jobs[enc2.nextToSet++] = 0;
        enc2.jobs[enc2.nextToSet++] = 1;
        enc2.jobs[enc2.nextToSet++] = 1;
        enc2.jobs[enc2.nextToSet++] = 0;
        enc2.jobs[enc2.nextToSet++] = 1;

        Schedule sched2 = enc2.toSchedule();
        System.out.println(sched2);
        assert sched2.isValid();
        assert sched2.makespan() == 14;
        
        System.out.println("-----Test fomSchedule-----");
        
        JobNumbers enc3 = new JobNumbers(instance);
        enc3.fromSchedule(sched1);
        System.out.println(enc3);
        
        JobNumbers enc4 = new JobNumbers(instance);
        enc4.fromSchedule(sched2);
        System.out.println(enc4);
    }
    
    @Test
    public void testResourceOrder() throws IOException {
    	System.out.println("-----Test Resource Order-----");
    	
    	Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
    	
    	System.out.println("Test init() :");
    	
    	ResourceOrderOld matrix1 = new ResourceOrderOld(instance);
    	int order1[] = {0,1,1,0,0,1};
    	matrix1.fromOrder(order1);
    	System.out.println(matrix1);
    	
    	ResourceOrderOld matrix2 = new ResourceOrderOld(instance);
    	int order2[] = {0,0,1,1,0,1};
    	matrix2.fromOrder(order2);
    	System.out.println(matrix2);
    	
    	System.out.println("Test toSchedule() :");
    	
    	Schedule sched1 = matrix1.toSchedule();
    	System.out.println(sched1);
    	assert sched1.isValid();
    	assert sched1.makespan() == 12;
    	
    	Schedule sched2 = matrix2.toSchedule();
        System.out.println(sched2);
        assert sched2.isValid();
        assert sched2.makespan() == 14;
        
        System.out.println("Test fromSchedule() :");
        
        ResourceOrderOld matrix3 = new ResourceOrderOld(instance);
    	matrix3.fromSchedule(sched1);
    	System.out.println(matrix3);
    	
    	ResourceOrderOld matrix4 = new ResourceOrderOld(instance);
    	matrix4.fromSchedule(sched2);
    	System.out.println(matrix4);
    }

    @Test
    public void testBasicSolver() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // build a solution that should be equal to the result of BasicSolver
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 12;

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        assert result.schedule.makespan() == sched.makespan(); // should have the same makespan
    }

}
