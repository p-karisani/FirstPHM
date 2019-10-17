package wespad;

import java.util.ArrayList;
import java.util.Hashtable;


public class EDump {

	private static String getTextKey(ETweet tw) {
		StringBuilder result = new StringBuilder();
		for (int tind = 0; tind < tw.ETokens.size(); ++tind) {
			if (!tw.ETokens.get(tind).POS.equals("U")) {
				result.append(tw.ETokens.get(tind).Text + "-");
			}
		}
		return result.toString();
	}

	public static ArrayList<ETweet> RemoveRepeatedandShorts(
			ArrayList<ETweet> list) throws Exception {
		ArrayList<ETweet> result = new ArrayList<>();
		Hashtable<String, Integer> reps = new Hashtable<>();
		for (int tind = 0; tind < list.size(); ++tind) {
			ETweet cur = list.get(tind);
			if (cur.ETokens.size() >= 5) {
				String key = getTextKey(cur);
				if (!reps.containsKey(key)) {
					reps.put(key, 0);
				}
				int count = reps.get(key);
				if (count < 10) {
					reps.put(key, count + 1);
					result.add(cur);
				}
			}			
		}
		return result;
	}

	public static void editQueries(ArrayList<ETweet> tws) {
		Hashtable<String, String> tbl = new Hashtable<>();
		tbl.put("alzheimer's", "alzheimer");
		tbl.put("parkinson's", "parkinson");
		for (int tind = 0; tind < tws.size(); ++tind) {
			ETweet tw = tws.get(tind);
			if (tbl.containsKey(tw.Query)) {
				tw.Query = tbl.get(tw.Query);
			}
		}
	}

}

