package Evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Data.Link;

public class EvaluationCombiner {
	
	public static Map<String, List<Link>> combineResult(Map<String, List<Link>> result1, Map<String, List<Link>> result2, 
			boolean normalize1, boolean normalize2, String combineMethod) {
		Map<String, List<Link>> combinedResult = new HashMap<String, List<Link>>();
		for(String sourceId: result1.keySet()) {
			List<Link> combinedLinkList = new ArrayList<Link>();
			if(!result2.containsKey(sourceId))
				continue;
			List<Link> linkList1 = result1.get(sourceId);
			Collections.sort(linkList1);
			List<Link> linkList2 = result2.get(sourceId);
			double max1 = linkList1.get(0).getEvaluationScore();
			Collections.sort(linkList2);
			double max2 = linkList2.get(0).getEvaluationScore();
			int rank1 = 0;
			for(Link link:linkList1) {
				rank1++;
				Link combined = new Link(link.getSourceId(), link.getTargetId());
				combined.setValid(link.isValid());
				double score1 = link.getEvaluationScore();
				if(normalize1)
					score1 = score1/max1;
				int rank2 = 0;
				for(Link link2:linkList2) {
					rank2++;
					if(!link2.getSourceId().equals(combined.getSourceId()))
						continue;
					else if(!link2.getTargetId().equals(combined.getTargetId()))
						continue;
					double score2 = link2.getEvaluationScore();
					if(normalize2)
						score2 = score2/max2;
					double combined_score = 0;
					if(combineMethod.equals("Max"))
						combined_score = Math.max(score1, score2);
					else if(combineMethod.equals("Mltp")) 
						combined_score = score1*score2;
					else if(combineMethod.equals("Rank")) 
						combined_score = -Math.min(rank1, rank2);
					else
						combined_score = (score1+score2)/2;
					combined.setEvaluationScore(combined_score);
					combinedLinkList.add(combined);
					break;			
				}
			}
			combinedResult.put(sourceId, combinedLinkList);
			
		}
		return combinedResult;
	}
}
