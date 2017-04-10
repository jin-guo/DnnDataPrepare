package Evaluation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import Data.Link;


public class Evaluator {
	Set<String> testLinks;
	Set<String> validLinks;
	Set<String> nonValidLinks;
	int validLinkCount;
	
	public Evaluator(){
		testLinks = new HashSet<String>();
		validLinks = new HashSet<String>();
		nonValidLinks = new HashSet<String>();
		validLinkCount = 0;
	}
	
	private void createSubset(double size) {
		
		validLinks = randomSelectElement(validLinks, (int) ((int)validLinks.size()*size)); 
		nonValidLinks = randomSelectElement(nonValidLinks, (int) ((int)nonValidLinks.size()*size));
		testLinks.clear();
		testLinks.addAll(validLinks);
		testLinks.addAll(nonValidLinks);
		writeTestLinks("RandomSubTestSet.txt");
	}
	
	private void writeTestLinks(String filePath) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(filePath);
			for(String eachLink:testLinks) {
				writer.println(eachLink);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return;
		
	}

	private Set<String> randomSelectElement(Set<String> inputSet, int size) {

		List<String> toReturn = new ArrayList<String>(inputSet);
		Collections.shuffle(toReturn);
		return new HashSet<String> (toReturn.subList(0, size));
	}
	
