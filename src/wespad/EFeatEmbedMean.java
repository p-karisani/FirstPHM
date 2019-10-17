package wespad;

import java.util.Hashtable;

public class EFeatEmbedMean extends EFeat {

	@Override
	void addFeat(ETweet tw, EFeatParam param) throws Exception {
		addFeatSelective(tw, param, false);
	}
	
	@Override
	void addFeat(EContext econ, EFeatParam param) throws Exception {
		addFeatSelective(econ, param, false);
	}
	
	void addFeatSelective(ETweet tw, EFeatParam param, boolean weighted, 
			String... posTags) throws Exception {
		String name = getFeatName();
		Hashtable<String, Double> feats = extractFeat(tw, param.Sps, 
				weighted, posTags);
		tw.Feats.put(name, feats);
	}
	
	public void addFeatSelective(EContext econ, EFeatParam param, 
			boolean weighted, String... posTags) throws Exception {
		String name = getFeatName();
		for (int ind = 0; ind < econ.CurrentTweets.size(); ++ind) {
			ETweet tw = econ.CurrentTweets.get(ind);
			Hashtable<String, Double> feats = extractFeat(tw, param.Sps, 
					weighted, posTags);
			tw.Feats.put(name, feats);
		}
	}

	private Hashtable<String, Double> extractFeat(ETweet tw, EEmbedSpace sps, 
			boolean weighted, String... posTags) throws Exception {
		EEmbedWord ave = EEmbedWord.GetCentroid(sps, tw, weighted, posTags);
		Hashtable<String, Double> feats = new Hashtable<>(ave.Values.length);
		for (int ind = 0; ind < ave.Values.length; ++ind) {
			feats.put("f" + ind, (double)ave.Values[ind]);
		}
		return feats;
	}

	public static Hashtable<String, Double> ConvertWordToTable(EEmbedWord word) {
		Hashtable<String, Double> feats = new Hashtable<>(word.Values.length);
		for (int ind = 0; ind < word.Values.length; ++ind) {
			feats.put("f" + String.format("%04d", ind), (double)word.Values[ind]);
		}
		return feats;
	}
	
}
