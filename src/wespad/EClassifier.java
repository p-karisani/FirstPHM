package wespad;

public class EClassifier {
	public cc.mallet.classify.Classifier Cls;
	public ELblConf LblCon;

	public EClassifier(cc.mallet.classify.Classifier cls, ELblConf lblCon) {
		Cls = cls;
		LblCon = lblCon;
	}

}
