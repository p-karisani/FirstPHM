package wespad;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.commons.lang.NotImplementedException;

import wespad.EMalletClassifier.CLSType;

public class EFeatEmbedMeta extends EFeat {

	private static Hashtable<String, CacheObj> CacheTbl = new Hashtable<>();
	
	class CacheObj {
		public EFeatParamEmbedMetaSub Param;
		public EClassifier CentClsBiased;
		public EClassifier CentCls;
		public ArrayList<EEmbedWord> TweetExamplars;
		public EClassifier SentCls;
		public ArrayList<ETree> Trees;
		public EClusterer TweetClu;
		public EClusterer TweetCluBiased;
		public EClusterer SentClu;
	}
	
	public static void ClearCacheTbl() {
		CacheTbl.clear();
	}
	
	class Meta {
		
		public Hashtable<String, EEmbedWordDensity> allWordTbl = new Hashtable<>();
		public ArrayList<EEmbedWord> allWord = new ArrayList<>();
		
		public ArrayList<EEmbedWordDensity> ListIG = new ArrayList<>();
		public double missedIG = 0;
		
		public EClassifier CentCls;
		public EClassifier CentClsBiased;

		public ArrayList<ETree> Trees = new ArrayList<>();

		public EClusterer TweetClu;
		public EClusterer TweetCluBiased;
	}

	@Override
	void addFeat(ETweet tw, EFeatParam param) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	void addFeat(EContext econ, EFeatParam param) throws Exception {
		ETweet.AddTokenListToTokenSet(econ.CurrentTweets);
		String name = getFeatName();
		EFeatParamEmbedMeta emParam = (EFeatParamEmbedMeta)param;

		ArrayList<Meta> mList = new ArrayList<>();
		for (int ind = 0; ind < emParam.subs.length; ++ind) {
			emParam.subs[ind].CacheId = null;
			if (emParam.CacheId != null) {
				emParam.subs[ind].CacheId = ind + "/" + emParam.CacheId;
			}
			Meta m = extractMeta(emParam.subs[ind]);
			mList.add(m);
		}
		HashSet<String> trainIDs = getTrainSet(param.Train);
		for (int ind = 0; ind < econ.CurrentTweets.size(); ++ind) {
			ETweet tw = econ.CurrentTweets.get(ind);
			for (int sind = 0; sind < emParam.subs.length; ++sind) {
				Hashtable<String, Double> feats = extractFeat(
						tw, emParam.subs[sind], mList.get(sind), trainIDs);
				tw.Feats.put(name + "@" + sind, feats);
			}
		}

		ETweet.EmptyTokenSet(econ.CurrentTweets);
	}
	
