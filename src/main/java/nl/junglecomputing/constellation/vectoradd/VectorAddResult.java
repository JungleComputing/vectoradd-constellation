package nl.junglecomputing.constellation.vectoradd;

class VectorAddResult implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    float[] c;
    private int offsetInParent;


    VectorAddResult(int n, int offsetInParent) {
	this.c = new float[n];
	this.offsetInParent = offsetInParent;
    }

    synchronized void add(VectorAddResult other) {
	for (int i = 0; i < other.c.length; i++) {
	    this.c[i + other.offsetInParent] = other.c[i];
	}
    }
}
