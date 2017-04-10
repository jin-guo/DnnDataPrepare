package Data;

public class Link implements Comparable<Link> {
	String sourceId;
	String targetId;
	boolean valid;
	double evaluationScore;
	String linkType;
	
	public Link(String sourceId, String targetId) {
		this.sourceId = sourceId;
		this.targetId = targetId;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getSourceId() {
		return sourceId;
	}

	public String getTargetId() {
		return targetId;
	}
	public String getLinkType() {
		return linkType;
	}
	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}

	public double getEvaluationScore() {
		return evaluationScore;
	}

	public void setEvaluationScore(double evaluationScore) {
		this.evaluationScore = evaluationScore;
	}

	// Sort the link by evaluation score from big to small
	@Override
	public int compareTo(Link o) {
		if(this.evaluationScore > o.evaluationScore)
			return -1;
		else if(this.evaluationScore < o.evaluationScore)
			return 1;
		return 0;
	}
	
	
}