	private Meta extractMeta(EFeatParamEmbedMetaSub param) throws Exception {
		Meta meta = new Meta();
		meta.allWordTbl = getWordTable(param.Sps, param.Train);
		meta.allWord = getAllWords(meta.allWordTbl);
		fillInvertedLists(meta, param);
		meta.ListIG = setIGAndSort(param, meta);

		if (param.CacheId == null) {
			meta.CentClsBiased = trainOverCentroids(param, meta, true);
			meta.TweetCluBiased = trainClusterOverTweets(param, meta, param.TweetClusterBiased, true);
			meta.CentCls = trainOverCentroids(param, meta, false);
			meta.TweetClu = trainClusterOverTweets(param, meta, param.TweetCluster, false);
			meta.Trees = EFREQT.getTreesFREQT(param.LblCon, param.Train, 2, 10, 10, 0, null);
		}
		else {
			if (!CacheTbl.containsKey(param.CacheId)) {
				CacheObj co = new CacheObj();
				co.Param = param;
				co.CentClsBiased = trainOverCentroids(param, meta, true);
				co.TweetCluBiased = trainClusterOverTweets(param, meta, param.TweetClusterBiased, true);
				co.CentCls = trainOverCentroids(param, meta, false);
				co.TweetClu = trainClusterOverTweets(param, meta, param.TweetCluster, false);
				co.Trees = EFREQT.getTreesFREQT(param.LblCon, param.Train, 2, 10, 10, 0, null);
				CacheTbl.put(param.CacheId, co);
			}
			CacheObj co = CacheTbl.get(param.CacheId);
			
			EFeatParamEmbedMetaSub orig = co.Param;
			if (co.Param.TweetClusterBiased != param.TweetClusterBiased) {
				co.TweetCluBiased = trainClusterOverTweets(param, meta, param.TweetClusterBiased, true);
				orig = param;
			}
			if (co.Param.TweetCluster != param.TweetCluster) {
				co.TweetClu = trainClusterOverTweets(param, meta, param.TweetCluster, false);
				orig = param;
			}
			co.Param = orig;
			
			meta.CentClsBiased = co.CentClsBiased;
			meta.TweetCluBiased = co.TweetCluBiased;
			meta.CentCls = co.CentCls;
			meta.TweetClu = co.TweetClu;
			meta.Trees = co.Trees;
		}


//		meta.PreNextCls = trainClsOverPreNext(param, meta, false);
//		meta.PreNextClu = trainCluOverPreNext(param, meta, 3, false);
//		meta.ProfCls = trainClsOverProfiles(param, meta, false);
//		meta.ProfClu = trainCluOverProfiles(param, meta, 3, false);
//		meta.SentCls = trainOverSents(param, meta);
//		meta.SentClu = trainClusterOverSentences(param, param.SentCluster);
//		co.SentCls = trainOverSents(param, meta);
//		co.SentClu = trainClusterOverSentences(param, param.SentCluster);
//		if (co.Param.SentCluster != param.SentCluster) {
//			co.SentClu = trainClusterOverSentences(param, param.SentCluster);
//			orig = param;
//		}
//		meta.SentCls = co.SentCls;
//		meta.SentClu = co.SentClu;
//		meta.TweetExamplars = getTweetExamplars(param);
//		meta.SentExamps = getSentenceExamplars(param, meta);
//		co.TweetExamplars = getTweetExamplars(param);
//		co.SentExamps = getSentenceExamplars(param, meta);
//		meta.TweetExamplars = co.TweetExamplars;
//		meta.SentExamps = co.SentExamps;
//		for (int ind = 0; ind < param.Train.size(); ++ind) {
//		ETweet tw = param.Train.get(ind);
////		setTermWeights(meta, param, tw);
////		EEmbedWord cent = EEmbedWord.GetCentroid(param.Sps, tw, false);
////		EEmbedWord centBiased = EEmbedWord.GetCentroid(param.Sps, tw, true);
////		EEmbedWord centNoun = EEmbedWord.GetCentroid(param.Sps, tw, false, 
////				"N", "O", "^", "S", "Z");
////		EEmbedWord centVerb = EEmbedWord.GetCentroid(param.Sps, tw, false, 
////				"V", "L", "M");
//		if (param.LblCon.PosNewLbl.isSource(tw.Label)) {
//			meta.tweetsPos.add(tw);
////			meta.CentPos.add(cent);
////			meta.CentPosBiased.add(centBiased);
////			meta.CentPosNoun.add(centNoun);
////			meta.CentPosVerb.add(centVerb);
//		}
//		else if (param.LblCon.NegNewLbl.isSource(tw.Label)) {
//			meta.tweetsNeg.add(tw);
////			meta.CentNeg.add(cent);
////			meta.CentNegBiased.add(centBiased);
////			meta.CentNegNoun.add(centNoun);
////			meta.CentNegVerb.add(centVerb);
//		}
////		tw.ResetWeights();
//	}
//		meta.CentClsVerb = trainOverCentroids(param, meta, false, 
//				"V", "N", "O", "S", "^", "Z", "L", "M");
//		meta.CentClsVerb = trainOverCentroids(param, meta, false, "V");
//		meta.CliquesPos = getCliques(param.Sps, meta.tweetsPos, param.CliqPosRad, CliqueMinimumSize);
//		meta.CliquesNeg = getCliques(param.Sps, meta.tweetsNeg, param.CliqPosRad, CliqueMinimumSize);
//		getWordExamplars(meta, param.IGExamp);
//		ArrayList<EEmbedWordDensity> ws = new ArrayList<>(meta.allWordTbl.values());
//		meta.WordClu = trainClusterOverWords(ws, new EClusterParam(param.WordCluster));
//		Hashtable<String, EEmbedWordDensity> poswords = getWordTable(param.Sps, meta.tweetsPos);
//		ArrayList<EEmbedWordDensity> poslist = new ArrayList<>(poswords.values());
//		poslist.sort((l, r) -> -1 * (l.Count - r.Count));
//		Hashtable<String, EEmbedWordDensity> negwords = getWordTable(param.Sps, meta.tweetsNeg);
//		ArrayList<EEmbedWordDensity> neglist = new ArrayList<>(negwords.values());
//		neglist.sort((l, r) -> -1 * (l.Count - r.Count));
//		meta.Sequences = EPrefixSpan.getSequencesPrefixSpanEmbed(
//				param, param.Train, 2, 10, 0, 0);
//		System.out.println(ELib.getTime());
//		ArrayList<ETree> trs = EFREQT.getTreesFREQT(param, param.Train, 2, 10, 0.005f);
//		System.out.println(ELib.getTime());
//		for (int tind = 0; tind < param.Train.size(); ++tind) {
//			ArrayList<ETree> existingTrs = EFREQT.findTrees(param.Train.get(tind), trs);
//		}
//		System.out.println(ELib.getTime());
//		for (int tind = 0; tind < trs.size(); ++tind) {
//			System.out.println(trs.get(tind));
//		}
//		System.out.println();
//		if (Float.compare(param.CentSeqThr, 0.6f) != 0) {
//			meta.SequencesPos = EPrefixSpan.getSequencesPrefixSpanEmbed(
//					param, param.Train, param.SeqMinLen, param.SeqSup, 
//					param.SeqIG, param.SeqBucSize);
//			meta.CentClsSeqMean = trainOverEmbedSequencedMean(param, meta);
//		}
//		meta.CentClsNoun = trainOverCentroids(param, meta, false, "N", "O", "S", "^", "Z", "L", "M");
//		meta.CentClsAdj = trainOverCentroids(param, meta, false, "A");
//		meta.CentClsInterj = trainOverCentroids(param, meta, false, "!");
//		meta.SequencesPos = getSequencesByTweetIG(param.Train, param.LblCon, param.SeqCount);
//		meta.SequencesPos = getSequencesByTweetFreq(meta.tweetsPos);
//		meta.SequencesNeg = getSequencesByTweetFreq(meta.tweetsNeg);
//		polishCommonSequences(meta.SequencesPos, meta.SequencesNeg, 8, 0.7f);
//		testSequences(meta.SequencesPos, meta.SequencesNeg, meta.CliquesPos, meta.CliquesNeg);
//		meta.SequencesPos = getSequencesByClique(meta.CliquesPos);
//		meta.SequencesNeg = getSequencesByClique(meta.CliquesNeg);
//		meta.EmbedWinsPos = getEmbedWins(param.Sps, meta.tweetsPos);
//		meta.EmbedWinsNeg = getEmbedWins(param.Sps, meta.tweetsNeg);
//		ArrayList<EEmbedWord> posWords = param.Sps.GetWords(meta.tweetsPos, false);
//		ArrayList<EEmbedWord> negWords = param.Sps.GetWords(meta.tweetsNeg, false);
//		meta.WordCls = trainOverWords(param, posWords, negWords);
//		for (Clique cl : meta.CliquesPos) {
//			System.out.println(cl.toString());
//		}
//		System.out.println("----------------");
//		for (Clique cl : meta.CliquesNeg) {
//			System.out.println(cl.toString());
//		}
//		removeCommonCliques(meta.CliquesPos, meta.CliquesNeg);
//		meta.CentCluPos = trainClusterOverCents(meta.CentPos, param.CentCluster);
//		meta.CentCluPosNoun = trainClusterOverCents(meta.CentPosNoun, param.CentNounCluster);
//		meta.CentCluPosVerb = trainClusterOverCents(meta.CentPosVerb, param.CentVerbCluster);
//		meta.CentCluPosBiased = trainClusterOverCents(meta.CentPosBiased, param.CentBiasedCluster);
//		meta.aveCentPosInRange = getAveCentInRange(meta.CentPos, param.BinCount);
//		meta.aveCentNegInRange = getAveCentInRange(meta.CentNeg, param.BinCount);
//		meta.aveCentPosBiasedInRange = getAveCentInRange(meta.CentPosBiased, param.BinCount);
//		meta.aveCentNegBiasedInRange = getAveCentInRange(meta.CentNegBiased, param.BinCount);
//		meta.ListIGExt = setIGExtAndSort(param, meta);
//		meta.posWordTbl = getTable(posWords);
//		meta.negWordTbl = getTable(negWords);
//		getDimensionPercentiles(new ArrayList<>(meta.posWordTbl.values()), 
//				meta.posVal50pDims);
//		getDimensionPercentiles(new ArrayList<>(meta.negWordTbl.values()), 
//				meta.negVal50pDims);
//		visualizePoints(meta, param);
		return meta;
	}
		
