package nl.junglecomputing.constellation.vectoradd;

// Good practice in Constellation: separate actual computaton from the
// orchestration
class ComputeVectorAdd {

    static void compute(float[] c, float[] a, float[] b) {
	for (int i = 0; i < c.length; i++) {
	    c[i] = a[i] + b[i];
	}
    }
}