	public void readTestLinks(String filePath) {
		try {			
			// Read file with encoding of UTF-8.
			BufferedReader bufferedReader = 
			        new BufferedReader(new InputStreamReader(
		                      new FileInputStream(filePath), "UTF8"));
			
			String line = bufferedReader.readLine();
		    while((line = bufferedReader.readLine()) != null) {
		    	String[] content = line.split("\t");
				if(content.length<4)
					continue;
				String key = content[0];
				testLinks.add(key);
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
				String key = sourceId + "::" + content[1];
				if(!testLinks.contains(key))
					continue;
				Link link = new Link(content[0], content[1]);
				double score;
				try {
					score = Double.parseDouble(content[2]);
				} catch(NumberFormatException e) {
					continue; 
				}
				link.setEvaluationScore(score);
				// For LSI result, true link is 1.
				if(content[3].equals("2") || content[3].toLowerCase().equals("true")) {
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
				System.out.print(ap + ",");

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
//		compareMap();
		generatePrecisionRecallGraphDatapoint();
	}

	private static void generatePrecisionRecallGraphDatapoint() {
//		String nn_resultPath = "/Users/Jinguo/Dropbox/Projects/2016_summer/CameraReady/experiment10sample/Sample_5.csv";
String nn_resultPath = "/Users/Jinguo/Dropbox/Projects/2016_summer/experiment/ForRebuttal/80_10_10_additional_2.csv";	
		String vsm_resultPath = "/Users/Jinguo/Documents/workspace/RAISE_Ontology/VSM_LocalIDF_PTC_big.txt";
		String lsi_resultPath = "/Users/Jinguo/Dropbox/Projects/2016_summer/experiment/experiment2/LSI_25.csv";
		Evaluator map = new Evaluator();
		map.readTestLinks("/Users/Jinguo/Dropbox/TraceNN_experiment/tracenn/data/trace_80_10_10_increase_from_45_additional_2/splitted_testing_additional_2.txt");
//		map.createSubset(0.222);
		
		Map<String, List<Link>> nn_resultPerSource = map.readEvaluationResult(nn_resultPath);
		Map<String, List<Link>> vsm_resultPerSource = map.readEvaluationResult(vsm_resultPath);
		Map<String, List<Link>> lsi_resultPerSource = map.readEvaluationResult(lsi_resultPath);

		double nn_mapScore = map.calculateMAP(nn_resultPerSource);
		System.out.println("NN MAP:"+ nn_mapScore);

		double vsm_mapScore = map.calculateMAP(vsm_resultPerSource);
		System.out.println("VSM MAP:"+ vsm_mapScore);

		double lsi_mapScore = map.calculateMAP(lsi_resultPerSource);
		System.out.println("LSI MAP:"+ lsi_mapScore);
		
		System.out.println("Generate Datapoint for NN:");
		generateDataPoint(map, nn_resultPerSource);
		System.out.println("Generate Datapoint for VSM:");
		generateDataPoint(map, vsm_resultPerSource);
		System.out.println("Generate Datapoint for LSI:");
		generateDataPoint(map, lsi_resultPerSource);
		
		
	}

	public static void generateDataPoint(Evaluator map, Map<String, List<Link>> resultPerSource) {
		List<Link> allLinks = new ArrayList<Link>();
		for(String key: resultPerSource.keySet()) {
			allLinks.addAll(resultPerSource.get(key));
		}
		Collections.sort(allLinks);
		double maxScore = allLinks.get(0).getEvaluationScore();
		double minScore = 0;
		int datapoint = 100000;
		double intervel = (maxScore-minScore)/datapoint;
		Set<PrecisionRecallPoint> dataSet = new HashSet<PrecisionRecallPoint>();
		for(double i=intervel+minScore;i<=maxScore;i=i+intervel) {
			int validLinkSoFarCount = 0;
			int allLinkSoFarCount = 0;
			for(int n=0;n<allLinks.size();n++) {
				allLinkSoFarCount++;
				Link currentLink = allLinks.get(n);
				if(currentLink.getEvaluationScore()<i) {
					break;
				}
				if(currentLink.isValid()) {
					validLinkSoFarCount++;
				}
			}
			double precision = (double)validLinkSoFarCount/allLinkSoFarCount;
			double recall = (double)validLinkSoFarCount/map.validLinkCount;
//			System.out.println("Precision:" + precision + "\tRecall:" + recall + "\tThreshold:" + i);
			PrecisionRecallPoint oneDataPoint= new PrecisionRecallPoint();
			oneDataPoint.setPrecision(precision);
			oneDataPoint.setRecall(recall);
			dataSet.add(oneDataPoint);
		}
		
		List<PrecisionRecallPoint> dataList = new ArrayList<PrecisionRecallPoint>(dataSet);
		Collections.sort(dataList);
		int actualDatapoint = 50;
		int count=0;
		System.out.println("Precision\tRecall");
		for(PrecisionRecallPoint eachData:dataList) {
			if(eachData.getRecall()> (double)count/actualDatapoint) {
				System.out.println(eachData.getPrecision() + "\t" + eachData.getRecall());
				count++;
			}
		}
	}

	public static void compareMap() {
//				String nn_resultPath = "/Users/Jinguo/Dropbox/Projects/2016_summer/experiment/experiment2/1471638239.2065_OnTestData_80_10_10_best_model.csv";	
				String nn_resultPath = "/Users/Jinguo/Dropbox/Projects/2016_summer/experiment/experiment2/1470992031.4712_OnTestData_45_45_10.csv";	
				String vsm_resultPath = "/Users/Jinguo/Documents/workspace/RAISE_Ontology/VSM_LocalIDF_PTC_big.txt";
				String lsi_resultPath = "/Users/Jinguo/Dropbox/Projects/2016_summer/experiment/experiment2/LSI_25.csv";
				Evaluator map = new Evaluator();
				map.readTestLinks(nn_resultPath);
				map.createSubset(0.222);
				Map<String, List<Link>> nn_resultPerSource = map.readEvaluationResult(nn_resultPath);
				Map<String, List<Link>> vsm_resultPerSource = map.readEvaluationResult(vsm_resultPath);
				Map<String, List<Link>> lsi_resultPerSource = map.readEvaluationResult(lsi_resultPath);
		
				double nn_mapScore = map.calculateMAP(nn_resultPerSource);
				System.out.println("NN MAP:"+ nn_mapScore);
		
				double vsm_mapScore = map.calculateMAP(vsm_resultPerSource);
				System.out.println("VSM MAP:"+ vsm_mapScore);
		
				double lsi_mapScore = map.calculateMAP(lsi_resultPerSource);
				System.out.println("LSI MAP:"+ lsi_mapScore);
		
//				Map<String, List<Link>> combined_resultPerSource =  
//						EvaluationCombiner.combineResult(nn_resultPerSource, vsm_resultPerSource, false, false, "Average");	
//				double combined_mapScore = map.calculateMAP(combined_resultPerSource);
//				System.out.println("Combined MAP:"+ combined_mapScore);
	}

	
}
