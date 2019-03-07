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

	this.parent = parent;
        this.computeDivideThreshold = computeDivideThreshold;

        // we create a result data structure with an array of length n, and an
        // offset of offset
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
	int n = result.c.length;
	if (n <= computeDivideThreshold) {
	    ComputeVectorAdd.compute(result.c, a, b);
	    return FINISH;
	}
	else {
	    submit(cons, 0, n/2);
	    submit(cons, n/2, n);

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
	    return FINISH;
	}
	else {
	    return SUSPEND;
	}
    }


    @Override
    public void cleanup(Constellation cons) {
	cons.send(new Event(identifier(), parent, result));
    }
}
