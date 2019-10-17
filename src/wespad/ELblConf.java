package wespad;

public class ELblConf {
	public ELbl[] Labels;
	public ELbl NegNewLbl;
	public ELbl PosNewLbl;
	
	public ELblConf(
			Integer nullableNegativeNewLabel, 
			Integer nullablePositiveNewLabel, 
			ELbl ... labels) {
		Labels = labels;
		if (nullableNegativeNewLabel != null) {
			for (int ind = 0; ind < labels.length; ++ind) {
				if (labels[ind].NewLbl == nullableNegativeNewLabel) {
					NegNewLbl = labels[ind];
					break;
				}
			}
		}
		if (nullablePositiveNewLabel != null) {
			for (int ind = 0; ind < labels.length; ++ind) {
				if (labels[ind].NewLbl == nullablePositiveNewLabel) {
					PosNewLbl = labels[ind];
					break;
				}
			}
		}
	}
	
	public int getCorrectNewLabel(int label) throws Exception {
		int ind = 0;
		while (ind < Labels.length) {
			if (Labels[ind].isSource(label)) {
				return Labels[ind].NewLbl;
			}
			else {
				++ind;
			}
		}
		ELib.printlnForce("Unknown label to map!");
		return -10;
	}
	
	public int getNewLabelCount() {
		return Labels.length;
	}
	
}
