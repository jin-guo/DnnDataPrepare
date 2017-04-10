import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class WordVectorUtil {
	static void generateVocabFile(String vectorFile, String vocabFile) {	
		try {
			PrintWriter writer = new PrintWriter(vocabFile);
			BufferedReader br = new BufferedReader(new FileReader(vectorFile));
			
			String line;
			while ((line = br.readLine()) != null) {
				String[] strings = line.split(" ");
				if(strings.length>2)
					writer.println(strings[0]);
			}
			writer.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) throws IOException
	{
		String vectorFile = "/Users/Jinguo/Documents/ICSE_tools/treelstm-master/data/glove/ptc_word2vec_vector_50d.txt";
		String vocabFile = "/Users/Jinguo/Documents/ICSE_tools/treelstm-master/data/glove/ptc_word2vec_vocab_50d.txt";
		WordVectorUtil.generateVocabFile(vectorFile, vocabFile);
		return;
	}
}
