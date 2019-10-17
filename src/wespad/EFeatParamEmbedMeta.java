package wespad;

import java.util.ArrayList;

public class EFeatParamEmbedMeta extends EFeatParam {
	
	EFeatParamEmbedMetaSub[] subs;
	
	public EFeatParamEmbedMeta(int index, EFeatParamEmbedMetaSub... _subs) {
		super(index);
		subs = _subs;
	}

	public void setInfo(ELblConf lblCon, ArrayList<ETweet> train, 
			ArrayList<ETweet> test, EEmbedSpace[] spaces) {
		this.LblCon = lblCon;
		this.Train = train;
		this.Test = test;
		for (int ind = 0; ind < subs.length; ++ind) {
			subs[ind].LblCon = lblCon;
			subs[ind].Train = train;
			subs[ind].Test = test;
			subs[ind].Sps = spaces[ind];
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int ind = 0; ind < subs.length; ++ind) {
			result.append(subs[ind] + " / ");
		}
		return result.toString();
	}

	public static ArrayList<EFeatParamEmbedMeta> GetParams(int count) {
		ArrayList<EFeatParamEmbedMeta> result = new ArrayList<>();
		int index = 0;
		ArrayList<EFeatParamEmbedMetaSub> subs = EFeatParamEmbedMetaSub.GetParams();
		if (count == 1) {
			for (int find = 0; find < subs.size(); ++find) {
				EFeatParamEmbedMetaSub first = subs.get(find).Copy(index);
				EFeatParamEmbedMeta item = new EFeatParamEmbedMeta(index, first);
				result.add(item);
			}
		}
		return result;
	}

}