	private EClassifier trainOverCentroids(EFeatParamEmbedMetaSub param, 
			Meta meta, boolean weighted, String... posTags) throws Exception {
		ArrayList<ETweet> stay = new ArrayList<>();
		for (int tind = 0; tind < param.Train.size(); ++tind) {
			ETweet tw = param.Train.get(tind);
			ArrayList<EEmbedWord> ws = param.Sps.GetWords(
					tw, true, true, false, posTags);
			if (ws.size() > 0) {
				stay.add(tw);
			}
		}
		ETweet.StoreFeats(stay);
		if (weighted) {
			for (int ind = 0; ind < stay.size(); ++ind) {
				setTermWeights(meta, param, stay.get(ind).ETokens);
			}
		}
		ELblConf lc = new ELblConf(0, 1, 
				new ELbl(0, EVar.LblNonEventHealth),
				new ELbl(1, EVar.LblEventHealth));
		EContext econ = new EContext(0, stay, null);
		EFeatCollector.addEmbedMeanFeatures(econ, param, weighted, posTags);
		EClassifier result = EMalletClassifier.Train(econ.CurrentTweets, lc, CLSType.Logistic);
		if (weighted) {
			for (int ind = 0; ind < stay.size(); ++ind) {
				stay.get(ind).ResetWeights();
			}
		}
		ETweet.RestoreFeats(stay);
		return result;
	}

