package wespad;

import java.util.ArrayList;

class EEmbedWordDensity {
	
	public EEmbedWord Word;
	public String WordTerm;
	public int Count;
	public float IG;
	public ArrayList<ETweet> InvertedList = new ArrayList<>();
	public EPair<Double, Double> Existing = new EPair<Double, Double>(0d, 0d);
	public EPair<Double, Double> Nonexisting = new EPair<Double, Double>(0d, 0d);
	
	public EEmbedWordDensity(EEmbedWord word, String wordTerm) {
		Word = word;
		WordTerm = wordTerm;
	}
	
	@Override
	public String toString() {
//		return WordTerm + ", " + Count + ", " + IG + ", " + IGExt;
		return WordTerm + ", " + "[T" + Count + ", V" + IG + "]" +
		"[EP"+ Existing.Key +", EN" + Existing.Value + "]" +
		"[NP" + Nonexisting.Key + ", NN" + Nonexisting.Value + "] ";
	}
	
}
