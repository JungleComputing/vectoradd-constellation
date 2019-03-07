package nl.junglecomputing.constellation.vectoradd;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Activity;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Context;
import ibis.constellation.Constellation;
import ibis.constellation.Event;
import ibis.constellation.NoSuitableExecutorException;
import ibis.constellation.Timer;

class VectorAddActivity extends Activity {

    static final String LABEL = "vectoradd";
    
    private static Logger logger = LoggerFactory.getLogger(VectorAddActivity.class);
    
    private static final boolean EXPECT_EVENTS = true;
    
    private static final int NR_ACTIVITIES_TO_SUBMIT = 2;

    private ActivityIdentifier parent;
    private int computeDivideThreshold;
    private VectorAddResult result;
    private float[] a;
    private float[] b;

    private int nrReceivedEvents;

    VectorAddActivity(ActivityIdentifier parent, int computeDivideThreshold,
	    int n, float[] a, float[] b) {
	this(parent, computeDivideThreshold, n, a, b, 0);
    }

    VectorAddActivity(ActivityIdentifier parent, int computeDivideThreshold, 
	    int n, float[] a, float[] b, int offset) {
        super(new Context(LABEL), EXPECT_EVENTS);

	// remember our parent and some other data
	this.parent = parent;
        this.computeDivideThreshold = computeDivideThreshold;

        // we create a result data structure with an array of length n with an
        // offset into the parent VectorAddResult.
        this.result = new VectorAddResult(n, offset);
	this.a = a;
	this.b = b;

	this.nrReceivedEvents = 0;

        if (logger.isDebugEnabled()) {
            logger.debug("Initialized with {} elements", n);
        }
    }

    @Override
    public int initialize(Constellation cons) {
	// we are doing the work here.  We find our job size (n) and check
	// whether we have to compute or divide.
	
	int n = result.c.length;
	if (n <= computeDivideThreshold) {

	    String executor = cons.identifier().toString();
	    Timer timer = cons.getTimer("java", executor, "vector add");
	    int timing = timer.start();

	    logger.debug("Compute a vector of size {}", n);
	    
	    ComputeVectorAdd.compute(result.c, a, b);
	    
	    timer.stop(timing);

	    // We are done, indicate that we are ready to cleanup
	    return FINISH;
	}
	else {
	    submit(cons, 0, n/2);
	    submit(cons, n/2, n);

	    // We are not done, we have to wait for two child activities.
	    // This also indicates that we are going to try to steal work.
	    return SUSPEND;
	}
    }


    private void submit(Constellation cons, int start, int end) {
	float[] aCopy = Arrays.copyOfRange(a, start, end);
	float[] bCopy = Arrays.copyOfRange(b, start, end);

	try {
	    cons.submit(new VectorAddActivity(identifier(), computeDivideThreshold,
			    end-start, aCopy, bCopy, start));
	}
	catch (NoSuitableExecutorException e) {
	    logger.error("Submitting VectorAddActivity: {}", e.getMessage());
	}
    }
		

    @Override
    public int process(Constellation cons, Event event) {
        if (logger.isDebugEnabled()) {
            logger.debug("Processing an event");
        }

	nrReceivedEvents++;
	result.add((VectorAddResult) event.getData());
	if (nrReceivedEvents == NR_ACTIVITIES_TO_SUBMIT) {
	    // We are done, move to cleanup.
	    return FINISH;
	}
	else {
	    // We are not done, we have to wait for more child activities.
	    return SUSPEND;
	}
    }


    @Override
    public void cleanup(Constellation cons) {
	logger.debug("Sending an event to my parent");
	cons.send(new Event(identifier(), parent, result));
    }
}
