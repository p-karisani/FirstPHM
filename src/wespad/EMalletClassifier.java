package wespad;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;

import cc.mallet.classify.AdaBoostM2Trainer;
import cc.mallet.classify.C45Trainer;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.DecisionTreeTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.NaiveBayesEMTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.classify.Trial;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.Labeling;

public class EMalletClassifier {

	public static enum CLSType {
		Bayes,
		Logistic,
		EM,
		C45,
		AdaBoost
	}
	
	private static Instance getIns(ETweet tw, ELblConf lc, Alphabet alp, String preId) throws Exception {
		ArrayList<String> feats = new ArrayList<>();
		String[] gs = tw.Feats.keySet().toArray(new String[0]);
		for (int gInd = 0; gInd < gs.length; ++gInd) {
			String[] fs = tw.Feats.get(gs[gInd]).keySet().toArray(new String[0]);
			for (int fInd = 0; fInd < fs.length; ++fInd) {
				String featName = gs[gInd] + "$" + fs[fInd];
				feats.add(featName);
			}
		}
		alp.lookupIndices(feats.toArray(new String[0]), true);
		double[] vals = new double[alp.size()];
		for (int gInd = 0; gInd < gs.length; ++gInd) {
			String[] fs = tw.Feats.get(gs[gInd]).keySet().toArray(new String[0]);
			for (int fInd = 0; fInd < fs.length; ++fInd) {
				double fv = tw.Feats.get(gs[gInd]).get(fs[fInd]);
				String featName = gs[gInd] + "$" + fs[fInd];
				int alpInd = alp.lookupIndex(featName);
				vals[alpInd] = fv;
			}
		}
		String lbl = lc.getCorrectNewLabel(tw.Label) + "";
		Instance result = new Instance(new FeatureVector(alp, vals), 
				lbl, preId + tw.Tweetid, null);
		return result;
	}

	public static void addToInstanceList(ArrayList<ETweet> list, 
			ELblConf lc, InstanceList result, boolean removeLabel) throws Exception {
		for (int tInd = 0; tInd < list.size(); ++tInd) {
			ETweet tw = list.get(tInd);
			if (removeLabel) {
				Instance ins = getIns(tw, lc, result.getAlphabet(), tInd + "$");
				result.addThruPipe(ins);
				ins.unLock();
				ins.setLabeling(null);
				ins.lock();
			}
			else {
				Instance ins = getIns(tw, lc, result.getAlphabet(), "");
				result.addThruPipe(ins);
			}
		}
	}
	
	public static Instance getInstance(ETweet tw, ELblConf lc, Pipe sp) throws Exception {
		ArrayList<ETweet> list = new ArrayList<>();
		list.add(tw);
		InstanceList il = new InstanceList(sp);
		addToInstanceList(list, lc, il, false);
		return il.get(0);
	}
	
	public static InstanceList getInstanceList(ArrayList<ETweet> list, 
			ELblConf lc) throws Exception {
		ArrayList<Pipe> pList = new ArrayList<Pipe>();
		Alphabet alp = new Alphabet();
		LabelAlphabet lalp = new LabelAlphabet();
		Target2Label t2l = new Target2Label(alp, lalp);
		pList.add(t2l);
		SerialPipes sp = new SerialPipes(pList);
		InstanceList result = new InstanceList(sp);
		addToInstanceList(list, lc, result, false);
		return result;
	}
	
	public static EClassifier Train(ArrayList<ETweet> trainWithLoadedFeatures,
			ELblConf lc, CLSType type) throws Exception {
		InstanceList traList = getInstanceList(trainWithLoadedFeatures, lc);
		Classifier cls = null;

		PrintStream outPS = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream temp = new PrintStream(baos);
		System.setOut(temp);
		if (type == CLSType.Bayes) {
			NaiveBayesTrainer trainer = new NaiveBayesTrainer();
			cls = trainer.train(traList);
		}
		else if (type == CLSType.Logistic) {
			MaxEntTrainer trainer = new MaxEntTrainer();
//			trainer.setL1Weight(0.1);
			cls = trainer.train(traList);			
		}
		else if (type == CLSType.C45) {
			C45Trainer trainer = new C45Trainer();
			cls = trainer.train(traList);
		}
		else if (type == CLSType.AdaBoost) {
			AdaBoostM2Trainer trainer = new AdaBoostM2Trainer(new DecisionTreeTrainer());
			cls = trainer.train(traList);
		}
		System.setOut(outPS);
		temp.close();

//		Optimizer opt = trainer.getOptimizer();
		EClassifier result = new EClassifier(cls, lc);
		return result;
	}
	
