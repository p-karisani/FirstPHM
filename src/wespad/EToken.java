package wespad;

import java.util.ArrayList;
import java.util.Hashtable;

public class EToken {
	public boolean HideText = false;
	public String TagLine = null;
	public short Order;
	public String Text;
	public String POS;
	public float Weight;
	public EToken Root;
	public short RootValue;
	public ArrayList<EToken> Children = new ArrayList<>();
	public boolean IsSynthesized;
	public boolean IsHuman;
	public boolean IsPositiveHuman;
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (this.Root != null) {
			result.append(this.Text + " [" + this.POS + ", " + this.Children.size() + ", " + this.Root.Text + "]");
		}
		else {
			result.append(this.Text + " [" + this.POS + ", " + this.Children.size()  + "]");
		}
		return result.toString();
//		
//		String rootinfo = "";
//		if (Root != null) {
//			rootinfo = "[r:" + Root.Text + ", childs:" + Children.size() + "]";
//		}
//		if (Float.compare(Weight, 1) == 0) {
//			return Text + " (" + POS + ")" + rootinfo;
//		}
//		else {
//			return Text + " (" + POS + ")" + " (" + Weight + ")" + rootinfo;
//		}
	}

	public static ArrayList<EToken> getCopy(ArrayList<EToken> list) {
		Hashtable<EToken, EToken> cops = new Hashtable<>();
		ArrayList<EToken> result = new ArrayList<>();
		for (int ind = 0; ind < list.size(); ++ind) {
			EToken cp = new EToken();
			cops.put(list.get(ind), cp);
			result.add(cp);
		}
		for (int ind = 0; ind < list.size(); ++ind) {
			EToken cp = result.get(ind);
			EToken org = list.get(ind);
			cp.TagLine = org.TagLine;
			cp.HideText = org.HideText;
			cp.Order = org.Order;
			cp.Text = org.Text;
			cp.POS = org.POS;
			cp.Weight = org.Weight;
			cp.IsSynthesized = org.IsSynthesized;
			cp.IsHuman = org.IsHuman;
			cp.IsPositiveHuman = org.IsPositiveHuman;
			if (org.Root != null) {
				cp.Root = cops.get(org.Root);
			}
			for (int cind = 0; cind < org.Children.size(); ++cind) {
				cp.Children.add(cops.get(org.Children.get(cind)));
			}
		}
		return result;
	}
	
}
