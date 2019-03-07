package nl.junglecomputing.constellation.vectoradd;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.StealStrategy;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Timer;
import ibis.constellation.util.SingleEventCollector;

public class VectorAdd {

    static Logger logger = LoggerFactory.getLogger(VectorAdd.class);

    public static void writeFile(float[] array) {
        try {
            PrintStream out = new PrintStream("vectoradd.out");
            for (int i = 0; i < array.length; i++) {
                out.println(array[i]);
            }
            out.close();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
	// the number of executors per node in the cluster
        int nrExecutorsPerNode = 4;
	
	// the threshold to decide whether to compute or divide tasks
	int computeDivideThreshold = 256;

        // size of the vectors to add
        int n = 8192;

	// number of nodes in the cluster
	int nrNodes = 1;

	// determine the number of tasks based on the size of the pool of nodes
        String ibisPoolSize = System.getProperty("ibis.pool.size");
        if (ibisPoolSize != null) {
            nrNodes = Integer.parseInt(ibisPoolSize);
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-nrExecutorsPerNode")) {
                i++;
                nrExecutorsPerNode = Integer.parseInt(args[i]);
            } else if (args[i].equals("-computeDivideThreshold")) {
                i++;
                computeDivideThreshold = Integer.parseInt(args[i]);
            } else if (args[i].equals("-n")) {
                i++;
                n = Integer.parseInt(args[i]);
            } else {
                throw new Error("Usage: java VectorAdd "
                        + "[ -nrExecutorsPerNode <num> ] "
                        + "[ -computeDivideThreshold <num> ] "
                        + "[ -n <num> ]");
            }
        }

        // set up the input data
        float[] a = new float[n];
        for (int i = 0; i < n; i++)
            a[i] = i;

        // b contains all zeros
        float[] b = new float[n];

        // Initialize cashmere with default executors for constellation.

        // This means that there is an executor that executes activities with
        // contexts 'global activity context 1', does not steal jobs, and
        // executors that steal from stealpool 'global 0' can steal its
        // activities.

        // In addition there are several executors (set by the property
        // 'cashmere.nGlobalExecutors') that execute jobs with label 'global
        // activity context 0', steal from stealpool 'global 0', and executors
        // that steal from stealpool 'local 0' can steal their activities.

        // Finally, there are several executors (set by the property
        // 'cashmere.nLocalExecutors') that execute jobs with label 'local 0',
        // steal from stealpool 'local 0', and executors that steal from
        // stealpool 'local 1' can steal their activities.
        // retrieve constellation from cashmere.

	ConstellationConfiguration config = 
	  new ConstellationConfiguration(new Context(VectorAddActivity.LABEL), 
		  StealStrategy.SMALLEST, StealStrategy.BIGGEST, 
		  StealStrategy.BIGGEST);

	Constellation constellation = 
	    ConstellationFactory.createConstellation(config, nrExecutorsPerNode);

	constellation.activate();

        if (constellation.isMaster()) {
            System.out.println("VectorAdd, running with n: " + n);
            System.out.println("I am the master!");

            // set up the various activities, staring with the main activity:

            // The SingleEventCollector is an activity that waits for a single
            // event to come in will finish then. We associate context 'global
            // activity context 1' with it.
            SingleEventCollector sec = new SingleEventCollector(new Context(VectorAddActivity.LABEL));

            // submit the single event collector
            ActivityIdentifier aid = constellation.submit(sec);
            // submit the vectorAddActivity. Set the parent as well.
            constellation.submit(new VectorAddActivity(aid, computeDivideThreshold, n, a, b));

            logger.debug("main(), just submitted, about to waitForEvent() "
                    + "for any event with target " + aid);
            VectorAddResult result = 
		(VectorAddResult) sec.waitForEvent().getData();
            logger.debug("main(), done with waitForEvent() on identifier "
		    + aid);

            writeFile(result.c);
        }
        logger.debug("calling Constellation.done()");
        constellation.done();
        logger.debug("called Constellation.done()");
    }
}
