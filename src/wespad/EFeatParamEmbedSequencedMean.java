package wespad;

import java.util.ArrayList;

public class EFeatParamEmbedSequencedMean extends EFeatParam {
	
	public ArrayList<ESequence> Seqs = new ArrayList<>();
	public float SeqBucSize;
	
	public EFeatParamEmbedSequencedMean(int index) {
		super(index);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int ind = 0; ind < Seqs.size(); ++ind) {
			result.append(Seqs.get(ind) + "\n");
		}
		return result.toString();
	}

}
