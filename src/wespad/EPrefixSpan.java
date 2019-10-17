package wespad;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.AlgoPrefixSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.SequentialPattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.SequentialPatterns;

public class EPrefixSpan {

	public static Hashtable<String, ESequence> getSequencesPrefixSpanEmbed(
			EFeatParam param, ArrayList<ETweet> tws,   
			int minLen, int minSupp, float minIG, float seqBucSize) throws Exception {
		ArrayList<ETweet> ctws = EDump.RemoveRepeatedandShorts(tws);
		HashSet<Integer> posIds = new HashSet<>();
		HashSet<Integer> negIds = new HashSet<>();
		for (int twind = 0; twind < ctws.size(); ++twind) {
			if (param.LblCon.PosNewLbl.isSource(ctws.get(twind).Label)) {
				posIds.add(twind);
			}
			else {
				negIds.add(twind);
			}
		}
		ArrayList<EEmbedWord> tempEwsList = param.Sps.GetWords(ctws, true, true, true);
		ArrayList<EEmbedWord> ewsList = bucketWords(tempEwsList, seqBucSize);
		Hashtable<String, Integer> wsTbl = new Hashtable<>();
		for (int wind = 0; wind < ewsList.size(); ++wind) {
			wsTbl.put(ewsList.get(wind).Term, wind);
		}
		
		Hashtable<Integer, Integer> sentTweet = new Hashtable<>();
		StringBuilder encodeSb = new StringBuilder();
		int sentId = 0;
		for (int twind = 0; twind < ctws.size(); ++twind) {
			ETweet tw = ctws.get(twind);
			ArrayList<ArrayList<EToken>> sents = ETree.GetSentences(tw.ETokens, false);
			for (int seind = 0; seind < sents.size(); ++seind) {
				ArrayList<EToken> terms = sents.get(seind);
				for (int teind = 0; teind < terms.size(); ++teind) {
					if (!ELib.isStopword(terms.get(teind).Text)) {
						int wordBucket = getBucketCenter(param.Sps, 
								terms.get(teind), ewsList, wsTbl, seqBucSize);
						if (wordBucket >= 0) {
							encodeSb.append(wordBucket + " -1 ");
						}
					}
				}
				encodeSb.append("-2\n");
				sentTweet.put(sentId, twind);
				++sentId;
			}
		}
		
		ArrayList<SequentialPattern> pats = getSeqPatterns(ctws.size(), 
				encodeSb.toString(), minLen, minSupp);
		ArrayList<ESequence> seqs = extractSequences(
				pats, ctws, param.LblCon, ewsList, posIds, negIds, sentTweet, minIG);
		seqs.sort((ll, rr) -> -1 * Float.compare(ll.Value, rr.Value));
//		for (Sequence sq : seqs) {
//			System.out.println(sq);
//		}
		Hashtable<String, ESequence> result = new Hashtable<>();
		for (int sind = 0; sind < seqs.size(); ++sind) {
			result.put(seqs.get(sind).OrderedKey, seqs.get(sind));
		}
		return result;
	}
	
	private static ArrayList<EEmbedWord> bucketWords(ArrayList<EEmbedWord> wlist, 
			float seqBucSize) {
		ArrayList<EEmbedWord> cwlist = new ArrayList<>(wlist);
		cwlist.sort((ll, rr) -> ll.Term.compareTo(rr.Term));
		ArrayList<EEmbedWord> result = new ArrayList<>();
		for (int paind = 0; paind < cwlist.size(); ++paind) {
			EEmbedWord paew = cwlist.get(paind);
			int chind = 0;
			while (chind < result.size()) {
				EEmbedWord chew = cwlist.get(chind);
				double dist = EEmbedWord.GetDistance(paew, chew);
				if (dist <= seqBucSize) {
					break;
				}
				++chind;
			}
			if (chind == result.size()) {
				result.add(paew);
			}
		}
		return result;
	}
	
	private static int getBucketCenter(EEmbedSpace sps, EToken term, 
			ArrayList<EEmbedWord> ewsList, Hashtable<String, Integer> wsTbl, 
			float seqBucSize) {
		if (wsTbl.containsKey(term.Text)) {
			return wsTbl.get(term.Text);
		}
		else {
			EEmbedWord ew = sps.GetWordAndCreateForAbsent(term);
			if (ew != null) {
				for (int wind = 0; wind < ewsList.size(); ++wind) {
					EEmbedWord curew = ewsList.get(wind);
					double dist = EEmbedWord.GetDistance(ew, curew);
					if (dist <= seqBucSize) {
						return wind;
					}
				}
			}
		}
		return -1;
	}
	
	private static ArrayList<SequentialPattern>	getSeqPatterns(int tweetCount, 
			String inputData, int minLen, int minSupp) throws Exception {
		String tempFile = EVar.CurrentPath + "twcoded.txt";
		PrintStream tempPs = new PrintStream(tempFile);
		tempPs.print(inputData);
		tempPs.close();

		AlgoPrefixSpan algo = new AlgoPrefixSpan();
		algo.setMaximumPatternLength(10);
		float minSupport = (float)minSupp / tweetCount;
		SequentialPatterns patterns = algo.runAlgorithm(tempFile, minSupport, null);
		Files.delete(Paths.get(tempFile));
		
		ArrayList<SequentialPattern> result = new ArrayList<>();
		for(List<SequentialPattern> level : patterns.levels) {
			if (0 < level.size() && minLen <= level.get(0).size()) {
				for(SequentialPattern pattern : level){
					result.add(pattern);
				}
			}
		}
		return result;
	}
	