	public static EClassifier TrainEM(ArrayList<ETweet> trainWithLoadedFeatures,
			ArrayList<ETweet> testWithLoadedFeatures, 
			ArrayList<ETweet> unlabeledWithLoadedFeaturesNullable, 
			ELblConf lc) throws Exception {
		InstanceList traList = getInstanceList(trainWithLoadedFeatures, lc);
//		addToInstanceList(testWithLoadedFeatures, lc, traList, true);
		if (unlabeledWithLoadedFeaturesNullable != null) {
			addToInstanceList(unlabeledWithLoadedFeaturesNullable, lc, traList, true);
		}
		NaiveBayesEMTrainer trainer = new NaiveBayesEMTrainer();
//		trainer.setUnlabeledDataWeight(0.3d);
		Classifier cls = trainer.train(traList);
		EClassifier result = new EClassifier(cls, lc);
		return result;
	}
	
	public static ArrayList<EPair<Integer, Double>>  GetLabels(
			EClassifier cls, ETweet twWithLoadedFeatures) throws Exception {
		Instance ins = getInstance(twWithLoadedFeatures, 
				cls.LblCon, cls.Cls.getInstancePipe());
		Classification cs = cls.Cls.classify(ins);
		Labeling lbling = cs.getLabeling();
		ArrayList<EPair<Integer, Double>>  result = new ArrayList<>();
		for (int ind = 0; ind < lbling.numLocations(); ++ind) {
			Integer lbl = Integer.parseInt(
					lbling.getLabelAtRank(ind).toString());
			Double val = lbling.getValueAtRank(ind);
			result.add(new EPair<Integer, Double>(lbl, val));
		}
		return result;
	}
	
	public static EPair<Integer, Double>  GetBestLabel(EClassifier cls, 
			ETweet twWithLoadedFeatures) throws Exception {
		ArrayList<EPair<Integer, Double>> list = GetLabels(cls, twWithLoadedFeatures);
		return list.get(0);
	}
	
	public static void Label(EClassifier cls,
			ArrayList<ETweet> testWithLoadedFeatures,
			EClassifier nullableBackoffCls) throws Exception {
		InstanceList tesList = new InstanceList(cls.Cls.getInstancePipe());
        addToInstanceList(testWithLoadedFeatures, cls.LblCon, tesList, false);
        Trial result = new Trial(cls.Cls, tesList);
        Hashtable<String, ETweet> tbl = ETweet.getAsTable(
        		testWithLoadedFeatures);
        for (int ind = 0; ind < tesList.size(); ++ind) {
        	Classification cs = result.get(ind);
        	ETweet tw = tbl.get(cs.getInstance().getName().toString());
        	int lbl = Integer.parseInt(
        			cs.getLabeling().getBestLabel().toString());
        	if (nullableBackoffCls != null) {
        		tw.NewLabel = GetBestLabel(nullableBackoffCls, tw).Key;
        		if (nullableBackoffCls.LblCon.NegNewLbl.NewLbl == 
        				tw.NewLabel) {
        			tw.NewLabel = cls.LblCon.NegNewLbl.NewLbl;
        		}
        		else {
        			tw.NewLabel = lbl;
        		}
        	}
        	else {
        		tw.NewLabel = lbl;
        	}
        }
	}
	
	public static void Label(ArrayList<ETweet> trainWithLoadedFeatures, 
			ArrayList<ETweet> testWithLoadedFeatures, ELblConf lc, CLSType type, 
			ArrayList<ETweet> unlabeledWithLoadedFeaturesNullable) throws Exception {
		EClassifier ec = Train(trainWithLoadedFeatures, lc, type);
		Label(ec, testWithLoadedFeatures, null);
	}

}