	private void fillInvertedLists(Meta meta, EFeatParamEmbedMetaSub param) {
		ArrayList<EEmbedWordDensity> wlist = new ArrayList<>(meta.allWordTbl.values());
		for (int tind = 0; tind < param.Train.size(); ++tind) {
			ETweet tw = param.Train.get(tind);
			for (int wind = 0; wind < wlist.size(); ++wind) {
				EEmbedWordDensity ewd = wlist.get(wind);
				if (tw.repo.Tokens.contains(ewd.Word.Term)) {
					ewd.InvertedList.add(tw);
				}
			}
		}
	}

	private Hashtable<String, EEmbedWordDensity> getWordTable(EEmbedSpace nullableSps, 
			ArrayList<ETweet> tlist) {
		Hashtable<String, EEmbedWordDensity> result = new Hashtable<>();
		if (nullableSps != null) {
			ArrayList<EEmbedWord> words = nullableSps.GetWords(tlist, true, false, false);
			for (int wind = 0; wind < words.size(); ++wind) {			
				EEmbedWordDensity ewd = new EEmbedWordDensity(words.get(wind), words.get(wind).Term);
				result.put(ewd.Word.Term, ewd);
				for (int tind = 0; tind < tlist.size(); ++tind) {
					ETweet tw = tlist.get(tind);
					if (tw.repo.Tokens.contains(ewd.Word.Term)) {
						++ewd.Count;
					}
				}
			}
		}
		else {
			for (int twind = 0; twind < tlist.size(); ++twind) {
				ETweet tw = tlist.get(twind);
				ArrayList<String> tokens = new ArrayList<>(tw.repo.Tokens);
				for (int tind = 0; tind < tokens.size(); ++tind) {
					String tok = tokens.get(tind);
					EEmbedWordDensity ewd = result.get(tok);
					if (ewd == null) {
						ewd = new EEmbedWordDensity(null, tok);
						result.put(tok, ewd);
					}
					++ewd.Count;
				}
			}
		}
		return result;
	}

	private ArrayList<EEmbedWord> getAllWords(Hashtable<String, EEmbedWordDensity> allTbl) {
		ArrayList<EEmbedWordDensity> lst = new ArrayList<>(allTbl.values());
		ArrayList<EEmbedWord> result = new ArrayList<>();
		for (int ind = 0; ind < lst.size(); ++ind) {
			if (lst.get(ind).Word != null) {
				result.add(lst.get(ind).Word);
			}
		}
		return result;
	}
	
