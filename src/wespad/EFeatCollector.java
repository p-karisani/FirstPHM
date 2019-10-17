package wespad;

public class EFeatCollector {
		
	public static void addEmbedMeanFeatures(EContext econ, 
			EFeatParam param, boolean weighted, String... posTags) 
					throws Exception {
		EFeatEmbedMean embed = new EFeatEmbedMean();
		embed.addFeatSelective(econ, param, weighted, posTags);
	}
		
	public static void addEmbedMetaFeatures(EContext econ, 
			EFeatParam param, boolean useFastGram) throws Exception {
		EFeat1Gram gram1 = new EFeat1Gram();
		gram1.addFeat(econ, param);
		EFeat2Gram gram2 = new EFeat2Gram();
		gram2.addFeat(econ, param);
		EFeatEmbedMeta embed = new EFeatEmbedMeta();
		embed.addFeat(econ, param);
	}
	
}
