package wespad;

import java.util.ArrayList;

class ESequence {

	public int TweetCount;
	public float Value;
	public String OrderedKey;
	public boolean IsPositive;
	EPair<Double, Double> Existing = new EPair<Double, Double>(0d, 0d);
	EPair<Double, Double> Nonexisting = new EPair<Double, Double>(0d, 0d);
	
	public ArrayList<EEmbedWordDensity> Words = new ArrayList<>();
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(
				String.format("%-21s",  "[T" + TweetCount + ", V" + Value + "]") 
				+ String.format("%-20s",  "[EP"+ Existing.Key +", EN" + Existing.Value + "]")
//				+ String.format("%-22s",  "[NP" + Nonexisting.Key + ", NN" + Nonexisting.Value + "] ")
		);
		if (IsPositive) {
			result.append("True\t\t");
		}
		else {
			result.append("False\t\t");
		}
		for (int cind = 0; cind < Words.size(); ++cind) {
			result.append(Words.get(cind).WordTerm + ", ");
		}
		return result.toString();
	}
	
	public String GetKey() {
		StringBuilder sb = new StringBuilder();
		for (int wind = 0; wind < Words.size(); ++wind) {
			sb.append(Words.get(wind).WordTerm + "-");
		}
		return sb.toString();
	}
	
}