	private ArrayList<EEmbedWordDensity> setIGAndSort(EFeatParamEmbedMetaSub param, 
			Meta meta) throws Exception {
		EPair<Double, Double> counts = getPosNegCount(param.Train, param.LblCon);
		double infoAll = ELib.getInfoForIG(counts);
		ArrayList<EEmbedWordDensity> result = new ArrayList<>(meta.allWordTbl.values());
		for (int eind = 0; eind < result.size(); ++eind) {
			EEmbedWordDensity word = result.get(eind);
			ArrayList<ETweet> exists = new ArrayList<>();
			ArrayList<ETweet> notexists = new ArrayList<>();
			for (int tind = 0; tind < param.Train.size(); ++tind) {
				if (param.Train.get(tind).repo.Tokens.contains(word.Word.Term)) {
					exists.add(param.Train.get(tind));
				}
				else {
					notexists.add(param.Train.get(tind));
				}
			}
			EPair<Double, Double> countsExists = getPosNegCount(
					exists, param.LblCon);
			double infoWordExist = ((double)exists.size() / param.Train.size()) * 
					ELib.getInfoForIG(countsExists);
			EPair<Double, Double> countsNotexists = getPosNegCount(
					notexists, param.LblCon);
			double infoWordNotexist = ((double)notexists.size() / param.Train.size()) * 
					ELib.getInfoForIG(countsNotexists);
			double infoWord = infoWordExist + infoWordNotexist;
			double ig = infoAll - infoWord;
			word.Existing = countsExists;
			word.Nonexisting = countsNotexists;
			word.IG = (float)ig;
			if (word.Count == 1 && countsExists.Value == 1) {
				meta.missedIG = ig;
			}
			if (Double.compare(ig, 0) == 0) {
				System.out.print("");
			}
		}
//		normalizeIG(meta.allWordTbl, 3);
		result.sort((l, r) -> -1 * Double.compare(l.IG, r.IG));
		return  new ArrayList<>(result.subList(0, 100));
//		return  new ArrayList<>(result.subList(0, param.IG)); 
	}
	
	private EPair<Double, Double> getPosNegCount(ArrayList<ETweet> list, 
			ELblConf lbl) {
		EPair<Double, Double> result = new EPair<Double, Double>(0d, 0d);
		for (int ind = 0; ind < list.size(); ++ind) {
			ETweet tw = list.get(ind);
			if (lbl.PosNewLbl.isSource(tw.Label)) {
				++result.Key;
			}
			else if (lbl.NegNewLbl.isSource(tw.Label)) {
				++result.Value;
			}			
		}
		return result;
	}
	
	public static void setTermWeights(Meta meta, EFeatParamEmbedMetaSub param, 
			ArrayList<EToken> toknes) throws Exception {
//		HashSet<String> tagSet = new HashSet<>(
//				Arrays.asList(new String[] {"V", "N", "O", "S", "^", "Z", "L", "M"}));
		for (int ind = 0; ind < toknes.size(); ++ind) {
			EToken et = toknes.get(ind);
			double val = meta.missedIG;
			if (meta.allWordTbl.containsKey(et.Text)) {
				val = meta.allWordTbl.get(et.Text).IG;
			}
			else {
				EEmbedWord ew = param.Sps.GetWord(et.Text);
				if (ew != null) {
					int close = EEmbedWord.FindClosestCosine(meta.allWord, ew);
					EEmbedWord cew = meta.allWord.get(close);
					val = meta.allWordTbl.get(cew.Term).IG;
				}
			}
//			else {
//				EEmbedWord wd = param.Sps.GetWord(term);
//				if (wd != null) {
//					for (int cind = 0; cind < wd.repo.CloseWords.size(); ++cind) {
//						EEmbedWord cwd = wd.repo.CloseWords.get(cind);
//						if (meta.allWordTbl.containsKey(cwd.Term)) {
//							val = meta.allWordTbl.get(cwd.Term).IG;
//							break;
//						}
//					}
//				}
//			}
			if (Double.compare(val, 0) == 0) {
				val = meta.missedIG;
			}
			et.Weight = (float)val;
//			if (tagSet.contains(et.POS)) {
//				et.Weight *= 2;
//			}
		}
	}
	
