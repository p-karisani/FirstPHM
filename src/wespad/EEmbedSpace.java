package wespad;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;


public class EEmbedSpace {

	public Hashtable<String, EEmbedWord> table = new Hashtable<>();
	
	private void loadFile(String embeddingFilePath, boolean forceLowercase) 
			throws Exception {
		Scanner scan = new Scanner(new File(embeddingFilePath), 
				StandardCharsets.UTF_8.name());
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			if (forceLowercase) {
				line = line.toLowerCase();
			}
			String[] tokens = line.split(" ");
			if (tokens.length > 10) {
				EEmbedWord em = new EEmbedWord(line);
				if (!table.containsKey(em.Term)) {
					table.put(em.Term, em);
				}
				else {
//					System.out.println(em.Term);
				}
			}
		}
		scan.close();
	}

	public EEmbedSpace(String embeddingFilePath, boolean addForceLowercase) throws Exception {
		loadFile(embeddingFilePath, false);
		if (addForceLowercase) {
			loadFile(embeddingFilePath, true);
		}
	}
	
	public ArrayList<EEmbedWord> GetWords(ETweet tw, boolean unique, 
			boolean removeStopwords, boolean createForAbsent, String... posTags) {
		HashSet<String> POSs = new HashSet<>(Arrays.asList(posTags));
		HashSet<String> uniqueTbl = new HashSet<>();
		ArrayList<EEmbedWord> result = new ArrayList<>();
		for (int tInd = 0; tInd < tw.ETokens.size(); ++tInd) {
			EToken tok = tw.ETokens.get(tInd);
			EEmbedWord ew = GetWord(tok.Text);
			if (ew == null && createForAbsent) {
				ew = GetWordAndCreateForAbsent(tok);
			}
			if (ew != null) {
				if (unique) {
					if (!uniqueTbl.contains(tok.Text)) {
						uniqueTbl.add(tok.Text);
						if (!removeStopwords || !ELib.isStopword(tok.Text)) {
							if (POSs.size() == 0 || POSs.contains(tok.POS)) {
								result.add(ew);
							}						
						}
					}
				}
				else {
					if (!removeStopwords || !ELib.isStopword(tok.Text)) {
						if (POSs.size() == 0 || POSs.contains(tok.POS)) {
							result.add(ew);
						}						
					}
				}
			}
		}
		return result;
	}
	
	public EEmbedWord GetWordAndCreateForAbsent(EToken token) {
		Random rnd = new Random(0);
		EEmbedWord ew = GetWord(token.Text);
		if (ew == null && !token.POS.equals("U")) {
			ew = new EEmbedWord(token.Text, GetWord("test").Values.length, 0);
			rnd.setSeed(getStringCharKey(token.Text));
			for (int vind = 0; vind < ew.Values.length; ++vind) {
				float vv = ((float)rnd.nextInt(1000000) - 500000) / 500000;
				ew.Values[vind] = vv;
			}
		}
		return ew;
	}	
	
	public boolean ContainsWord(String word) {
		return table.containsKey(word);
	}
	
	public EEmbedWord GetWord(String word) {
		return table.get(word);
	}
	
	public ArrayList<EEmbedWord> GetWords(ArrayList<ETweet> list, 
			boolean unique, boolean removeStopwords, boolean createForAbsent) {
		HashSet<String> uniqueTbl = new HashSet<>();
		ArrayList<EEmbedWord> result = new ArrayList<>();
		for (int ind = 0; ind < list.size(); ++ind) {
			ArrayList<EEmbedWord> ws = GetWords(list.get(ind), unique, removeStopwords, 
					createForAbsent);
			for (int wind = 0; wind < ws.size(); ++wind) {
				if (!unique || !uniqueTbl.contains(ws.get(wind).Term)) {
					result.add(ws.get(wind));
					uniqueTbl.add(ws.get(wind).Term);
				}
			}
		}
		return result;
	}
	
	private int getStringCharKey(String term) {
		int result = 0;
		for (int cind = 0; cind < term.length(); ++cind) {
			result += term.charAt(cind);
		}
		return result;
	}
	
}
