package wespad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

public class EEmbedWord {

	class Repo {
		public ArrayList<EEmbedWord> CloseWords = new ArrayList<>();
		public double IDF;
	}

	public String Term;
	public float[] Values;
	public Repo repo = new Repo();
	
	public EEmbedWord(String term, int valueCount, float defaultValue) {
		Term = term;
		Values = new float[valueCount];
		for (int vind = 0; vind < Values.length; ++vind) {
			Values[vind] = defaultValue;
		}
	}
	
	public EEmbedWord(String line) {
		String[] tokens = line.split(" ");
		Term = tokens[0];
		Values = new float[tokens.length - 1];
		for (int ind = 1; ind < tokens.length; ++ind) {
			Values[ind - 1] = Float.parseFloat(tokens[ind]);
		}
	}
	
	@Override
	public String toString() {
		return Term;
	}
	
	private static Hashtable<String, Double> cmpTable = new Hashtable<>();
	public static double GetCosineSim(EEmbedWord w1, EEmbedWord w2) {
		String key = getKey(w1, w2);
		if (cmpTable.containsKey(key)) {
			return cmpTable.get(key);
		}
		double prod = 0;
		for (int ind = 0; ind < w1.Values.length; ++ind) {
			prod += w1.Values[ind] * w2.Values[ind];
		}
		double l1 = 0;
		for (int ind = 0; ind < w1.Values.length; ++ind) {
			l1 += w1.Values[ind] * w1.Values[ind];
		}
		double l2 = 0;
		for (int ind = 0; ind < w2.Values.length; ++ind) {
			l2 += w2.Values[ind] * w2.Values[ind];
		}
		double result = prod / (Math.sqrt(l1) * Math.sqrt(l2));
		if (result < 0) {
			result = 0;
		}
//		if (!key.equals(""))
//			cmpTable.put(key, result);
		return result;
	}

	private static Hashtable<String, Float> disTable = new Hashtable<>();
	public static double GetDistance(EEmbedWord w1, EEmbedWord w2) {
		String key = getKey(w1, w2);
		if (disTable.containsKey(key)) {
			return disTable.get(key);
		}
		double result = 0;
		for (int ind = 0; ind < w1.Values.length; ++ind) {
			double dir = w1.Values[ind] - w2.Values[ind];
			result += dir * dir;
		}
		result = Math.sqrt(result);
//		if (!key.equals(""))
//			disTable.put(key, (float)result);
		return result;
	}

	private static String getKey(EEmbedWord w1, EEmbedWord w2) {
		String result = "";
		if (w1.Term.equals("") || w2.Term.equals("")) {
			return result;
		}
		if (w1.Term.compareTo(w2.Term) >= 0) {
			result = w1.Term + "$" + w2.Term + "$" + w1.Values.length;
		}
		else {
			result = w2.Term + "$" + w1.Term + "$" + w1.Values.length;
		}
		return result;
	}
	
	public static EEmbedWord GetSum(ArrayList<EEmbedWord> words) {
		EEmbedWord result = new EEmbedWord("", words.get(0).Values.length, 0);
		for (int wInd = 0; wInd < words.size(); ++wInd) {
			for (int eInd = 0; eInd < result.Values.length; ++eInd) {
				result.Values[eInd] += words.get(wInd).Values[eInd];
			}
		}
		return result;
	}
	
	public static EEmbedWord GetSubtract(EEmbedWord lw, EEmbedWord rw) {
		EEmbedWord result = new EEmbedWord("", lw.Values.length, 0);
		for (int eInd = 0; eInd < result.Values.length; ++eInd) {
			result.Values[eInd] = lw.Values[eInd] - rw.Values[eInd];
		}
		return result;
	}
	
	public static EEmbedWord GetAverage(ArrayList<EEmbedWord> words) {
		EEmbedWord result = GetSum(words);
		for (int eInd = 0; eInd < result.Values.length; ++eInd) {
			result.Values[eInd] /= words.size();
		}
		return result;
	}
	
	public static EEmbedWord GetWeightedAverage(ArrayList<EEmbedWord> words,
			ArrayList<Float> weights) {
		ArrayList<EEmbedWord> scaledList = new ArrayList<>();
		float sum = 0;
		for (int ind = 0; ind < weights.size(); ++ind) {
			if (weights.get(ind) > 0) {
				sum += weights.get(ind);
			}
		}
		for (int ind = 0; ind < words.size(); ++ind) {
			if (weights.get(ind) > 0) {
				EEmbedWord w = EEmbedWord.Scale(words.get(ind), 
						weights.get(ind) / sum);
				scaledList.add(w);
			}
			else {
				scaledList.add(new EEmbedWord("", words.get(0).Values.length, 0));
			}
		}
		EEmbedWord ave = EEmbedWord.GetSum(scaledList);		
		return ave;
	}

	public static EEmbedWord Scale(EEmbedWord word, float factor) {
		EEmbedWord result = new EEmbedWord("", word.Values.length, 0);
		for (int eInd = 0; eInd < result.Values.length; ++eInd) {
			result.Values[eInd] = word.Values[eInd] * factor;
		}
		return result;
	}
	
	public static EEmbedWord GetCentroid(EEmbedSpace sps, ETweet tw, 
			boolean weighted, String... posTags) throws Exception {
		HashSet<String> posSet = new HashSet<>(Arrays.asList(posTags));
		ArrayList<EEmbedWord> termList = new ArrayList<>();
		ArrayList<Float> termWeights = new ArrayList<>();
		for (int tInd = 0; tInd < tw.ETokens.size(); ++tInd) {
			EToken et = tw.ETokens.get(tInd);
			EEmbedWord word = sps.GetWord(et.Text);
			if (word != null) {
				if (posTags.length == 0 || 
						posSet.contains(et.POS)) {
					termList.add(word);
					termWeights.add(et.Weight);
				}
			}
		}
		EEmbedWord ave = null;
		if (termList.size() > 0) {
			if (!weighted) {
				ave = EEmbedWord.GetAverage(termList);
			}
			else {
				ave = EEmbedWord.GetWeightedAverage(termList, termWeights);
			}
		}
		else {
			ave = new EEmbedWord("", sps.GetWord("hello").Values.length, 0);
		}
		return ave;
	}
	
	public static int FindClosestCosine(ArrayList<EEmbedWord> words, EEmbedWord ew) {
		int maxInd = 0;
		double maxSim = 0;
		for (int exind = 0; exind < words.size(); ++exind) {
			double sim = EEmbedWord.GetCosineSim(ew, words.get(exind));
			if (exind == 0 || maxSim < sim) {
				maxInd = exind;
				maxSim = sim;
			}
		}
		return maxInd;
	}	
	
}