	private ETweet convertWordToTweet(EEmbedWord word, String twId, int label) {
		ETweet result = new ETweet();
		result.Label = label;
		result.Tweetid = twId;
		Hashtable<String, Double> feats = EFeatEmbedMean.ConvertWordToTable(word);
		EFeatEmbedMean fem = new EFeatEmbedMean();
		result.Feats.put(fem.getFeatName(), feats);
		return result;
	}

	private EClusterer trainClusterOverTweets(EFeatParamEmbedMetaSub param,
			Meta meta, int clusterCount, boolean weighted) throws Exception {
		ArrayList<ETweet> tws = new ArrayList<>();
		for (int twind = 0; twind < param.Train.size(); ++twind) {
			ETweet tw = param.Train.get(twind);
			if (weighted) {
				setTermWeights(meta, param, tw.ETokens);
			}
			EEmbedWord cent = EEmbedWord.GetCentroid(param.Sps, tw, weighted);
			if (weighted) {
				tw.ResetWeights();
			}
			tws.add(convertWordToTweet(cent, twind + "", 0));
		}
		EClusterer clu = EElkiClusterer.Train(tws, new EClusterParam(clusterCount));
//		ETests.printClusStat(clu, tws);
		return clu;
	}
		
	private HashSet<String> getTrainSet(ArrayList<ETweet> tlist) {
		HashSet<String> result = new HashSet<>();
		for (int tind = 0; tind < tlist.size(); ++tind) {
			result.add(tlist.get(tind).Tweetid);
		}
		return result;
	}
	
	private Hashtable<String, Double> extractFeat(ETweet tw, 
			EFeatParamEmbedMetaSub param, Meta meta, HashSet<String> trainIDs) 
					throws Exception {
		Hashtable<String, EEmbedWordDensity> tbl = new Hashtable<>();
		for (int tInd = 0; tInd < tw.ETokens.size(); ++tInd) {
			EEmbedWord word = param.Sps.GetWord(tw.ETokens.get(tInd).Text);
			if (word != null) {	

				EEmbedWordDensity ewd = tbl.get(word.Term);
				if (ewd == null) {
					ewd = new EEmbedWordDensity(word, word.Term);
					tbl.put(word.Term, ewd);
				}
				++ewd.Count;
			}
		}
		Hashtable<String, Double> feats = new Hashtable<>();

		boolean forceCls = false;
		if (param.CoreStep == 1) {
			if (!trainIDs.contains(tw.Tweetid)) {
				forceCls = true;
			}
		}
		addTweetCluWithClassFeats(feats, tw, meta.TweetCluBiased, param, meta, 
				meta.CentClsBiased, param.CentPrThr, param.CentPrThr, "@texfb", true, forceCls);
		addTweetCluWithClassFeats(feats, tw, meta.TweetClu, param, meta, 
				meta.CentCls, param.CentPrThr, param.CentPrThr, "@texf", false, forceCls);
		addTreeFeats(feats, tw, param, meta, meta.Trees, "@trf");
		if (tw.Info != null && tw.Info.PreTweets.size() > 0) {
			addTweetCluWithClassFeats(feats, tw.Info.PreTweets.get(0), meta.TweetClu, param, meta, 
					meta.CentCls, param.CentPrThr, param.CentPrThr, "@pretexf", false, false);
		}
		if (tw.Info != null && tw.Info.NexTweets.size() > 0) {
			addTweetCluWithClassFeats(feats, tw.Info.NexTweets.get(0), meta.TweetClu, param, meta, 
					meta.CentCls, param.CentPrThr, param.CentPrThr, "@nextexf", false, false);
		}

		return feats;
	}
		
