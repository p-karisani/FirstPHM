package wespad;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

public class ETweet {
	public static final int useridAlign = 25;
	public static final String tokenInReplyTo = "InReplyTo";
	public static final String tokenReply = "Reply";
	public static final String tokenDummyQuery = "DUMMY";
	
	class Repo {
		public Object ATag = null;
		
		public EEmbedWord Cent = null;
		
		public EEmbedWord centforBiasedMean = null;
		public Hashtable<String, Hashtable<String, Double>> FeatsCopy = null;
		public HashSet<String> Tokens = new HashSet<>();
		public double weightForRankInMeta = 0;

		public EEmbedWord centWordBased  = null;
		public double centWordBasedDist = 0;
		
//		public ArrayList<ETweet> similarTweets = new ArrayList<>();
		public Hashtable<String, ETweet> similarTweets = new Hashtable<>();
		public HashSet<String> Sequences = new HashSet<>();
		
		public EEmbedWord centforFisher = null;
		public double centSim = 0;

		public ArrayList<EEmbedWord> bertVecs = new ArrayList<>();
		public EEmbedWord bertVecsSent = null;
		public Integer[] indexesDisease = null;
		public Integer[] indexesHuman = null;
		public Integer[] indexesPositiveHuman = null;
		
		public Repo getCopy() {
			Repo result = new Repo();
			result.ATag = this.ATag;
			result.Cent = this.Cent;
			result.centforBiasedMean = this.centforBiasedMean;
			if (this.FeatsCopy != null) {
				result.FeatsCopy.putAll(this.FeatsCopy);
			}
			if (this.Tokens != null) {
				result.Tokens.addAll(this.Tokens);
			}
			result.weightForRankInMeta = this.weightForRankInMeta;
			result.centWordBased = this.centWordBased;
			result.centWordBasedDist = this.centWordBasedDist;
			if (this.similarTweets != null) {
				result.similarTweets.putAll(this.similarTweets);
			}
			if (this.Sequences != null) {
				result.Sequences.addAll(this.Sequences);
			}
			result.centforFisher = this.centforFisher;
			result.centSim = this.centSim;
			
			result.bertVecs.addAll(this.bertVecs);
			result.bertVecsSent = this.bertVecsSent;
			if (this.indexesDisease != null) {
				result.indexesDisease = this.indexesDisease.clone();
			}
			if (this.indexesHuman != null) {
				result.indexesHuman = this.indexesHuman.clone();
			}
			if (this.indexesPositiveHuman != null) {
				result.indexesPositiveHuman = this.indexesPositiveHuman.clone();
			}
			return result;
		}

	}

	public String AAAText;
	public String Tweetid;
	public int Label;
	public String Userid;
	public String Time;
	public int ReplyCount;
	public int LikeCount;
	public int RetweetCount;
	public String Query;
	public String[] QueryList;
	public String Text;
	public ArrayList<ETweet> InReplyToSample = new ArrayList<>();
	public ArrayList<ETweet> ReplySample = new ArrayList<>();
	public Repo repo = new Repo();
	
	public int NewLabel;
	public Hashtable<String, Hashtable<String, Double>> Feats = new Hashtable<>();
	
	public ESetType SetType;
	
	public ArrayList<EToken> ETokens = new ArrayList<>();
	
	public ETweetInfo Info = null;

	public ETweet() {
		
	}
	
