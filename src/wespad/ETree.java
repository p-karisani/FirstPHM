package wespad;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class ETree {

	public String SExp;
	public int TweetCount;
	public float Value;
	public boolean IsPositive;
	HashSet<String> ExistingTweetIds = new HashSet<>();
	EPair<Double, Double> Existing = new EPair<Double, Double>(0d, 0d);
	EPair<Double, Double> Nonexisting = new EPair<Double, Double>(0d, 0d);
	ArrayList<EToken> AbstractTokens = new ArrayList<>();
	HashSet<String> subTrees = new HashSet<>();
	
	public ETree(ArrayList<EToken> tokens) {
		SExp = GetTreeText(tokens, "(", ")", false);
		AbstractTokens = tokens;
	}
	
	public ETree(String sexp) {
		SExp = sexp;
		AbstractTokens = getAbstractTokens(sexp);
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(
				String.format("%-17s",  "[T" + TweetCount + ", V" + String.format("%.4f", Value) + "]") 
				+ String.format("%-20s",  "[EP"+ String.format("%-5s",  Existing.Key.intValue()) +
															", EN" + String.format("%-5s",  Existing.Value.intValue()) + "]")
				+ String.format("%-20s",  "[NP" + String.format("%-5s",  Nonexisting.Key.intValue()) + 
															", NN" + String.format("%-5s",  Nonexisting.Value.intValue()) + "] ")
		);
//		if (IsPositive) {
//			result.append("True\t\t");
//		}
//		else {
//			result.append("False\t\t");
//		}
		result.append(SExp);
		return result.toString();
	}
	
	public static String GetTreeText(ETweet tw) throws Exception {
		String result = GetTreeText(tw, "(", ")", false);
		return result;
	}
	
	private static String GetTreeText(ETweet tw, String stTag, String enTag, boolean printPOS) throws Exception {
		ArrayList<ArrayList<EToken>> sents = GetSentences(tw.ETokens, false);
		StringBuilder result = new StringBuilder();
		result.append(stTag);
		result.append(EVar.SynTreeRoot);
		for (int sind = 0; sind < sents.size(); ++sind) {
			ArrayList<EToken> sent = sents.get(sind);
			if (sent.get(0).RootValue != -1) {
				String stext = GetTreeText(sent, stTag, enTag, printPOS);
				result.append(stext);
				result.append(" ");
			}
		}
		result.append(enTag);
		return result.toString();
	}
	
	public static EToken GetRootFirst(ArrayList<EToken> tokens) {
		ArrayList<EToken> result = GetRoots(tokens);
		for (int rind = 0; rind < result.size(); ++rind) {
			EToken r = result.get(rind);
			if (r.RootValue != -1) {
				return r;
			}
		}
		return null;
	}
	
	public static ArrayList<EToken> GetRoots(ArrayList<EToken> tokens) {
		ArrayList<EToken> result = new ArrayList<>();
		HashSet<EToken> nodes = new HashSet<>();
		for (int ind = 0; ind < tokens.size(); ++ind) {
			nodes.add(tokens.get(ind));
		}
		for (int tind = 0; tind < tokens.size(); ++tind) {
			EToken cur = tokens.get(tind);
			if (cur.Root == null || !nodes.contains(cur.Root)) {
				result.add(cur);
			}
		}
		return result;
	}
	
	private static ArrayList<EToken> getAllChildren(EToken root) {
		ArrayList<EToken> result = new ArrayList<>();
		Queue<EToken> list = new LinkedList<>();
		list.add(root);
		while (!list.isEmpty()) {
			EToken cur = list.poll();
			result.add(cur);
			for (int ind = 0; ind < cur.Children.size(); ++ind) {
				list.add(cur.Children.get(ind));
			}
		}
		return result;
	}

	public static ArrayList<ArrayList<EToken>> GetSentences(
			ArrayList<EToken> tokens, boolean returnTreelessNodes) {
		ArrayList<EToken> roots = GetRoots(tokens);
		ArrayList<ArrayList<EToken>> result = new ArrayList<>();
		for (int rind = 0; rind < roots.size(); ++rind) {
			EToken curRoot = roots.get(rind);
			if (curRoot.RootValue != -1 || returnTreelessNodes) {
				ArrayList<EToken> cur = getAllChildren(curRoot);
				cur.sort((r, l) -> Integer.compare(r.Order, l.Order));
				result.add(cur);
			}
		}
		return result;
	}
	
	private static String GetTreeText(ArrayList<EToken> tokens, 
			String stTag, String enTag, boolean printPOS) {
//		for (int tind = 0; tind < tokens.size(); ++tind) {
//			EToken cur = tokens.get(tind);
//			if (cur.RootValue == 0) {
//				String text = GetTreeText(cur);
//				return text;
//			}
//		}

		EToken root = GetRootFirst(tokens);
		if (root != null) {
			String text = GetTreeText(root, stTag, enTag, printPOS);
			return text;
		}
		return null;
	}
	
	private static String GetTreeText(EToken tok, 
			String stTag, String enTag, boolean printPOS) {
		StringBuilder result = new StringBuilder();
		result.append(stTag);
		if (printPOS) {
			result.append(tok.POS + " ");
		}
		result.append(tok.Text);
		for (int cind = 0; cind < tok.Children.size(); ++cind) {
			String ctext = GetTreeText(tok.Children.get(cind), stTag, enTag, printPOS);
			result.append(ctext);
		}
		result.append(enTag);
		return result.toString();
	}

	private static String getTokenName(String sexp) {
		StringBuilder result = new StringBuilder();
		int cind = 1;
		while (cind < sexp.length()) {
			char curch = sexp.charAt(cind);
			if (curch == '(' || curch == ')') {
				break;
			}
			result.append(curch);
			++cind;
		}
		return result.toString().trim();
	}
	
	private static ArrayList<String> getChildren(String sexp) {
		ArrayList<String> result = new ArrayList<>();
		int parCount = 0;
		int stPos = 0;
		int pos = 1;
		while (pos < sexp.length()) {
			char curc = sexp.charAt(pos);
			if (curc == '(') {
				++parCount;
			}
			else if (curc == ')') {
				--parCount;
			}
			if (curc == '(' && parCount == 1) {
				stPos = pos;
			}
			else if (curc == ')' && parCount == 0) {
				String childStr = sexp.substring(stPos, pos + 1);
				result.add(childStr);
			}
			++pos;
		}
		return result;
	}

	public static ArrayList<EToken> getAbstractTokens(String sexp) {
		ArrayList<EToken> result = new ArrayList<>();
		EToken root = new EToken();
		root.Text = getTokenName(sexp);
		result.add(root);
		ArrayList<String> childExp = getChildren(sexp);
		for (int cind = 0; cind < childExp.size(); ++cind) {
			String chStr = childExp.get(cind);
			ArrayList<EToken> chts = getAbstractTokens(chStr);
			chts.get(0).Root = root;
			root.Children.add(chts.get(0));
			result.addAll(chts);
		}
		return result;
	}
	
}
