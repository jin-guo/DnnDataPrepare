package Evaluation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Data.Link;

public class MapCalculator {
	int validLinkCount;
	
	public MapCalculator(){
		validLinkCount = 0;
	}

	public Map<String, List<Link>> readEvaluationResult(String filePath) {	
		validLinkCount = 0;
		Map<String, List<Link>> resultPerSource = new HashMap<String, List<Link>>();

		try {			
			// Read file with encoding of UTF-8.
			BufferedReader bufferedReader = 
			        new BufferedReader(new InputStreamReader(
		                      new FileInputStream(filePath), "UTF8"));
			
			String line;
		    while((line = bufferedReader.readLine()) != null) {
		    	String[] content = line.split(",");
				if(content.length<4)
					continue;
				String sourceId = content[0];
				Link link = new Link(content[0], content[1]);
				double score;
				try {
					score = Double.parseDouble(content[2]);
				} catch(NumberFormatException e) {
					continue; 
				}
				link.setEvaluationScore(score);
				// Answer set: 2 for link and 1 for non-link.
				if(content[3].toLowerCase().equals("2")) {
					link.setValid(true);
					validLinkCount++;
				} else {
					link.setValid(false);
				}
				if(resultPerSource.containsKey(sourceId))
					resultPerSource.get(sourceId).add(link);
				else {
					List<Link> linkList = new ArrayList<Link>();
					linkList.add(link);
					resultPerSource.put(sourceId, linkList);
				}
		    }
		    bufferedReader.close(); 
		}
		catch(FileNotFoundException ex) {
		    System.out.println( "Unable to open file '" + 
		        filePath + "'");                
		}
		catch(IOException ex) {
		    System.out.println( "Error reading file '" 
		        + filePath + "'");                  
		}		
		return resultPerSource;
	}
	
	public double calculateMAP(Map<String, List<Link>> resultPerSource) {
		double map = 0;
		double sourceWithValidlinkCount  =  0;
		System.out.println("AP:");
		for(String sourceId : resultPerSource.keySet()) {
			double ap = calculateAP(resultPerSource.get(sourceId));
			if(ap>0) {
				map = map + ap;
				sourceWithValidlinkCount++;
//				System.out.println(sourceId + "," + ap);
				System.out.println(sourceId + ":" + ap);

			}
			
		}
		System.out.println("Data point #:" + sourceWithValidlinkCount);
		map = map/sourceWithValidlinkCount;
		return map;
	}
	
	private double calculateAP(List<Link> AlllinkForSource) {
		Collections.sort(AlllinkForSource);
		int true_count = 0;
		int total_count = 0;
		double precision_sum = 0;
		for(int i = 0; i<AlllinkForSource.size();i++) {
			total_count++;	
			if(AlllinkForSource.get(i).isValid()) {
				true_count++;
				precision_sum = precision_sum + (double)true_count/total_count;
			}
		}
		if(true_count>0) {
			double ap = precision_sum/true_count;
			return ap;
		} else 
			return 0;
	}
	
	
	public static void main(String args[]) throws IOException
	{
		compareMap();
	}

	public static void compareMap() {
				String resultPath = "testResultFile.csv";	
				MapCalculator map = new MapCalculator();
				Map<String, List<Link>> resultPerSource = map.readEvaluationResult(resultPath);
		
				double mapScore = map.calculateMAP(resultPerSource);
				System.out.println("MAP:"+ mapScore);
		
		}
}
