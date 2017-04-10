import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import opennlp.tools.util.InvalidFormatException;
import Data.Artifact;
import Data.Link;

public class SentencePairCreator {
	Map<String, Artifact> sourceArtifacts;
	Map<String, Artifact> targetArtifacts;
	Map<String, Link> traceLinks;
	int traceLinkCount;
	Map<Integer, Link> datasets;
	boolean balanceDataFlag;
	int nolinkRatio;
	List<String> vocab;
	
	
	public SentencePairCreator(){
		sourceArtifacts = new HashMap<String, Artifact>();
		targetArtifacts = new HashMap<String, Artifact>();
		traceLinks = new HashMap<String, Link>();
		datasets = new HashMap<Integer, Link>();
		balanceDataFlag = true;
		nolinkRatio=0;
		traceLinkCount = 0;
		vocab = new ArrayList<String>();
		
	}
	
	
	public void readFiles(String sourceFile, String targetFile, String answerFile, boolean processFlag) {
		try {
			FileImporter.initializeModels();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		sourceArtifacts = FileImporter.readArtifact(sourceFile, processFlag);
		targetArtifacts = FileImporter.readArtifact(targetFile, processFlag);
		vocab.addAll(FileImporter.getGlobalVocab());
		traceLinks = FileImporter.readAnswerFile(answerFile, "TRACE");	
		createDataset();
	}


	public void createDataset() {
		int count = 0;
		for(String sourceId:sourceArtifacts.keySet()) {
			if(sourceArtifacts.get(sourceId).getContent().length()<10)
				continue;
			for(String targetId:targetArtifacts.keySet()) {
				if(targetArtifacts.get(targetId).getContent().length()<10)
					continue;
				String key = sourceId + "::" + targetId;
				if(traceLinks.containsKey(key)) { 
					datasets.put(count, traceLinks.get(key));
					traceLinkCount++;
				} else {
					Link invalidTraceLink = new Link(sourceId, targetId);
					invalidTraceLink.setValid(false);
					invalidTraceLink.setLinkType("TRACE");
					datasets.put(count, invalidTraceLink);
				}
				count++;	
			}
		}
		System.out.println("Total " + traceLinkCount + " trace links added to the dataset.");
	}
	
	// Create Three files: Training, Validation, and Testing
	public void generateDataset(double trainRatio, double validationRatio, boolean writeAllTestDataFlag){
		Random generator = new Random();
		int count = 0;
		List<Integer> pickedIndexForUnderSampling = new ArrayList<Integer>();
		if(balanceDataFlag) {
			// Add nolinkRatio * traceLinkCount non-link to dataset
			while(count<nolinkRatio * traceLinkCount) {
				int i = generator.nextInt(datasets.keySet().size());
				if(!pickedIndexForUnderSampling.contains(i) && !datasets.get(i).isValid()) {
					pickedIndexForUnderSampling.add(i);
					count++;
				}
			}
		
			// Add link to dataset
			for(int i=0; i<datasets.size();i++) {
				if(datasets.get(i).isValid()) {
					pickedIndexForUnderSampling.add(i);
				}
			}
		} else {
			// Do not balance data
			for(int i=0; i<datasets.size();i++) {
				pickedIndexForUnderSampling.add(i);	
			}
		}
		
		Collections.shuffle(pickedIndexForUnderSampling);
	
		int trainSize = (int) (pickedIndexForUnderSampling.size()*trainRatio);
		int validationSize = (int) (pickedIndexForUnderSampling.size()*validationRatio);
		
		writeVocab("vocab_ptc_artifact_small.txt");
//		
//		writeDataset("train_symbol_" + nolinkRatio + "_set3.txt", pickedIndexForUnderSampling, 0, trainSize);
//		writeDataset("validation_symbol_" + nolinkRatio + "_set3.txt", pickedIndexForUnderSampling, trainSize, trainSize + validationSize);
//		writeDataset("test_symbol_"+ nolinkRatio + "_set3.txt", pickedIndexForUnderSampling, trainSize + validationSize, pickedIndexForUnderSampling.size());
		writeArtifact("SourceArtifact_small.txt", sourceArtifacts);
		writeArtifact("TargetArtifact_small.txt", targetArtifacts);
		
		if(writeAllTestDataFlag)
			writeAllTestDataset("test_symbol_" + nolinkRatio + "_small.txt", pickedIndexForUnderSampling, 0, trainSize + validationSize);
		
	}


	private void writeAllTestDataset(String filePath, List<Integer> pickedIndexForUnderSampling, int startIndex, int endIndex) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(filePath);
			writer.println("pair_ID" + "\t" + "sentence_A" + "\t" + "sentence_B"
			 + "\t" + "relatedness_score" + "\t" + "entailment_judgment");
			Set<Integer> testIndex = datasets.keySet();
			Set<Integer> trainAndDevIndex = new HashSet<Integer>(pickedIndexForUnderSampling.subList(startIndex, endIndex));
			testIndex.removeAll(trainAndDevIndex);
			for(int i:testIndex) {
				Link link = datasets.get(i);
				String key = link.getSourceId() + "::" + link.getTargetId();
				writer.print(key + "\t");
				// Only write the artifact id instead of the original sentence.
				writer.print(link.getSourceId() + "\t");
				writer.print(link.getTargetId() + "\t");
//				writer.print(sourceArtifacts.get(link.getSourceId()).getContent() + "\t");
//				writer.print(targetArtifacts.get(link.getTargetId()).getContent() + "\t");
				if(link.isValid())
					writer.print("2" + "\t");
				else
					writer.print("1" + "\t");
				writer.print(link.getLinkType());
				writer.print("\n");

			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return;
		
	}


	private void writeArtifact(String filePath, Map<String, Artifact> artifacts) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(filePath);
			for(Artifact artifact:artifacts.values()) {
				writer.println(artifact.getId() + '\t' + artifact.getContent());
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return;
		
	}


	private void writeVocab(String filePath) {
		PrintWriter writer;
		try {
			Collections.sort(vocab);
			writer = new PrintWriter(filePath);
			for(String word:vocab) {
				writer.println(word);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return;
	}


	private void writeDataset(String filePath, List<Integer> list, int startIndex, int endIndex) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(filePath);
			writer.println("pair_ID" + "\t" + "sentence_A" + "\t" + "sentence_B"
			 + "\t" + "relatedness_score" + "\t" + "entailment_judgment");
			for(int i=startIndex; i<endIndex; i++) {
				Link link = datasets.get(list.get(i));
				String key = link.getSourceId() + "::" + link.getTargetId();
				writer.print(key + "\t");
				// Only write the artifact id instead of the original sentence.
				writer.print(link.getSourceId() + "\t");
				writer.print(link.getTargetId() + "\t");
//				writer.print(sourceArtifacts.get(link.getSourceId()).getContent() + "\t");
//				writer.print(targetArtifacts.get(link.getTargetId()).getContent() + "\t");
				if(link.isValid())
					writer.print("2" + "\t");
				else
					writer.print("1" + "\t");
				writer.print(link.getLinkType());
				writer.print("\n");

			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
			
	}
	
	public void writeAllDataset(String filePath) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(filePath);
			writer.println("pair_ID" + "\t" + "sentence_A" + "\t" + "sentence_B"
			 + "\t" + "relatedness_score" + "\t" + "entailment_judgment");
			for(int i: datasets.keySet()) {
				Link link = datasets.get(i);
				String key = link.getSourceId() + "::" + link.getTargetId();
				writer.print(key + "\t");
				writer.print(sourceArtifacts.get(link.getSourceId()).getContent() + "\t");
				writer.print(targetArtifacts.get(link.getTargetId()).getContent() + "\t");
				if(link.isValid())
					writer.print("2" + "\t");
				else
					writer.print("1" + "\t");
				writer.print(link.getLinkType());
				writer.print("\n");
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
			
	}
	
	public static void main(String args[]) throws IOException
	{
		SentencePairCreator datasetCreator = new SentencePairCreator();
//		String sourceFile = "/Users/Jinguo/Dropbox/Git/OntologyMiningFromTracing/data/Artifacts/PTC_new_data/SSRS.xml";
//		String targetFile = "/Users/Jinguo/Dropbox/Git/OntologyMiningFromTracing/data/Artifacts/PTC_new_data/SSDD2_new.xml";
//		String answerFile = "/Users/Jinguo/Dropbox/Git/OntologyMiningFromTracing/data/Artifacts/PTC_new_data/TraceMatrixSSRStoSSDD.xml";
//		datasetCreator.readFiles(sourceFile, targetFile, answerFile, false);
//		datasetCreator.balanceData(false, 20);
//		datasetCreator.generateDataset(0.8, 0.1, false);
		
		String sourceFile = "PTC_srs_full/source_srs.xml";
		String targetFile = "PTC_srs_full/target_sdd.xml";
		String answerFile = "PTC_srs_full/answer_srs_sdd.xml";
		
		datasetCreator.readFiles(sourceFile, targetFile, answerFile, false);
		datasetCreator.balanceData(false, 20);
		datasetCreator.generateDataset(0.8, 0.1, true);

	}


	private void balanceData(boolean b, int ratio) {
		this.balanceDataFlag = b;
		if(b)
			this.nolinkRatio = ratio;
	}

}
