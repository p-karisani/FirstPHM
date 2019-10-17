package wespad;

import java.util.ArrayList;

public class EFeatParamEmbedMetaSub extends EFeatParam {

	static int[] TweetClusterBiasedValues = new int[] 
			{4};
	static int[] TweetClusterValues = new int[] 
			{3};
	static float[] CentPrThrValues = new float[] 
			{0.15f};
	
	public float CentPrThr;
	public int TweetClusterBiased;
	public int TweetCluster;
	public int CoreStep = 0;


	public EFeatParamEmbedMetaSub(int index, float _centPrThr, 
			int _tweetClusterBiased, int _tweetCluster) {
		super(index);
		CentPrThr = _centPrThr;
		TweetClusterBiased = _tweetClusterBiased;
		TweetCluster = _tweetCluster;
	}

	@Override
	public String toString() {
		return ""
				+ " TwCluB: " + TweetClusterBiased
				+ " TwClu: " + TweetCluster
				+ " CentPT: " + CentPrThr	
				;
	}
	
	public EFeatParamEmbedMetaSub Copy(int index) {
		return new EFeatParamEmbedMetaSub(index, 
				this.CentPrThr, this.TweetClusterBiased, 
				this.TweetCluster);
	}
	
	private static int CentPrThrind;
	private static int TweetClusterBiasedind;
	private static int TweetClusterind;

	public static ArrayList<EFeatParamEmbedMetaSub> GetParams() {
		ArrayList<EFeatParamEmbedMetaSub> result = new ArrayList<>();
		for (TweetClusterBiasedind = 0; TweetClusterBiasedind < TweetClusterBiasedValues.length; ++TweetClusterBiasedind) {
		for (TweetClusterind = 0; TweetClusterind < TweetClusterValues.length; ++TweetClusterind) {
		for (CentPrThrind = 0; CentPrThrind < CentPrThrValues.length; ++CentPrThrind) {
			addParam(result);
		}}}
		return result;
	}

	private static void addParam(ArrayList<EFeatParamEmbedMetaSub> result) {
		result.add(new EFeatParamEmbedMetaSub(0,
				CentPrThrValues[CentPrThrind],
				TweetClusterBiasedValues[TweetClusterBiasedind],
				TweetClusterValues[TweetClusterind]
		));	
	}
	
}
