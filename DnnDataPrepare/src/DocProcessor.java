/**
 * Process document in format of txt or xml
 * 
 * @author  Jin Guo
 * @version 2.0
 * @since   2014-03-31 
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import Data.SentenceTuple;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class DocProcessor {
	Tokenizer tokenizer;
	SentenceDetectorME sentenceDetector;
	StringBuilder sb;
	boolean initializedFlag;
	Set<String> vocab;
	
	DocProcessor() {
		sb = new StringBuilder();
		initializedFlag = false;
		vocab = new HashSet<String>();	
	}
	
	/**
	 * Initilize all the OpenNLP models that are necessary for sentence detection and tokenization.
	 * @throws InvalidFormatException
	 * @throws FileNotFoundException
	 */
	public void initializeModels() throws InvalidFormatException, FileNotFoundException {
		try {
			SentenceModel smodel;
			smodel = new SentenceModel(new FileInputStream("./externallibs/OpenNLP_Models/en-sent.bin"));
			sentenceDetector = new SentenceDetectorME(smodel);
			TokenizerModel tmodel = new TokenizerModel(new FileInputStream("./externallibs/OpenNLP_Models/en-token.bin"));
			tokenizer = new TokenizerME(tmodel);
			initializedFlag = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	/**
	 * Process document in either in txt or xml format. 
	 * For txt files, each file is saved as one line (divide files by '\n').
	 * For xml files, each artifact element is saved as one line.
	 * @param filePath
	 */
	protected void processFileWithNlp(String filePath)
	{
		List<String> allRawParagraphs = FileImporter.readTextFile(filePath);
	    for(String eachParagraph : allRawParagraphs) {
	    	String sentences[] = sentenceDetector.sentDetect(eachParagraph);
			for(String eachSentence : sentences) {
				sb.append(preprocessStringText(eachSentence, false));
			}
	    	if(filePath.endsWith("xml")) {
    			sb.append("\n");
    		}
	    }
	    sb.append("\n");
	}
	
	/**
	 * Process document in either in txt or xml format.
	 * @param filePath
	 */
	protected List<SentenceTuple> processFileWithNlpParagraphPreserved(String filePath, Map<Integer, String> sentenceMap)
	{
		int newKey = 0;
		if(sentenceMap.keySet().size() >0)
			newKey = Collections.max(sentenceMap.keySet()) + 1;
		List<SentenceTuple> sentenceTuplesToReturn = new ArrayList<SentenceTuple>();
		List<String> allRawParagraphs = FileImporter.readTextFile(filePath);
	    for(String eachParagraph : allRawParagraphs) {
	    	String sentences[] = sentenceDetector.sentDetect(eachParagraph);
	    	int sentenceCount = 0;
	    	for(String eachSentence : sentences) {
	    		String processedSentence = preprocessStringText(eachSentence, false);
	    		// The processed sentence should more than EOS token.
	    		if(processedSentence.equals("</s>"))
	    			continue;
	    		sentenceMap.put(newKey, processedSentence);	    		
	    		if(sentenceCount>2) {		
		    		SentenceTuple tuple = new SentenceTuple();
		    		tuple.setEmbeddingSentenceID(newKey-1);
		    		tuple.setEmbeddingSentence(sentenceMap.get(newKey-1));
		    		tuple.setPreSentenceID(newKey-2);
		    		tuple.setPreSentence(sentenceMap.get(newKey-2));
		    		tuple.setPostSentenceID(newKey);
		    		tuple.setPostSentence(sentenceMap.get(newKey));
		    		int tokenCount_Embedding = sentenceMap.get(newKey-1).length() - sentenceMap.get(newKey-1).replace(" ", "").length();
		    		int tokenCount_Pre = sentenceMap.get(newKey-2).length() - sentenceMap.get(newKey-2).replace(" ", "").length();
		    		int tokenCount_Post = sentenceMap.get(newKey).length() - sentenceMap.get(newKey).replace(" ", "").length();
		    		if(tokenCount_Embedding > 10 && tokenCount_Pre>10 && tokenCount_Post>10 )
		    			sentenceTuplesToReturn.add(tuple);
	    		}
	    		newKey ++; //Increase the key
	    		sentenceCount++;
			}
	    	
	    }
	    return sentenceTuplesToReturn;
	}


	/**
	 * Spell out numbers if flag is turned on, remove all non-alphabetical characters, and change all characters to lower case.
	 * @param	stringText		input text in string
	 * @param	replaceNumbers	flag for spelling out numbers
	 * @return					processed string
	 */
	public String preprocessStringText(String stringText, boolean replaceNumbers) {
		StringBuilder stringToReturn = new StringBuilder();
		String processedString = stringText;
		if(replaceNumbers) {
			processedString = processedString.replaceAll("0", " zero ");
			processedString = processedString.replaceAll("1", " one ");
			processedString = processedString.replaceAll("2", " two ");
			processedString = processedString.replaceAll("3", " three ");
			processedString = processedString.replaceAll("4", " four ");
			processedString = processedString.replaceAll("5", " five ");
			processedString = processedString.replaceAll("6", " six ");
			processedString = processedString.replaceAll("7", " seven ");
			processedString = processedString.replaceAll("8", " eight ");
			processedString = processedString.replaceAll("9", " nine ");
		}
		// remove chars that are not alphabetic, numeric nor hyphen.
		processedString = processedString.replaceAll("[^A-Za-z0-9-]", " ");
		// replace redundant hyphen
		processedString = processedString.replaceAll("-+", "-");


		String tokens[] = tokenizer.tokenize(processedString);		
		for(String token : tokens) {
			// Remove tokens that don't contain any number nor alphabetic characters
			if(token.matches(".*[A-Za-z0-9]+.*")) {
				// remove hyphen when it's not in the middle of words
				if (token.startsWith("-"))
					token = token.substring(1, token.length());
				else if (token.endsWith("-"))
					token = token.substring(0, token.length()-1);

				stringToReturn.append(token.toLowerCase());
				stringToReturn.append(" "); 
				vocab.add(token.toLowerCase());
			}
		}
		
		// Append the End of Sentence (EOS) token.
		stringToReturn.append("</s>");
		return stringToReturn.toString();
	}

	/**
	 * @param filePath
	 */
	protected void processFile(String filePath)
	{
		List<String> allRawParagraphs = FileImporter.readTextFile(filePath);
	    for(String eachParagraph : allRawParagraphs) {
	    	String sentences[] = sentenceDetector.sentDetect(eachParagraph);
	    	for(String eachSentence : sentences) {	
				sb.append(eachSentence.toLowerCase());
				sb.append(" ");
	    	}
	    	if(filePath.endsWith("xml")) {
    			sb.append("\n");
    		}
	    }
	    sb.append("\n");
	}
	
	
	/**
	 * Process all files in the dirPath, merge the result into a single file and save it to outputPath
	 * @param dirPath
	 * @param outputPath
	 */
	public void mergeFiles(String dirPath, String outputPath) {
		sb.setLength(0);
		File folder = new File(dirPath);
		Collection<File> fileCollection = FileUtils.listFiles(folder, new String[] {"txt", "xml"}, true);
		int count=0;
		System.out.println(fileCollection.size() + " files to be processed.");
		Set<String> fileName = new HashSet<String>();
		for (File file:fileCollection) {
			if(fileName.contains(file.getName())) {
				continue;
			}
			fileName.add(file.getName());
			processFileWithNlp(file.getAbsolutePath()); 
		    count++;
		    System.out.println("File #" + count + ":" + file.getName() + " processed.");
		}
		writeProcessedText(outputPath);	
	}
	
	/**
	 * Process all files in the dirPath, merge the result into a single file and save it to outputPath
	 * @param dirPath
	 * @param outputPath
	 */
	public void createSikpThoughData(String dirPath, String outputSentenceFile, String outputTupleFile) {
		sb.setLength(0);
		vocab.clear();
		File folder = new File(dirPath);
		Collection<File> fileCollection = FileUtils.listFiles(folder, new String[] {"txt", "xml"}, true);
		int count=0;
		System.out.println(fileCollection.size() + " files to be processed.");
		Set<String> fileName = new HashSet<String>();
		Map<Integer, String> sentenceMap = new HashMap<Integer, String> ();
		List<SentenceTuple> sentenceTuples = new ArrayList<SentenceTuple>();
		for (File file:fileCollection) {
			if(fileName.contains(file.getName())) {
				continue;
			}
			fileName.add(file.getName());
			sentenceTuples.addAll(processFileWithNlpParagraphPreserved(file.getAbsolutePath(), sentenceMap)); 
		    count++;
		    System.out.println("File #" + count + ":" + file.getName() + " processed.");
		}
		wirteSkipThoughtSentences(sentenceMap, outputSentenceFile);
		wirteSkipThoughtSentenceTuples(sentenceMap, sentenceTuples, outputTupleFile);
	}
	
	/**
	 * Save the SentenceTuples into a csv file. Each row is a sentence tuple containing the ID of the embedding target sentence,
	 * its preceding sentence and following sentence. 
	 * @param sentenceTuples
	 * @param outputTupleFile
	 */
	private void wirteSkipThoughtSentenceTuples(Map<Integer, String> sentenceMap, List<SentenceTuple> sentenceTuples, String outputTupleFile) {
		PrintWriter writer;
		PrintWriter writer_embed;
		PrintWriter writer_pre;
		PrintWriter writer_post;
		PrintWriter writer_sentence_tuples;

		try {
			writer = new PrintWriter(outputTupleFile);
			writer_embed = new PrintWriter("embedding_sentence_id.txt");
			writer_pre = new PrintWriter("pre_sentence_id.txt");
			writer_post = new PrintWriter("post_sentence_id.txt");
			writer_sentence_tuples = new PrintWriter("sentence_tuples.csv");

			writer.print("EmbeddingSentence\tPreSentence\tPostSentence\n");
			for(SentenceTuple eachTuple:sentenceTuples) {
				writer.print(eachTuple.getEmbeddingSentenceID());
				writer.print("\t");
				writer.print(eachTuple.getPreSentenceID());
				writer.print("\t");
				writer.print(eachTuple.getPostSentenceID());
				writer.print("\n");
				
				// Write the id for each type pf sentence to separate file for torch input.
				writer_embed.println(eachTuple.getEmbeddingSentenceID());
				writer_pre.println(eachTuple.getPreSentenceID());
				writer_post.println(eachTuple.getPostSentenceID());
				
				writer_sentence_tuples.print(eachTuple.getEmbeddingSentence());
				writer_sentence_tuples.print("\t");
				writer_sentence_tuples.print(eachTuple.getPreSentence());
				writer_sentence_tuples.print("\t");
				writer_sentence_tuples.print(eachTuple.getPostSentence());
				writer_sentence_tuples.print("\n");
			}
			writer.close();
			writer_embed.close();
			writer_pre.close();
			writer_post.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
		
	}
	
	/**
	 * Save the sentence into a csv file. Each row is a sentence with its ID
	 * @param sentenceMap
	 * @param outputSentenceFile
	 */
	private void wirteSkipThoughtSentences(Map<Integer, String> sentenceMap, String outputSentenceFile) {
		PrintWriter writer;
		PrintWriter writer_ID;
		PrintWriter writer_Sentence;
		try {
			writer = new PrintWriter(outputSentenceFile);
			writer_ID = new PrintWriter("skipthough_sentence_id.txt");
			writer_Sentence = new PrintWriter("skipthough_sentence_content.txt");
			writer.print("ID\tSentence\n");
			for(Integer key:sentenceMap.keySet()) {
				writer.print(key);
				writer.print("\t");
				writer.print(sentenceMap.get(key));
				writer.print("\n");
				// Write the id and content to separate file for torch input.
				writer_ID.println(key);
				writer_Sentence.println(sentenceMap.get(key));
			}
			writer.close();	
			writer_ID.close();
			writer_Sentence.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
		
	}

	/**
	 * Write processed result to filePath
	 * @param filePath
	 */
	private void writeProcessedText(String filePath) {
		if(sb.length()==0) {
			System.out.print("No text to write.");
			return;
		}
		PrintWriter writer;
		try {
			writer = new PrintWriter(filePath);
			writer.print(sb.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Write the sorted vocabulary of the corpus to the file
	 * @param vocabFile
	 */
	private void writeVocab(String vocabFile) {
		if(vocab.size() == 0) {
			System.out.print("Empty vocabulary!");
			return;
		}
		List<String> vocabSorted = new ArrayList<String>(vocab);
		Collections.sort(vocabSorted);
		PrintWriter writer;
		try {
			writer = new PrintWriter(vocabFile);
			writer.println("</s>"); // Write the EOS token.
			for(String token:vocabSorted) {
				writer.println(token);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
		
	}
	public static void main(String args[]) throws IOException
	{
		DocProcessor docProcessor = new DocProcessor();
		docProcessor.initializeModels();
//		System.out.println(docProcessor.preprocessStringText("ok-now yes - _i undersrand 00 ))) _ -.", false));
		String dirPath = "/Users/Jinguo/GitHub/dataExtrator/HealthITDomain";//"/Users/Jinguo/Dropbox/Git/OntologyMiningFromTracing/data/DomainFiles/PTC";//SiemensDocumentsCloud";
		String outputPath = "/Users/Jinguo/GitHub/HealthIT_doc_NumberSymbol.txt";//"/Users/Jinguo/Desktop/PTC_artifact_doc_noSymbol_2.txt";
		String outputTuple = "/Users/Jinguo/GitHub/HealthI_Tuple.txt";//"/Users/Jinguo/Desktop/PTC_artifact_doc_noSymbol_2.txt";
		String vocabFile = "/Users/Jinguo/GitHub/HealthIT_Vocab.txt";
//		docProcessor.mergeFiles(dirPath, outputPath);
		docProcessor.createSikpThoughData(dirPath, outputPath, outputTuple);
		docProcessor.writeVocab(vocabFile);
	}

	

		    	
}
