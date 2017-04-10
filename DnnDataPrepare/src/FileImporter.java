
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import Data.Artifact;
import Data.Link;

public class FileImporter {
	static SentenceDetectorME sentenceDetector;
	static Tokenizer tokenizer;
	static Set<String> vocabGlobal = new HashSet<String>();
	
	static public void initializeModels() throws InvalidFormatException, FileNotFoundException {

		try {
			SentenceModel smodel;
			smodel = new SentenceModel(new FileInputStream("./externallibs/OpenNLP_Models/en-sent.bin"));
			sentenceDetector = new SentenceDetectorME(smodel);
			TokenizerModel tmodel = new TokenizerModel(new FileInputStream("./externallibs/OpenNLP_Models/en-token.bin"));
			tokenizer = new TokenizerME(tmodel);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	public static List<String> readTextFile(String filePath) {
		if(filePath.endsWith("txt"))
			return readTxtFile(filePath);
		else if(filePath.endsWith("xml"))
			return readXmlFile(filePath);
		return new ArrayList<String>();
	}
	
	private static List<String> readXmlFile(String filePath) {
		List<String> allContent= new ArrayList<String>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
			InputStream inputStream= new FileInputStream(filePath);
			Reader reader = new InputStreamReader(inputStream); 
			InputSource is = new InputSource(reader);
			
			Document doc = db.parse(is);
			Element docEle = doc.getDocumentElement();
	
			NodeList artifactList = docEle.getElementsByTagName("artifact");
	
			// Print total artifact elements in document
			System.out.println("# of artifacts: " + artifactList.getLength());
			if (artifactList != null && artifactList.getLength() > 0) {
				for (int i = 0; i < artifactList.getLength(); i++) {
	
					Node node = artifactList.item(i);
	
					if (node.getNodeType() == Node.ELEMENT_NODE) {
	
						Element e = (Element) node;
						NodeList nodeList = e.getElementsByTagName("id");
						if(nodeList.getLength() == 0)
							nodeList = e.getElementsByTagName("art_id");
	
						nodeList = e.getElementsByTagName("content");
						if(nodeList.getLength() == 0)
							nodeList = e.getElementsByTagName("art_title");
						// Continue if the content is empty.
						if(nodeList.item(0) == null || nodeList.item(0).getChildNodes() == null ||
								nodeList.item(0).getChildNodes().item(0) == null) 
							continue;
						String content = nodeList.item(0).getChildNodes().item(0).getNodeValue().trim();
						content = content.replace("\t", " ").replace("\n", " ").replace("\r", " ");	
//						StringBuilder sentenceWithEosToken = new StringBuilder();
//						if(sentenceDetector!=null) {
//							String sentences[] = sentenceDetector.sentDetect(content);
//					    	for(String eachSentence : sentences) {
//					    		sentenceWithEosToken.append(eachSentence);
//					    		sentenceWithEosToken.append(" EOS ");
//					    	}
//						}
//						allContent.add(sentenceWithEosToken.toString());
						allContent.add(content);
						
					}
				}
			}
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return allContent;
	}
	
	public static Map<String, Artifact> readArtifact(String filePath) {
		Map<String, Artifact> artifacts= new HashMap<String, Artifact>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
			InputStream inputStream= new FileInputStream(filePath);
			Reader reader = new InputStreamReader(inputStream); 
			InputSource is = new InputSource(reader);
			
			Document doc = db.parse(is);
			Element docEle = doc.getDocumentElement();
	
			NodeList artifactList = docEle.getElementsByTagName("artifact");
	
			// Print total student elements in document
			System.out.println("# of artifacts: " + artifactList.getLength());
			if (artifactList != null && artifactList.getLength() > 0) {
				for (int i = 0; i < artifactList.getLength(); i++) {
	
					Node node = artifactList.item(i);
	
					if (node.getNodeType() == Node.ELEMENT_NODE) {
	
						Element e = (Element) node;
						NodeList nodeList = e.getElementsByTagName("id");
						if(nodeList.getLength() == 0)
							nodeList = e.getElementsByTagName("art_id");
						String id = nodeList.item(0).getChildNodes().item(0).getNodeValue().trim();
	
						nodeList = e.getElementsByTagName("content");
						if(nodeList.getLength() == 0)
							nodeList = e.getElementsByTagName("art_title");
						// Continue if the content is empty.
						if(nodeList.item(0) == null || nodeList.item(0).getChildNodes() == null ||
								nodeList.item(0).getChildNodes().item(0) == null) 
							continue;
						String content = nodeList.item(0).getChildNodes().item(0).getNodeValue().trim();
						content = content.replace("\t", " ").replace("\n", " ").replace("\r", " ");
						StringBuilder sentenceWithEosToken = new StringBuilder();
						if(sentenceDetector!=null) {
							String sentences[] = sentenceDetector.sentDetect(content);
					    	for(String eachSentence : sentences) {
					    		sentenceWithEosToken.append(eachSentence);
					    		sentenceWithEosToken.append(" </s> ");
					    	}
						}
	
						Artifact arf = new Artifact();
						arf.setId(id);
						arf.setContent(sentenceWithEosToken.toString());
						artifacts.put(id, arf);
					}
				}
			}
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return artifacts;
	}
	
	public static Map<String, Link> readAnswerFile(String filePath, String linkType) {
		Map<String, Link> links = new HashMap<String, Link>();
		try {
			File file = new File(filePath);

			if(file.exists()) {
				if(file.isFile()) { 
					if(filePath.endsWith("txt"))
						readAnswerSetFromSingleTxtFile(filePath, links);
					else if(filePath.endsWith("xml"))
						readAnswerSetFromSingleXmlFile(file, links);
				} else if (file.isDirectory()) {
					File[] fileArray = file.listFiles();
					for(File eachFile:fileArray) {
						if(eachFile.getAbsolutePath().endsWith("txt"))
							readAnswerSetFromSingleTxtFile(eachFile.getAbsolutePath(), links);
						else if(eachFile.getAbsolutePath().endsWith("xml"))
							readAnswerSetFromSingleXmlFile(eachFile, links);
					}
				}
					
			}
			for(Link eachLink:links.values()) {
				eachLink.setLinkType("TRACE");
			}
		} catch (Exception e) {
				System.out.println(e);
		}
		
		return links;	
	}
	
	private static void readAnswerSetFromSingleXmlFile(File file, Map<String, Link> links)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		Element docEle = doc.getDocumentElement();

		NodeList lisktList = docEle.getElementsByTagName("link");

		// Print total artifact elements in document
		System.out.println("# of links: " + lisktList.getLength());

		if (lisktList != null && lisktList.getLength() > 0) {
			for (int i = 0; i < lisktList.getLength(); i++) {

				Node node = lisktList.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) node;
					NodeList nodeList = e.getElementsByTagName("source_artifact_id");
					String sourceId = nodeList.item(0).getChildNodes().item(0).getNodeValue();
					nodeList = e.getElementsByTagName("target_artifact_id");
					String targetId = nodeList.item(0).getChildNodes().item(0).getNodeValue();

					Link link = new Link(sourceId, targetId);
					link.setValid(true);
					String key = sourceId + "::" + targetId;
					links.put(key, link);
				}
			}
		}
		System.out.println("# of links: " + links.size() + " read from the file " + file.getName());

	}
	

	
	private static void readAnswerSetFromSingleTxtFile(String fileName, Map<String, Link> links)
			throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = "";


		while ((line = br.readLine()) != null) {
			String[] strings = line.split(",");
			if(strings.length != 2)
				continue;

			String sourceId = strings[0].trim();
			String targetId = strings[1].trim();
			Link link = new Link(sourceId, targetId);
			link.setValid(true);
			String key = sourceId + "::" + targetId;
			links.put(key, link);
		}
		System.out.println("# of links: " + links.size() + " read from the file " + fileName);
		br.close();
	}
	
	/**
	 * 
	 * @param 	filePath Input file path
	 * @return  
	 */
	private static List<String> readTxtFile(String filePath) {

		List<String> allRawParagraphs = new ArrayList<String>();
		
		String line = null;
		
		try {
			 File file =  new File(filePath);
			
			// Read file with encoding of UTF-8.
			BufferedReader bufferedReader = 
			        new BufferedReader(new InputStreamReader(
		                      new FileInputStream(file), "UTF8"));
			
		
		    // Split the text into smaller paragraphs
		    StringBuilder currentParagraphRaw = new StringBuilder();
		    while((line = bufferedReader.readLine()) != null) {
				boolean containsAlphabet = line.matches(".*[a-zA-Z]+.*");
				if(!containsAlphabet) {
					if(currentParagraphRaw.length()>0) {
						allRawParagraphs.add(Util.removeRedundantSpace(currentParagraphRaw.toString()));
					 	currentParagraphRaw.setLength(0);
					}
					 continue;
				}
				
				// Remove the reference in the format of "[##]" from the text.
				line = line.replaceAll("\\[[0-9,]+\\]", "");

				// Remove the urls in the text
				line = line.replaceAll("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "");
				if(line.length()==0)
					continue;
				
				// Replace underscore with space.
				line = line.replaceAll("_|/", " ");
				
				// Remove the lines contains less than 3 letters.
				String letterOnly = line.replaceAll("[^a-zA-Z]", "");
				if(letterOnly.length()<=2)
					continue;
				
				
				// Remove the beginning of the line if it's not alphabetic chars and split the paragraph.
				Matcher matchList = Pattern.compile("^[^a-zA-Z]+").matcher(line);
				if (matchList.find()) {
					String toRemove = matchList.group();
					if(currentParagraphRaw.length()>0) {
						allRawParagraphs.add(Util.removeRedundantSpace(currentParagraphRaw.toString()));
					    currentParagraphRaw.setLength(0);
					}
					currentParagraphRaw.append(line.substring(toRemove.length()-1, line.length()));
				} else {
					currentParagraphRaw.append(line);
				}
				
				line = Util.preprocessSentence(line);
				// If the current line ends with period, split the paragraph. (space might appear before or after the period symbol.)
				matchList = Pattern.compile(" *\\. *$").matcher(line);
				if(matchList.find()) {
				    allRawParagraphs.add(Util.removeRedundantSpace(currentParagraphRaw.toString()));
				    currentParagraphRaw.setLength(0);
					
				} else {    
				    currentParagraphRaw.append(" ");
				}       
		    }
		    bufferedReader.close(); 
		}
		catch(FileNotFoundException ex) {
		    System.out.println(
		        "Unable to open file '" + 
		        filePath + "'");                
		}
		catch(IOException ex) {
		    System.out.println(
		        "Error reading file '" 
		        + filePath + "'");                  
		    // Or we could just do this: 
		    // ex.printStackTrace();
		}
		
		return allRawParagraphs;
	}
	
	public static void main(String args[]) {
		String fileName = "/Users/Jinguo/Dropbox/Git/OntologyMiningFromTracing/data/DomainFiles/MIP_Data/Artifact/Open-PCA-Pump-Requirements.txt";
		List<String> output= FileImporter.readTextFile(fileName);
		int count=0;
		for(String eachParagraph:output) {
			System.out.println(count +": " +eachParagraph);
			count++;
		}
//		System.out.println(Util.removeRedundantSpace(" Testing       test .  "));
		return;
	}

	public static Map<String, Artifact> readArtifact(String filePath, boolean processFlag) {
		
		vocabGlobal.add("</s>");
		Map<String, Artifact> artifacts= new HashMap<String, Artifact>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		
			InputStream inputStream= new FileInputStream(filePath);
			Reader reader = new InputStreamReader(inputStream); 
			InputSource is = new InputSource(reader);
			
			Document doc = db.parse(is);
			Element docEle = doc.getDocumentElement();
	
			NodeList artifactList = docEle.getElementsByTagName("artifact");
	
			// Print total student elements in document
			System.out.println("# of artifacts: " + artifactList.getLength());
			if (artifactList != null && artifactList.getLength() > 0) {
				for (int i = 0; i < artifactList.getLength(); i++) {
	
					Node node = artifactList.item(i);
	
					if (node.getNodeType() == Node.ELEMENT_NODE) {
	
						Element e = (Element) node;
						NodeList nodeList = e.getElementsByTagName("id");
						if(nodeList.getLength() == 0)
							nodeList = e.getElementsByTagName("art_id");
						String id = nodeList.item(0).getChildNodes().item(0).getNodeValue().trim();
	
						nodeList = e.getElementsByTagName("content");
						if(nodeList.getLength() == 0)
							nodeList = e.getElementsByTagName("art_title");
						// Continue if the content is empty.
						if(nodeList.item(0) == null || nodeList.item(0).getChildNodes() == null ||
								nodeList.item(0).getChildNodes().item(0) == null) 
							continue;
						String content = nodeList.item(0).getChildNodes().item(0).getNodeValue().trim();
						content = content.replace("\t", " ").replace("\n", " ").replace("\r", " ");
						if(processFlag) {
							StringBuilder sb = new StringBuilder();
							String sentences[] = sentenceDetector.sentDetect(content);
					    	for(String eachSentence : sentences) {
				    				// spell out numbers
					    			String eachSentencePro = eachSentence.replaceAll("0", " zero ");
					    			eachSentencePro = eachSentencePro.replaceAll("1", " one ");
					    			eachSentencePro = eachSentencePro.replaceAll("2", " two ");
					    			eachSentencePro = eachSentencePro.replaceAll("3", " three ");
					    			eachSentencePro = eachSentencePro.replaceAll("4", " four ");
					    			eachSentencePro = eachSentencePro.replaceAll("5", " five ");
					    			eachSentencePro = eachSentencePro.replaceAll("6", " six ");
					    			eachSentencePro = eachSentencePro.replaceAll("7", " seven ");
					    			eachSentencePro = eachSentencePro.replaceAll("8", " eight ");
					    			eachSentencePro = eachSentencePro.replaceAll("9", " nine ");
					    			eachSentencePro = eachSentencePro.replaceAll("[^A-Za-z]", " ");
									String tokens[] = tokenizer.tokenize(eachSentencePro);		    		
						    		for(String token : tokens) {
//						    			String tokenPro = token.replaceAll("[^A-Za-z]", " ");
//						    			String tokenPro = token.replaceAll("[^A-Za-z_-]", " ");
						    			if(token.length()>1) {
						    				sb.append(token.toLowerCase());
						    				sb.append(" "); 
						    				vocabGlobal.add(token.toLowerCase());
						    			}
						    		}
						    		// Append the end of sentence token
						    		sb.append("</s> ");
					    	}
					    	content = sb.toString();
					    } else {
					    	StringBuilder sb = new StringBuilder();
							String sentences[] = sentenceDetector.sentDetect(content);
					    	for(String eachSentence : sentences) {
				    				// spell out numbers
					    			String eachSentencePro = eachSentence.replaceAll("[^A-Za-z0-9_-]", " ");
					    			eachSentencePro = eachSentencePro.toLowerCase();
					    			eachSentencePro = eachSentencePro.replaceAll("(?<![a-z])\\_", " ");
					    			eachSentencePro = eachSentencePro.replaceAll("\\_(?![a-z])", " ");
						    		// Append the end of sentence token
					    			sb.append(eachSentencePro);
						    		sb.append(" </s> ");
						    		String[] tokens = eachSentencePro.split(" ");
						    		vocabGlobal.addAll(new HashSet<String> (Arrays.asList(tokens)));
					    	}
					    	content = sb.toString();
					    }
						Artifact arf = new Artifact();
						arf.setId(id);
						arf.setContent(content);
						artifacts.put(id, arf);
					}
				}
			}
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return artifacts;
	}
	
	public static Set<String> getGlobalVocab() {
		return vocabGlobal;
	}
	
//	 public static void main(String args[]) {
//
//	        String fileName = "best-practices-7";
//	        File file = new File("/Users/Jinguo/Documents/workspace/OntologyMiningFromTracing/data/DomainFiles/EHR_Data/" + fileName +".pdf");
//	        try {
//	        	File processedTextToWrite = new File("/Users/Jinguo/Documents/workspace/OntologyMiningFromTracing/data/DomainFiles/EHR_Data/ProcessedText/ " + fileName + ".txt");
//
//				if (!processedTextToWrite.exists()) {
//					processedTextToWrite.createNewFile();
//				}
//				
//				FileWriter fw = new FileWriter(processedTextToWrite.getAbsoluteFile());
//				BufferedWriter bw = new BufferedWriter(fw);
//				
//	        	File origTextToWrite = new File("/Users/Jinguo/Documents/workspace/OntologyMiningFromTracing/data/DomainFiles/EHR_Data/Text/" + fileName + ".txt");
//
//				if (!origTextToWrite.exists()) {
//					origTextToWrite.createNewFile();
//				}
//
//				FileWriter origfw = new FileWriter(origTextToWrite.getAbsoluteFile());
//				BufferedWriter origbw = new BufferedWriter(origfw);
//				
//				
//	        	PDDocument doc = PDDocument.load(file);
//                List allPages = doc.getDocumentCatalog().getAllPages();
//                StringBuilder forTokenize = new StringBuilder();
//                for( int i=1; i<=allPages.size(); i++ )
//
//                {
//                	PDFTextStripper stripper = new PDFTextStripper();
//                	stripper.setStartPage(i);
//                	stripper.setEndPage(i);
//
//        			String text = stripper.getText(doc);
//        			if(i%2 == 0)
//        				text = text.replaceFirst("\\d+ the CoMMonwealth fund", "");
//        			else
//        				text = text.replaceFirst("uSing eleCtroniC health reCordS to iMprove Quality and effiCienCy: the experienCeS of leading hoSpitalS \\d+", "");
//        			//System.out.println("Page " + i + "\n" + cleanText(text) + "\n**********\n");
//        			text = cleanText(text);
//        			origbw.write(text);
//        			forTokenize.append(text);
//                }
//                origbw.close();
//                doc.close();               
//                MaxentTagger tagger = new MaxentTagger("/Users/Jinguo/Documents/java_libraries/stanford-postagger-2015-04-20/models/english-left3words-distsim.tagger");
//                List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(forTokenize.toString()));
//                for (List<HasWord> sentence : sentences) {
//                	StringBuilder eachSentence = new StringBuilder();
//                	for(HasWord eachWord:sentence) {
//
//                		eachSentence.append(eachWord.word());
//                		eachSentence.append(" ");
//                	}
//                	String sentenceString = eachSentence.toString();
//                	sentenceString = sentenceString.replace("s ' ", "s'");
//                	sentenceString = sentenceString.replace(" \'s", "\'s");
//                	sentenceString = sentenceString.replace("-LRB- ", "(");
//                	sentenceString = sentenceString.replace(" -RRB-", ")");
//                	sentenceString = sentenceString.replace("`` ", "\"");
//                	sentenceString = sentenceString.replace(" \'\'", "\"");
//            		sentenceString = sentenceString.replaceAll("\\s([,.;:!])", "$1");
//            		sentenceString = sentenceString.replaceAll(" -LSB- [,\\-0-9]+ -RSB-", "");
//
//                	 
//                	bw.write(sentenceString + "\n");
//                
//                }
//                
//                bw.close();
//	        } catch (IOException e) {
//	            // TODO Auto-generated catch block
//	            e.printStackTrace();
//	        } 
//	 }
//	 

}