	private static ArrayList<ESequence> extractSequences(ArrayList<SequentialPattern> pats, 
			ArrayList<ETweet> tws, ELblConf lbl, ArrayList<EEmbedWord> ewsList, 
			HashSet<Integer> posIds, HashSet<Integer> negIds, 
			Hashtable<Integer, Integer> sentTweet, float minIG) {
		ArrayList<ESequence> result = new ArrayList<>();
		EPair<Double, Double> counts = ELib.getPosNegCount(tws, lbl);
		for (int pind = 0; pind < pats.size(); ++pind) {
			SequentialPattern sp = pats.get(pind);
			ESequence cursq = new ESequence();
			cursq.TweetCount = sp.getAbsoluteSupport();
			for (int iind = 0; iind < sp.size(); ++iind) {
				EEmbedWord item = ewsList.get(sp.get(iind).get(0));
				cursq.Words.add(new EEmbedWordDensity(item, item.Term));
			}
			cursq.OrderedKey = getOrderedSequenceKey(cursq.Words);
			List<Integer> ids = sp.getSequenceIDs();
			HashSet<Integer> added = new HashSet<>();
			for (int idind = 0; idind < ids.size(); ++idind) {
				int twid = sentTweet.get(ids.get(idind));
				if (!added.contains(twid)) {
					added.add(twid);
					if (posIds.contains(twid)) {
						++cursq.Existing.Key;
					}
					else {
						++cursq.Existing.Value;
					}
				}
			}
			cursq.Nonexisting.Key = posIds.size() - cursq.Existing.Key;
			cursq.Nonexisting.Value = negIds.size() - cursq.Existing.Value;
			cursq.IsPositive = cursq.Existing.Key > cursq.Existing.Value ? true : false;
			double ig = ELib.GetIG(counts, cursq.Existing, cursq.Nonexisting);
			cursq.Value = (float)ig;
			if (cursq.Value >= minIG) {
				result.add(cursq);
			}
		}
		return result;
	}
	
	private static String getOrderedSequenceKey(ArrayList<EEmbedWordDensity> words) {
		StringBuilder key = new StringBuilder();
		for (int tind = 0; tind < words.size(); ++tind) {
			key.append(words.get(tind).WordTerm);
			if (tind + 1 < words.size()) {
				key.append("-");
			}
		}
		return key.toString();
	}
	
	public static ArrayList<ESequence> findSequences(ETweet tw, 
			ArrayList<ESequence> seqs, boolean removeSubs)  throws Exception {
		ArrayList<ArrayList<EToken>> sents = ETree.GetSentences(tw.ETokens, false);
		ArrayList<ESequence> result = new ArrayList<>();
		for (int sind = 0; sind < sents.size(); ++sind) {
			ArrayList<EToken> sent = sents.get(sind);
			ArrayList<ESequence> cur = getSequencesInSentence(sent, seqs);
			result.addAll(cur);
		}
		if (removeSubs) {
			result = removeSubSequences(result);
		}
		return result;
	}
	
	private static ArrayList<ESequence> getSequencesInSentence(
			ArrayList<EToken> terms, ArrayList<ESequence> seqs) {
		Hashtable<String, ArrayList<Integer>> posTbl = new Hashtable<>();
		for (int tind = 0; tind < terms.size(); ++tind) {
			ArrayList<Integer> posList = posTbl.get(terms.get(tind).Text);
			if (posList == null) {
				posList = new ArrayList<>();
				posTbl.put(terms.get(tind).Text, posList);
			}
			posList.add(tind);
		}
		ArrayList<ESequence> result = new ArrayList<>();
		for (int sind = 0; sind < seqs.size(); ++sind) {
			ESequence seq = seqs.get(sind);
			int wind = 0;
			while (wind < seq.Words.size()) {
				EEmbedWordDensity word = seq.Words.get(wind);
				if (!posTbl.containsKey(word.WordTerm)) {
					break;
				}
				if (0 < wind) {
					ArrayList<Integer> prePos = posTbl.get(seq.Words.get(wind - 1).WordTerm);
					ArrayList<Integer> curPos = posTbl.get(word.WordTerm);
					if (Collections.max(curPos) <= Collections.min(prePos)) {
						break;
					}
				}
				++wind;
			}
			if (wind == seq.Words.size()) {
				result.add(seq);
			}
		}
		return result;
	}
	
	private static ArrayList<ESequence> removeSubSequences(
			ArrayList<ESequence> ords) {
		ArrayList<ESequence> result = new ArrayList<>();
		for (int paind = 0; paind < ords.size(); ++paind) {
			ESequence paOrd = ords.get(paind);
			int chind = 0;
			while (chind < ords.size()) {
				ESequence chOrd = ords.get(chind);
				if (paind != chind) {
					int paPos = 0;
					int chPos = 0;
					while (paPos < paOrd.Words.size() && chPos < chOrd.Words.size()) {
						if (paOrd.Words.get(paPos).WordTerm.equals(
								chOrd.Words.get(chPos).WordTerm)) {
							++paPos;
							++chPos;
						}
						else {
							++chPos;
						}
					}
					if (paPos == paOrd.Words.size()) {
						break;
					}
				}
				++chind;
			}
			if (chind == ords.size()) {
				result.add(paOrd);
			}
		}
		return result;
	}
	
}
