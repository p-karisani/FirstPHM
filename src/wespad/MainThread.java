package wespad;

import java.util.ArrayList;

import wespad.EMalletClassifier.CLSType;

public class MainThread {

	public static void main(String[] args) throws Exception {
		ELib.println("Started at " + ELib.getTime());

		ArrayList<ETweet> list = ETweet.load(EVar.Results + "tweets.txt", ETagLoadType.StoredTags, true);
		ArrayList<ETweet> train = new ArrayList<>(list.subList(0, 70));
		ArrayList<ETweet> test = new ArrayList<>(list.subList(70, 100));

		EContext econ = new EContext(0, list, null);
		ELblConf lc = new ELblConf(0, 1, 
				new ELbl(0, EVar.LblNonEventHealth),
				new ELbl(1, EVar.LblEventHealth));
		EEmbedSpace SPS = new EEmbedSpace(EVar.Embedword2vec300Extracted, true);
		EEmbedSpace[] spsList = new EEmbedSpace[] {SPS};
		EFeatParamEmbedMeta param = EFeatParamEmbedMeta.GetParams(spsList.length).get(0);

		((EFeatParamEmbedMeta)param).setInfo(lc, train, test, spsList);
		param.CacheId = null;
		EFeatCollector.addEmbedMetaFeatures(econ, param, false);
		EMalletClassifier.Label(param.Train, param.Test, lc, CLSType.Bayes, null);

		for (int tind = 0; tind < test.size(); ++tind) {
			System.out.println(
					"tweet id: " + test.get(tind).Tweetid + "\t|\t" + 
					"original label: " + test.get(tind).Label + "\t|\t" +
					"prediction: " + test.get(tind).NewLabel);
		}

		ELib.println("Done at " + ELib.getTime());
	}

}


