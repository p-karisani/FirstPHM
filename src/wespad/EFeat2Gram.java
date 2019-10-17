package wespad;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class EFeat2Gram extends EFeat {

	@Override
	void addFeat(ETweet tw, EFeatParam param) throws Exception {
		String name = getFeatName();
		Hashtable<String, Double> feats = extractFeat(tw);
		tw.Feats.put(name, feats);
	}
	
	@Override
	void addFeat(EContext econ, EFeatParam param) throws Exception {
		String name = getFeatName();
		HashSet<ETweet> done = new HashSet<>();
		addFeatsToList(econ.CurrentTweets, name, done);
		addFeatsToList(econ.UnlabeledCurrentTweets, name, done);
		if (param.Test != null) {
			addFeatsToList(param.Test, name, done);
		}
		if (param.Train != null) {
			addFeatsToList(param.Train, name, done);
		}
	}
	
	private void addFeatsToList(ArrayList<ETweet> list, String tblName, 
			HashSet<ETweet> done) {
		for (int ind = 0; ind < list.size(); ++ind) {
			ETweet tw = list.get(ind);
			if (!done.contains(tw)) {
				Hashtable<String, Double> feats = extractFeat(tw);
				tw.Feats.put(tblName, feats);
				done.add(tw);
			}
		}
	}

	private void addToken(Hashtable<String, Double> feats, String token) {
		if (!feats.contains(token)) {
			feats.put(token, 0.0);
		}
		feats.put(token, 1d);
	}
	
	private Hashtable<String, Double> extractFeat(ETweet tw) {
		Hashtable<String, Double> feats = new Hashtable<>(tw.ETokens.size());
		for (int tInd = 0; tInd < tw.ETokens.size() - 1; ++tInd) {
			String tok = tw.ETokens.get(tInd).Text + " " + tw.ETokens.get(tInd + 1).Text;
			addToken(feats, tok);
		}
		return feats;
	}
	
}
