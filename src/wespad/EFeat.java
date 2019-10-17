package wespad;

public abstract class EFeat {
	
	abstract void addFeat(EContext econ, EFeatParam param) throws Exception;
	abstract void addFeat(ETweet tw, EFeatParam param) throws Exception;
	
	public String getFeatName() {
		return this.getClass().getSimpleName();
	}

}