	private boolean addCentroidClass(Hashtable<String, Double> feats, ETweet tw, 
			EFeatParamEmbedMetaSub param, Meta meta, EClassifier ec, 
			float thresNeg, float thresPos, String prefix, boolean weighted, 
			boolean forceCls, String... posTags) throws Exception {
		ArrayList<EEmbedWord> ws = param.Sps.GetWords(tw, true, true, false, posTags);
		if (ws.size() == 0) {
			return false;
		}
		ETweet.StoreFeats(tw);
		if (weighted) {
			setTermWeights(meta, param, tw.ETokens);
		}
		EFeatEmbedMean fem = new EFeatEmbedMean();
		fem.addFeatSelective(tw, param, weighted, posTags);
		EPair<Integer, Double> lbl = EMalletClassifier.GetBestLabel(ec, tw);
		double prob = 0;
		if (ec.LblCon.PosNewLbl.NewLbl == lbl.Key) {
			prob = lbl.Value;
		}
		else {
			prob = 1 - lbl.Value;
		}
		boolean result = false;
		if (prob < 0.5 - thresNeg) {
			feats.put(prefix + "@n", 1d);
			result = true;
		}
		else if (0.5 + thresPos < prob) {
			feats.put(prefix + "@p", 1d);
			result = true;
		}
		else if (forceCls) {
			
			ArrayList<ArrayList<EToken>> sents = ETree.GetSentences(tw.ETokens, false);
			for (int sind = 0; sind < sents.size(); ++sind) {
				ArrayList<EToken> curs = sents.get(sind);
				ArrayList<EEmbedWord> corWs = new ArrayList<>();
				ArrayList<Float> corWe = new ArrayList<>();
				for (int wind = 0; wind < curs.size(); ++wind) {
					EEmbedWord ew = param.Sps.GetWord(curs.get(wind).Text);
					if (ew != null) {
						corWs.add(ew);
						corWe.add(curs.get(wind).Weight);
					}
				}
				
				if (corWs.size() > 1) {
					EEmbedWord cent = null;
					if (weighted) {
						cent = EEmbedWord.GetWeightedAverage(corWs, corWe);
					}
					else {
						cent = EEmbedWord.GetAverage(corWs);
					}
					ETweet wtw = convertWordToTweet(cent, "000", 0);
					lbl = EMalletClassifier.GetBestLabel(ec, wtw);
					double corProb = 0;
					if (ec.LblCon.PosNewLbl.NewLbl == lbl.Key) {
						corProb = lbl.Value;
					}
					else {
						corProb = 1 - lbl.Value;
					}
//					boolean set = false;
					if (0.5 + thresPos < corProb) {
						feats.put(prefix + "@p", 1d);
						result = true;
//						set = true;
					}
//					if (set) {
//						System.out.print("\\");
//					}
//					if (param.LblCon.PosNewLbl.isSource(tw.Label)) {
//						System.out.print("+");
//					}
//					else {
//						System.out.print("-");
//					}
				}
			}

		}
		if (weighted) {
			tw.ResetWeights();
		}
		ETweet.RestoreFeats(tw);
		return result;
	}

	private void addTweetCluWithClassFeats(Hashtable<String, Double> feats, ETweet tw, 
			EClusterer nullableClu, EFeatParamEmbedMetaSub param, 
			Meta meta, EClassifier ec, float thresNeg, float thresPos, String prefix, 
			boolean weighted, boolean forceCls) throws Exception {
		String newPrefix = prefix;
		if (nullableClu != null) {
			if (weighted) {
				setTermWeights(meta, param, tw.ETokens);
			}
			EEmbedWord cent = EEmbedWord.GetCentroid(param.Sps, tw, weighted);
			if (weighted) {
				tw.ResetWeights();
			}
			ETweet centTw = convertWordToTweet(cent, "temp", 0);
			EPair<Integer, Double> lbl = EElkiClusterer.GetBestLabel(nullableClu, centTw);
			newPrefix = prefix + "@" + lbl.Key;
		}
		addCentroidClass(feats, tw, param, meta, ec, thresNeg, thresPos, newPrefix, weighted, forceCls);
	}
		
	private void addTreeFeats(Hashtable<String, Double> feats, ETweet tw, 
			EFeatParamEmbedMetaSub param, Meta meta, ArrayList<ETree> trs, 
			String prefix) throws Exception {
		ArrayList<ETree> existing = EFREQT.findTrees(tw, trs, false);
		for (int eind = 0; eind < existing.size(); ++eind) {
			String key = existing.get(eind).SExp;
			feats.put(prefix + "@" + key, 1d);
		}
	}
		
}
