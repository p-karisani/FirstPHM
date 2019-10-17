package wespad;

import java.util.ArrayList;

public class EFeatParam {
	public int Index;
	public String CacheId;
	public ELblConf LblCon;
	public ArrayList<ETweet> Train;
	public ArrayList<ETweet> Test;
	public EEmbedSpace Sps;
	
	public EFeatParam(int index) {
		Index = index;
	}
	
	@Override
	public String toString() {
		return "";
	}
	
}
