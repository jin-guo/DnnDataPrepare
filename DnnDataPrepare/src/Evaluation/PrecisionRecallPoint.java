package Evaluation;

public class PrecisionRecallPoint implements Comparable<PrecisionRecallPoint> {
	double precision = 0;
	double recall = 0;
	
	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	public double getRecall() {
		return recall;
	}
	public void setRecall(double recall) {
		this.recall = recall;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(precision);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(recall);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrecisionRecallPoint other = (PrecisionRecallPoint) obj;
		if (Double.doubleToLongBits(precision) != Double.doubleToLongBits(other.precision))
			return false;
		if (Double.doubleToLongBits(recall) != Double.doubleToLongBits(other.recall))
			return false;
		return true;
	}
	@Override
	public int compareTo(PrecisionRecallPoint o) {
		if(o.getRecall()>this.getRecall())
			return -1;
		else if (o.getRecall()<this.getRecall())
			return 1;
		return 0;
	}
	@Override
	public String toString() {
		return "PrecisionRecallPoint [precision=" + precision + ", recall=" + recall + "]";
	}
	
	
	
}