	public ETweet(String line) {
		String[] tokens = line.split("\t");
		int ind = setFields(this, tokens, 0);
//		while (ind < tokens.length) {
//			ETweet tw = new ETweet();
//			if (tokens[ind].equals(tokenInReplyTo)) {
//				InReplyToSample.add(tw);
//			}
//			else if (tokens[ind].equals(tokenReply)) {
//				ReplySample.add(tw);
//			}
//			ind = setFields(tw, tokens, ind + 1);
//		}
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getString(this));
		for (int ind = 0; ind < InReplyToSample.size(); ++ind) {
			result.append("\t");
			result.append(tokenInReplyTo);
			result.append("\t");
			result.append(getString(InReplyToSample.get(ind)));
		}
		for (int ind = 0; ind < ReplySample.size(); ++ind) {
			result.append("\t");
			result.append(tokenReply);
			result.append("\t");
			result.append(getString(ReplySample.get(ind)));
		}
		return result.toString();
	}
	
	public void clearExtraInfo(boolean removeStructs) {
		if (removeStructs) {
			InReplyToSample = null;
			ReplySample = null;
			Feats = null;
			ETokens = null;
			return;
		}
		for (int ind = 0; ind < InReplyToSample.size(); ++ind) {
			InReplyToSample.get(ind).clearExtraInfo(true);
		}
		for (int ind = 0; ind < ReplySample.size(); ++ind) {
			ReplySample.get(ind).clearExtraInfo(true);
		}
	}
	
	public ETweet getCopy(boolean copyRepo) {
		ETweet result = new ETweet();
		result.AAAText = this.AAAText;
		result.Tweetid = this.Tweetid;
		result.Label = this.Label;
		result.Userid = this.Userid;
		result.Time = this.Time;
		result.ReplyCount = this.ReplyCount;
		result.LikeCount = this.LikeCount;
		result.RetweetCount = this.RetweetCount;
		result.Query = this.Query;
		result.QueryList = Arrays.copyOf(this.QueryList, this.QueryList.length);
		result.Text = this.Text;
		if (this.InReplyToSample != null) {
			for (int ind = 0; ind < this.InReplyToSample.size(); ++ind) {
				result.InReplyToSample.add(this.InReplyToSample.get(ind).getCopy(false));
			}
			for (int ind = 0; ind < this.ReplySample.size(); ++ind) {
				result.ReplySample.add(this.ReplySample.get(ind).getCopy(false));
			}
		}
		result.NewLabel = this.NewLabel;
		if (this.Feats != null) {
			String[] fs = this.Feats.keySet().toArray(new String[0]);
			for (int fInd = 0; fInd < fs.length; ++fInd) {
				Hashtable<String, Double> tbl = new Hashtable<>();
				result.Feats.put(fs[fInd], tbl);
				Hashtable<String, Double> thisTbl = this.Feats.get(fs[fInd]);
				String[] ts = thisTbl.keySet().toArray(new String[0]);
				for (int tInd = 0; tInd < ts.length; ++tInd) {
					tbl.put(ts[tInd], thisTbl.get(ts[tInd]));
				}
			}
		}
		if (this.ETokens != null) {
			result.ETokens = EToken.getCopy(this.ETokens);
		}
		if (copyRepo) {
			result.repo = this.repo.getCopy();
		}
		return result;
	}
	
	public void ResetWeights() {
		for (int ind = 0; ind < ETokens.size(); ++ind) {
			ETokens.get(ind).Weight = 1;
		}
	}
	
	private static  int setFields(ETweet tw, String[] tokens, int startIndex) {
		int ind = startIndex;
		tw.Tweetid = tokens[ind];
		ind++;
		tw.Label = Integer.parseInt(tokens[ind]);
		ind++;
		tw.Userid = tokens[ind].substring(0, useridAlign).trim();
		tw.Time = tokens[ind].substring(useridAlign, tokens[ind].length());
		ind++;
		tw.ReplyCount = Integer.parseInt(tokens[ind]);
		ind++;
		tw.LikeCount = Integer.parseInt(tokens[ind]);
		ind++;
		tw.RetweetCount = Integer.parseInt(tokens[ind]);
		ind++;
		tw.Query = tokens[ind];
		configureQuery(tw);
		ind++;
		String text = tokens[ind];		
		tw.AAAText = text;
		tw.Text = removeSarcasmTag(text);
		ind++;
		return ind;
	}
	
	public static String removeSarcasmTag(String text) {
		String sar = "#sarcasm";
		int pos = text.length() - sar.length();
		if (pos >= 0 && text.substring(pos).equals(sar)) {
			String result = text.substring(0, pos);
			return result;
		}
		else {
			return text;
		}
	}
	
	public static String getString(ETweet tw) {
		StringBuilder sb = new StringBuilder();
		sb.append(tw.Tweetid);
		sb.append("\t");
		sb.append(tw.Label);
		sb.append("\t");
		sb.append(String.format("%-" + useridAlign + "s", tw.Userid));
		String tm = ELib.normalizeTimeToStore(tw.Time);
		sb.append(tm);
		sb.append("\t");
		sb.append(tw.ReplyCount);
		sb.append("\t");
		sb.append(tw.LikeCount);
		sb.append("\t");
		sb.append(tw.RetweetCount);
		sb.append("\t");
		if (!tw.Query.equals(ETweet.tokenDummyQuery)) {
			sb.append(tw.Query);
		}
		else {
			if (tw.QueryList.length > 0) {
				for (String q : tw.QueryList) {
					sb.append("|" + q);
				}
			}
			else {
				sb.append("|");
			}
		}
		sb.append("\t");
		sb.append(tw.Text);	
		return sb.toString();
	}
	
	public static ArrayList<ETweet> load(String filePath, ETagLoadType type, 
			boolean unifyQueries) throws Exception {
		ArrayList<ETweet> result = new ArrayList<>();
		List<String> lines = Files.readAllLines(Paths.get(filePath), 
				Charset.forName("UTF-8"));
		if (type == ETagLoadType.POSTagger || type == ETagLoadType.POSTagger_All) {
			for (int ind = 0; ind < lines.size(); ++ind) {
				ETweet tw = new ETweet(lines.get(ind));
				if (!tw.Text.trim().equals("")) {
					if (type == ETagLoadType.POSTagger) {
						tw.ETokens = EFeat1Gram.getTokensWithPOSTagger(tw.Text, true);
					}
					else {
						tw.ETokens = EFeat1Gram.getTokensWithPOSTagger(tw.Text, false);
					}
					result.add(tw);
				}

				if ((ind + 1) % 10000 == 0) {
					System.out.println("loading " + filePath + "\t\t" + (ind + 1) + "/" + lines.size());
				}
			}
		}
		else {
			ArrayList<ArrayList<String>> tags = null;
			if (type == ETagLoadType.StoredTags ||
					type == ETagLoadType.StoredTags_All_DontUseThis) {
				tags = EFeat1Gram.ReadDepTags(filePath + "-tags");
			}
			for (int ind = 0; ind < lines.size(); ++ind) {
				ETweet tw = new ETweet(lines.get(ind));
				if (type == ETagLoadType.StoredTags_All_DontUseThis) {
					tw.ETokens = EFeat1Gram.ConvertAllTagsToTokens(tags.get(ind), true);
				}
				else if (type == ETagLoadType.StoredTags) {
					tw.ETokens = EFeat1Gram.ConvertTagsToTokens(tags.get(ind), true);
				}
				result.add(tw);

				if ((ind + 1) % 500000 == 0) {
					System.out.println("loading " + filePath + "\t\t" + (ind + 1) + "/" + lines.size());
				}
			}
		}
		if (unifyQueries) {
			for (int ind = 0; ind < result.size(); ++ind) {
				result.get(ind).Query = "Q";
				result.get(ind).QueryList = new String[] {"Q"};
			}
		}
		return result;
	}
	
	private static void configureQuery(ETweet tw) {
		if (tw.Query.charAt(0) == '|') {
			ArrayList<String> temp = new ArrayList<String>(Arrays.asList(tw.Query.split("\\|")));
			temp.removeAll(Collections.singleton(""));
			tw.QueryList = temp.toArray(new String[0]);
			tw.Query = ETweet.tokenDummyQuery;
		}
		else {
			tw.QueryList = new String[1];
			tw.QueryList[0] = tw.Query;
		}
	}
	
	public static void clearExtraInfo(ArrayList<ETweet> list, 
			boolean removeStructs) {
		for (int ind = 0; ind < list.size(); ++ind) {
			list.get(ind).clearExtraInfo(removeStructs);
		}
	}
	
	public static void save(ArrayList<ETweet> list, String filePath, 
			boolean saveTags) throws Exception {
		PrintStream ps = new PrintStream(filePath, "UTF-8");
		for (int ind = 0; ind < list.size(); ++ind) {
			ps.println(list.get(ind).toString());
		}
		ps.close();
		if (saveTags) {
			PrintStream psT = new PrintStream(filePath + "-tags", "UTF-8");
			for (int ind = 0; ind < list.size(); ++ind) {
				ETweet curt = list.get(ind);
				for (int toind = 0; toind < curt.ETokens.size(); ++toind) {
					psT.println(curt.ETokens.get(toind).TagLine);
				}
				psT.println();
			}
			psT.close();
		}
	}

	public static Hashtable<String, Hashtable<Integer, ArrayList<ETweet>>> split(
			ArrayList<ETweet> list) {
		Hashtable<String, Hashtable<Integer, ArrayList<ETweet>>> splits = 
				new Hashtable<>();
		for (int ind = 0; ind < list.size(); ++ind) {
			ETweet tw = list.get(ind);
			Hashtable<Integer, ArrayList<ETweet>> qTbl = splits.get(tw.Query);
			if (qTbl == null) {
				qTbl = new Hashtable<>();
				splits.put(tw.Query, qTbl);
			}
			ArrayList<ETweet> tar = qTbl.get(tw.Label);
			if (tar == null) {
				tar = new ArrayList<>();
				qTbl.put(tw.Label, tar);
			}
			tar.add(tw);
		}
		return splits;
	}

	public static ArrayList<ArrayList<ETweet>> splitByQueryAndLabel(
			ArrayList<ETweet> list, int fold) {
		Hashtable<String, Hashtable<Integer, ArrayList<ETweet>>> splits = split(list);
		ArrayList<ArrayList<ETweet>> result = new ArrayList<>();
		for (int ind = 0; ind < fold; ++ind) {
			result.add(new ArrayList<ETweet>());
		}
		String[] qs = splits.keySet().toArray(new String[0]);
		for (int qInd = 0; qInd < qs.length; ++qInd) {
			Hashtable<Integer, ArrayList<ETweet>> qTbl = splits.get(qs[qInd]);
			Integer[] ls = qTbl.keySet().toArray(new Integer[0]);
			for (int lInd = 0; lInd < ls.length; ++lInd) {
				ArrayList<ETweet> tws = qTbl.get(ls[lInd]);
				for (int tInd = 0; tInd < tws.size(); ++tInd) {
					result.get(tInd % fold).add(tws.get(tInd));
				}
			}
		}
		return result;
	}
	
	public static ArrayList<ETweet> sampleWithOriginalDist(
			ArrayList<ETweet> list, int sampleCount, int randSeed) throws Exception {
		Hashtable<String, Hashtable<Integer, ArrayList<ETweet>>> allTbl = split(list);
		Hashtable<Integer, ArrayList<ETweet>> tbl = allTbl.get(list.get(0).Query);
		Integer[] lbls = tbl.keySet().toArray(new Integer[0]);
		ArrayList<ETweet> result = new ArrayList<>();
		for (int ind = 0; ind < lbls.length; ++ind) {
			ArrayList<ETweet> tws = tbl.get(lbls[ind]);
			tws.sort((l, r) -> l.Tweetid.compareTo(r.Tweetid));
			ETweet.ShuffleTweets(tws, randSeed);
			int size = (int)Math.ceil(tws.size() * (float)sampleCount / list.size());
			size = Math.min(tws.size(), size);
			result.addAll(tws.subList(0, size));
		}
		// the following lines were not initially added
		// to be consistent with previous results I put in if statement
		// I hadn't used the code for samples below 500, 
		// so this way it should be consistent with previous results
		if (sampleCount > 500) {
			ETweet.ShuffleTweets(result, randSeed);
		}
		return result;
	}
	
	public static ArrayList<ETweet> excludeFolds(
			ArrayList<ArrayList<ETweet>> folds, int... indexes) {
		ArrayList<ETweet> result = new ArrayList<>();
		for (int ind = 0; ind < folds.size(); ++ind) {
			if (!ArrayUtils.contains(indexes, ind)) {
				result.addAll(folds.get(ind));
			}
		}
		return result;
	}
	
	public static ArrayList<ETweet> getTweetsByQuery(
			ArrayList<ETweet> list, String ... queries) {
		HashSet<String> qs = new HashSet<>();
		for (int ind = 0; ind < queries.length; ++ind) {
			qs.add(queries[ind].toLowerCase());
		}
		ArrayList<ETweet> result = new ArrayList<>();
		for (int ind = 0; ind < list.size(); ++ind) {
			if (qs.contains(list.get(ind).Query.toLowerCase())) {
				result.add(list.get(ind));
			}
		}
		return result;
	}
	
	public static ArrayList<ETweet> getTweetsByLabel(
			ArrayList<ETweet> list, int ... labels) {
		HashSet<Integer> lbls = new HashSet<>();
		for (int ind = 0; ind < labels.length; ++ind) {
			lbls.add(labels[ind]);
		}
		ArrayList<ETweet> result = new ArrayList<>();
		for (int ind = 0; ind < list.size(); ++ind) {
			if (lbls.contains(list.get(ind).Label)) {
				result.add(list.get(ind));
			}
		}
		return result;
	}
	
	public static ArrayList<ETweet> getTweetsByNewLabel(
			ArrayList<ETweet> list, int newLabel) {
		ArrayList<ETweet> result = new ArrayList<>();
		for (int ind = 0; ind < list.size(); ++ind) {
			if (newLabel == list.get(ind).NewLabel) {
				result.add(list.get(ind));
			}
		}
		return result;
	}
	
	public static ArrayList<ETweet> getTweetsByRealNewLabel(ELblConf lc,
			ArrayList<ETweet> list, int newLabel) throws Exception {
		ArrayList<ETweet> result = new ArrayList<>();
		for (int ind = 0; ind < list.size(); ++ind) {
			if (newLabel == lc.getCorrectNewLabel(list.get(ind).Label)) {
				result.add(list.get(ind));
			}
		}
		return result;
	}
	
	public static ArrayList<ETweet> getMisclassified(ELblConf lc,
			ArrayList<ETweet> list) throws Exception {
		ArrayList<ETweet> result = new ArrayList<>();
		for (int ind = 0; ind < list.size(); ++ind) {
			if (list.get(ind).NewLabel != 
					lc.getCorrectNewLabel(list.get(ind).Label)) {
				result.add(list.get(ind));
			}
		}
		return result;
	}
	
	public static ArrayList<ETweet> getCorrectClassified(
			ELblConf lc, ArrayList<ETweet> list) throws Exception {
		ArrayList<ETweet> result = new ArrayList<>();
		for (int ind = 0; ind < list.size(); ++ind) {
			if (list.get(ind).NewLabel == 
					lc.getCorrectNewLabel(list.get(ind).Label)) {
				result.add(list.get(ind));
			}
		}
		return result;
	}
	
	public static String[] getQueries(ArrayList<ETweet> list) {
		HashSet<String> qs = new HashSet<>();
		for (int ind = 0; ind < list.size(); ++ind) {
			if (!qs.contains(list.get(ind).Query)) {
				qs.add(list.get(ind).Query);
			}
		}
		return qs.toArray(new String[0]);
	}

	public static ArrayList<ArrayList<ETweet>> categorizeByQuery(
			ArrayList<ETweet> list) {
		String[] qs = ETweet.getQueries(list);
		ArrayList<ArrayList<ETweet>> result = new ArrayList<>();
		Hashtable<String, Integer> indexes = new Hashtable<>();
		for (int qInd = 0; qInd < qs.length; ++qInd) {
			result.add(new ArrayList<ETweet>());
			indexes.put(qs[qInd], qInd);
		}
		for (int tInd = 0; tInd < list.size(); ++tInd) {
			int qInd = indexes.get(list.get(tInd).Query);
			result.get(qInd).add(list.get(tInd));
		}
		return result;
	}
	
	public static Hashtable<String, ETweet> getAsTable(
			ArrayList<ETweet> list) {
        Hashtable<String, ETweet> tbl = new Hashtable<>();
        for (int ind = 0; ind < list.size(); ++ind) {
        	ETweet curt = list.get(ind);
        	if (!tbl.containsKey(curt.Tweetid)) {
        		tbl.put(curt.Tweetid, curt);
        	}
        	else {
        		ELib.Pass();
        	}
        }
        return tbl;
	}
	
	public static ArrayList<ETweet> copyList(
			ArrayList<ETweet> list, boolean byRef, boolean copyRepo) {
		ArrayList<ETweet> result = new ArrayList<>();
		for (int ind = 0; ind < list.size(); ++ind) {
			if (byRef) {
				result.add(list.get(ind));
			}
			else {
				result.add(list.get(ind).getCopy(copyRepo));
			}
		}
		return result;
	}
	
	public static void StoreFeats(ETweet tw) {
		tw.repo.FeatsCopy = tw.Feats;
		tw.Feats = new Hashtable<>();
	}
	
	public static void RestoreFeats(ETweet tw) {
		tw.Feats = tw.repo.FeatsCopy;
		tw.repo.FeatsCopy = null;
	}
	
	public static void StoreFeats(ArrayList<ETweet> list) {
		for (int ind = 0; ind < list.size(); ++ind) {
			StoreFeats(list.get(ind));
		}
	}
	
	public static void RestoreFeats(ArrayList<ETweet> list) {
		for (int ind = 0; ind < list.size(); ++ind) {
			RestoreFeats(list.get(ind));
		}
	}

	public static void AddTokenListToTokenSet(ArrayList<ETweet> list) {
		for (int ind = 0; ind < list.size(); ++ind) {
			ETweet tw = list.get(ind);
			tw.repo.Tokens.clear();
			for (int tind = 0; tind < tw.ETokens.size(); ++tind) {
				tw.repo.Tokens.add(tw.ETokens.get(tind).Text);
			}
		}
	}
	
	public static void EmptyTokenSet(ArrayList<ETweet> list) {
		for (int ind = 0; ind < list.size(); ++ind) {
			ETweet tw = list.get(ind);
			tw.repo.Tokens.clear();
		}
	}
	
	public static void ShuffleTweets(ArrayList<ETweet> list, long seed) {
		Random rnd = new Random(seed);
		ArrayList<ETweet> cpList = new ArrayList<>();
		while (list.size() > 0) {
			int ind = rnd.nextInt(list.size());
			cpList.add(list.get(ind));
			list.remove(ind);
		}
		list.addAll(cpList);
	}

	public static void SetSetType(ArrayList<ETweet> list, ESetType type) {
		for (int ind = 0; ind < list.size(); ++ind) {
			list.get(ind).SetType = type;
		}
	}
	
}
