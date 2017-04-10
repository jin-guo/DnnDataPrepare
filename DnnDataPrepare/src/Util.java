

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.stemmer.PorterStemmer;


public class Util {
	static PorterStemmer stemmer = new PorterStemmer();

	public static String removeSpecialCharactersInString(String str)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i); 
			if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || 
					(c >= 'a' && c <= 'z') || c == ' ' ||  c == '-') {
				sb.append(c);
			}
		}
		return sb.toString();
	}
  
	public static String removeRedundantSpace(String input) {
		input = input.replaceAll("\\s+", " ");
		input = input.trim();
		input = input.replaceAll(" .$", ".");
		return input;
	}
	  
	public static String processPhraseString(String phraseString) {
		 String cleanPhraseString = Util.removeSpecialCharactersInString(phraseString);

         cleanPhraseString = cleanPhraseString.replaceFirst(
            		 "\\A(a|the|A|The|an|An|any|Any|all|no|most|its|their|each|this|that|these|those|others|relevant|relate)\\s+", "");

         return removeRedundantSpace(cleanPhraseString);
	 }
	  
	 public static String processTermString(String origString) {			
		 // Any word contains more than 25 chars, 
		 // or contains less than two letters is invalid.
		 if(origString.length()>25 ||
				!origString.matches(".*[a-zA-Z]+.*[a-zA-Z]+.*"))
			 return "";
		
		 Matcher matcher = Pattern.compile("[^a-zA-Z]+$").matcher(origString);
		 if (matcher.find()) {
			 String toRemove = matcher.group();
			 origString = origString.substring(0, origString.length() - toRemove.length());
		 }
		 return removeSpecialCharactersInString(origString);
	 }
	 
	 
	 public static String stem(String input) {
		 String stem = stemmer.stem(input);
		 if(stem != null)
			 return stem;
		 else
			 return "";
	 }
	 

	 public static List<String> processWordsOrderInPhrase(List<String> phraseTerms) {
		if(phraseTerms.size()==1) 
			return phraseTerms;
		int ofIndex =  getIndexOfString(phraseTerms, "of");
		int pocessionIndex = getIndexOfString(phraseTerms, "'s");
		
		// If both cases are not found
		if(ofIndex == -1 && pocessionIndex == -1)
			return phraseTerms;
		
	 	List<String> processedPhrase = new ArrayList<String>(phraseTerms);
	 	// Remove "'s"
		if(pocessionIndex >0) {
			processedPhrase.remove(pocessionIndex);
		}
		
		// Remove "of"
		if(ofIndex>0) {
			if(ofIndex >pocessionIndex && pocessionIndex>0)
				ofIndex --;
			List<String> beforeOf = new ArrayList<String>(processedPhrase.subList(0, ofIndex));
			processedPhrase.removeAll(beforeOf);
			processedPhrase.remove(0);
			processedPhrase.addAll(beforeOf);
		}
	 	return processedPhrase;
	 	
	 }
	 
	 // Find the first match of a string in a string list, return its index.
	 // Return -1 if the match is not found.
	 public static int getIndexOfString(List<String> stringList, String stringToCheck) {
		 for(String eachTerm:stringList) {
			 if(eachTerm.equals(stringToCheck))
				 return stringList.indexOf(eachTerm);
		 }
		 return -1;
	 }
	 
	 public static String preprocessSentence(String inputSentence) {

		 // Remove the the parentheses with no alphabetic characters inside.
		 inputSentence = inputSentence.replaceAll("\\([^a-zA-Z]+\\)", "");
			
		 // Remove the the conjunctive symbols to space.
		 inputSentence = inputSentence.replaceAll("[^a-zA-Z0-9 .]{2,}", " ");
			
		 // Replace the control characters to space. 
		 inputSentence = inputSentence.replaceAll("[\u0000-\u001f]", " ");

		 //Remove the reference such as "versluis2"
		 Matcher matchList = Pattern.compile("[a-zA-Z]+([0-9]+)").matcher(inputSentence);
		 while (matchList.find()) {
			 String toRemove = matchList.group(1);
			 inputSentence = inputSentence.replace(toRemove, "");	
		 }
		
		 // Change the slash to "or"
		 matchList = Pattern.compile("( [a-zA-Z]+)/([a-zA-Z]+ )").matcher(inputSentence);
		 while (matchList.find()) {
			 String beforeSlash = matchList.group(1);
			 String afterSlash = matchList.group(2);
			 String replacement = beforeSlash + " or " + afterSlash;
			 if(beforeSlash.equals("and") && afterSlash.equals("or"))
				 replacement = beforeSlash;
			 inputSentence = inputSentence.replace(matchList.group(0), replacement);
		 }
		 return inputSentence;
	 }
	 
	 public static void main(String[] args) {
		 List<String> testPhrase = new ArrayList<String>();
		 testPhrase.add("temperature");
		 testPhrase.add("of");
		 testPhrase.add("the");
		 testPhrase.add("day");		 

		 System.out.println(processWordsOrderInPhrase(testPhrase));

}
//		public static String processPhraseString(String input) {
//			input = input.replaceAll("[*'\"“”]", "");		
//			return input;
//		}	
}
