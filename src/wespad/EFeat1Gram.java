package wespad;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger.TaggedToken;

public class EFeat1Gram extends EFeat {

	static Tagger tag;
	
	static {
		try {
			tag = new Tagger();
			tag.loadModel(EVar.POSModel);
		}
		catch (Exception exc) {
			System.out.println(exc.getMessage());
		}
	}

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
		for (int tInd = 0; tInd < tw.ETokens.size(); ++tInd) {
			addToken(feats, tw.ETokens.get(tInd).Text);
		}
		return feats;
	}

	public static void loadTokens(ArrayList<ETweet> list) throws Exception {
		for (int ind = 0; ind < list.size(); ++ind) {
			ETweet tw = list.get(ind);
			if (tw.Text.length() > 0) {
				tw.ETokens = getTokensWithPOSTagger(tw.Text, true);				
			}
			else {
				tw.ETokens.clear();
			}
		}
	}
	
	public static ArrayList<EToken> getTokensWithPOSTagger(String text, 
			boolean removeDelimiters) {
		ArrayList<EToken> result = new ArrayList<>();
		HashSet<String> qs = new HashSet<>();
//		qs.addAll(Arrays.asList(tw.Query.split(" ")));
		List<TaggedToken> tts = null;
		try {
			tts = tag.tokenizeAndTag(text);
		}
		catch (Exception err) {
			System.out.println("exception in tokenization text: (" + text + ")");
		}
		for (int tInd = 0; tInd < tts.size(); ++tInd) {
			TaggedToken tok = tts.get(tInd);
			tok.token = tok.token.toLowerCase();
			if (!removeDelimiters || !ELib.isDelimiter(tok.token)
//					&& !qs.contains(tok.token)
					) {
				String term = tok.token;
//				if (tok.token.charAt(0) == '#') {
//					term = tok.token.substring(1);
//				}
				EToken etok = new EToken();
				etok.Text = term;
				etok.POS = tok.tag;
				etok.Weight = 1;
				result.add(etok);
			}
		}
		return result;
	}

	public static ArrayList<ArrayList<String>> ReadDepTags(String filePath) 
			throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(filePath), 
				Charset.forName("UTF-8"));
		ArrayList<ArrayList<String>> result = new ArrayList<>();
		ArrayList<String> cur = null;
		int lind = 0;
		while (lind < lines.size()) {
			String[] tokens = lines.get(lind).split("\t");
			if (tokens[0].equals("1")) {
				cur = new ArrayList<>();
				result.add(cur);
			}
			if (!lines.get(lind).equals("")) {
				cur.add(lines.get(lind));
			}
			++lind;
		}
		
//		//	verify format
//		for (int rind = 0; rind < result.size(); ++rind) {
//			ArrayList<String> tags = result.get(rind);
//			for (int tind = 0; tind < tags.size(); ++tind) {
//				String[] tokens = tags.get(tind).split("\t");
//				if (tind + 1 != Integer.parseInt(tokens[0])) {
//					System.out.println("unformatted tags found!");
//				}
//			}
//		}
		
		return result;
	}
	
	public static ArrayList<EToken> convertLinesToTokens(
			ArrayList<String> tags, boolean loadTree) {
		ArrayList<EToken> result = new ArrayList<>();
		for (int tind = 0; tind < tags.size(); ++tind) {
			EToken et = new EToken();
			result.add(et);
		}
		for (short tind = 0; tind < tags.size(); ++tind) {
			EToken et = result.get(tind);
			et.TagLine = tags.get(tind);
			String[] cols = et.TagLine.split("\t");
			et.Order = (short)(tind + 1);
			et.Text = cols[1].toLowerCase();
			et.POS = cols[4];
			if (cols.length > 8) {
				et.IsSynthesized = cols[8].equals("new") ? true : false;
			}			
			if (cols.length > 9) {
				et.IsHuman = cols[9].equals("H") ? true : false;
			}
			if (cols.length > 10) {
				et.IsPositiveHuman = cols[10].equals("PH") ? true : false;
			}
			et.Weight = 1;
			if (loadTree) {
				et.RootValue = Short.parseShort(cols[6]);
				if (et.RootValue != -1 && et.RootValue != 0) {
					et.Root = result.get(et.RootValue - 1);
				}
			}			
		}
		return result;
	}
	
	public static ArrayList<EToken> ConvertAllTagsToTokens(
			ArrayList<String> tags, boolean loadTree) {
		ArrayList<EToken> result = convertLinesToTokens(tags, loadTree);
		if (loadTree) {
			buildTweetTrees(result);
		}
		return result;
	}
		
	public static ArrayList<EToken> ConvertTagsToTokens(ArrayList<String> tags, 
			boolean loadTree) {
		ArrayList<EToken> result = convertLinesToTokens(tags, loadTree);
		int tind = 0;
		while (tind < result.size()) {
			EToken et = result.get(tind);
			if (ELib.isDelimiter(et.Text)) {
				result.remove(tind);
			}
			else {
				if (et.Text.charAt(0) == '#') {
					et.Text = et.Text.substring(1);
				}
				++tind;
			}
		}
		if (loadTree) {
			buildTweetTrees(result);
		}
		return result;
	}
	
	public static void buildTweetTrees(ArrayList<EToken> tokens) {
		for (int paind = 0; paind < tokens.size(); ++paind) {
			EToken pat = tokens.get(paind);
			if (pat.RootValue != -1) {
				for (int chind = 0; chind < tokens.size(); ++chind) {
					EToken cht = tokens.get(chind);
					if (pat.Order == cht.RootValue) {
						pat.Children.add(cht);
					}
				}
			}
		}
	}
	
}
