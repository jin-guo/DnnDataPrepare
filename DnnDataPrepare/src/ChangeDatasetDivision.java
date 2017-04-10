import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChangeDatasetDivision {
	List<String> validLinks;
	List<String> nonValidLinks;
	List<String> mergeSet;
	List<String> splitSet;
	
	public static void main(String args[]) throws IOException
	{
		ChangeDatasetDivision changeDatasetDivision = new ChangeDatasetDivision();
		changeDatasetDivision.readMergeSet("/Users/Jinguo/Dropbox/TraceNN_experiment/tracenn/data/trace_80_10_10_increase_from_45_additional_2/train_symbol_0.txt");
		changeDatasetDivision.readSplitSet("/Users/Jinguo/Dropbox/TraceNN_experiment/tracenn/data/trace_80_10_10_increase_from_45_additional_2/test_symbol_0.txt");
		changeDatasetDivision.generateNewDataset((double)10/45);
	}
	
	public ChangeDatasetDivision() {
		validLinks = new ArrayList<String>();
		nonValidLinks = new ArrayList<String>();
		mergeSet = new ArrayList<String>();
		splitSet = new ArrayList<String>();
	}
	 
	private void generateNewDataset(double splitRatio) {
		splitData(validLinks, splitRatio);
		splitData(nonValidLinks, splitRatio);
		Collections.shuffle(mergeSet);
		Collections.shuffle(splitSet);
		writeDatasetToFile(mergeSet, "merged_training_additional_9.txt");	
		writeDatasetToFile(splitSet, "splitted_testing_additional_9.txt");
		
	}
	
	private void writeDatasetToFile(List<String> mergeSet, String filePath) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(filePath);
			writer.println("pair_ID" + "\t" + "sentence_A" + "\t" + "sentence_B"
			 + "\t" + "relatedness_score" + "\t" + "entailment_judgment");
			for(String datapoint: mergeSet) {
				writer.println(datapoint);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
			
			
	}
	
	private void splitData(List<String> linkList, double splitRatio) {
		Collections.shuffle(linkList);
		int splitIndex = (int) (linkList.size()*splitRatio);
		for(int i=0;i<splitIndex;i++) {
			splitSet.add(linkList.get(i));
		}
		for(int i=splitIndex;i<linkList.size();i++) {
			mergeSet.add(linkList.get(i));
		}
	}
	
	private void readMergeSet(String filePath) {
		try {			
			// Read file with encoding of UTF-8.
			BufferedReader bufferedReader = 
			        new BufferedReader(new InputStreamReader(
		                      new FileInputStream(filePath), "UTF8"));
			
			String line = bufferedReader.readLine();
		    while((line = bufferedReader.readLine()) != null) {
		    	mergeSet.add(line);
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
	
	private void readSplitSet(String filePath) {
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
				if(content[3].equals("2") || content[3].toLowerCase().equals("true")) {
					validLinks.add(line);
				} else {
					nonValidLinks.add(line);	
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
	}

}
