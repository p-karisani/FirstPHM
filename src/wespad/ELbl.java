package wespad;

import org.apache.commons.lang3.ArrayUtils;

public class ELbl {
	public int NewLbl;
	public int[] SourceLbls;
	
	public ELbl(int newLabel, int ... sourceLbls) {
		NewLbl = newLabel;
		SourceLbls = sourceLbls;
	}
	
	public boolean isSource(int lbl) {
		return ArrayUtils.contains(SourceLbls, lbl);
	}

}
