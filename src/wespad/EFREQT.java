package wespad;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class EFREQT {
	
	public static ArrayList<ETree> getTreesFREQT(ELblConf nullableLbl, 
			ArrayList<ETweet> tws,   int minLen, int maxLen, int minSupp, 
			float minIG, String nullableKeyword) throws Exception {
		ArrayList<ETweet> ctws = EDump.RemoveRepeatedandShorts(tws);
		String outp = runFREQT(ctws, minLen, minSupp);
		ArrayList<ETree> trees = getTrees(nullableLbl, ctws, outp, minIG);
		trees = filter(trees, maxLen, nullableKeyword);
		return trees;
	}

	public static ArrayList<ETree> filter(ArrayList<ETree> trees, int maxLength, 
			String nullableKeyword) {
		ArrayList<ETree> result = new ArrayList<>();
		for (int trind = 0; trind < trees.size(); ++trind) {
			ETree tr = trees.get(trind);
			if (tr.AbstractTokens.size() <= maxLength) {
				for (int toind = 0; toind < tr.AbstractTokens.size(); ++toind) {
					EToken to = tr.AbstractTokens.get(toind);
					if (nullableKeyword == null || to.Text.startsWith(nullableKeyword)) {
						result.add(tr);
						break;
					}
				}
			}
		}
		return result;
	}
		
	private static String runFREQT(ArrayList<ETweet> tws,   
			int minLen, int minSupp) throws Exception {
		String inFile = EVar.CurrentPath + "freqt-inp.txt";
		PrintStream ps = new PrintStream(inFile);
		for (int twind = 0; twind < tws.size(); ++twind) {
			ps.println(ETree.GetTreeText(tws.get(twind)));
		}
		ps.close();
		ProcessBuilder freqtProcConfig = new ProcessBuilder(
				"/bin/bash", "-c", EVar.FREQTPath + 
				" -m " + minSupp + " -M " + minLen + " -w < \"" + inFile + "\"");
		freqtProcConfig.redirectErrorStream(true);
		Process freqtProc = freqtProcConfig.start();		
		BufferedReader stdIn = new BufferedReader(
				new InputStreamReader(freqtProc.getInputStream()));
		StringBuilder result = new StringBuilder();
		String line = null;
		while ((line = stdIn.readLine()) != null){
			result.append(line + "\n");
		}
		stdIn.close();		
		Files.delete(Paths.get(inFile));
		return result.toString();
	}
	
	private static ArrayList<ETree> getTrees(ELblConf lbl, 
			ArrayList<ETweet> tws, String xml, float minIG) throws Exception {
		xml = xml.replace("&", "&amp;");
	    InputSource is = new InputSource();
	    is.setCharacterStream(new StringReader(xml));
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//	    dbf.setValidating(false);
//	    dbf.setNamespaceAware(true);
//	    dbf.setFeature("http://xml.org/sax/features/namespaces", false);
//	    dbf.setFeature("http://xml.org/sax/features/validation", false);
//	    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
//	    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder xmlDB = dbf.newDocumentBuilder();
		Document doc = xmlDB.parse(is);
		NodeList nl = doc.getFirstChild().getChildNodes();
		ArrayList<ETree> result = getTrees(lbl, tws, nl);
		int tind = 0;
		while (tind < result.size()) {
			if (result.get(tind).Value < minIG) {
				result.remove(tind);
			}
			else {
				++tind;
			}
		}
		fillSubTrees(result);
		result.sort((ll, rr) -> -1 * Float.compare(ll.Value, rr.Value));
		return result;
	}
	
	private static void fillSubTrees(ArrayList<ETree> trs) throws Exception {
		for (int paind = 0; paind < trs.size(); ++paind) {
			ETree patr = trs.get(paind);
			for (int chind = 0; chind < trs.size(); ++chind) {
				ETree chtr = trs.get(chind);
				if (paind != chind && !patr.subTrees.contains(chtr.SExp)) {
					boolean check = exists(patr.AbstractTokens, chtr.AbstractTokens, false);
					if (check) {
						patr.subTrees.add(chtr.SExp);
						patr.subTrees.addAll(chtr.subTrees);
					}
				}
			}
		}
	}
	
	private static Node getNodeByName(Node nd, String name) {
		NodeList nl = nd.getChildNodes();
		for (int nind = 0; nind < nl.getLength(); ++nind) {
			if (nl.item(nind).getNodeName().equals(name)) {
				return nl.item(nind);
			}
		}
		return null;
	}
	
	private static HashSet<Integer> getTweetIdsByLabel(
			ArrayList<ETweet> tws, ELblConf lbl, int newlbl) throws Exception {
		HashSet<Integer> result = new HashSet<>();
		for (int twind = 0; twind < tws.size(); ++twind) {
			if (lbl.getCorrectNewLabel(tws.get(twind).Label) == newlbl) {
				result.add(twind);
			}
		}
		return result;
	}
	
	private static ArrayList<Integer> getExistingIds(String line) {
		String[] ids = line.split(" ");
		ArrayList<Integer> result = new ArrayList<>();
		for (int ind = 0; ind < ids.length; ++ind) {
			result.add(Integer.parseInt(ids[ind]));
		}
		return result;
	}
	
	private static ArrayList<ETree> getTrees(ELblConf lbl, 
			ArrayList<ETweet> tws, NodeList nl) throws Exception {
		EPair<Double, Double> counts = null;
		HashSet<Integer> posIds = null;
		HashSet<Integer> negIds = null;
		if (lbl != null) {
			counts = ELib.getPosNegCount(tws, lbl);
			posIds = getTweetIdsByLabel(tws, lbl, lbl.PosNewLbl.NewLbl);
			negIds = getTweetIdsByLabel(tws, lbl, lbl.NegNewLbl.NewLbl);
		}
		ArrayList<ETree> result = new ArrayList<>();
		for (int nind = 0; nind < nl.getLength(); ++nind) {
			Node nd = nl.item(nind);
			if (nd.getNodeType() == Node.ELEMENT_NODE) {
				String text = getNodeByName(nd, "what").getTextContent();
				ETree tr = new ETree(text);
				result.add(tr);
				tr.TweetCount = Integer.parseInt(getNodeByName(nd, "support").getTextContent());
				ArrayList<Integer> exist = getExistingIds(getNodeByName(nd, "where").getTextContent());
				for (int idind = 0; idind < exist.size(); ++idind) {
					tr.ExistingTweetIds.add(tws.get(exist.get(idind)).Tweetid);
				}
				if (lbl != null) {
					for (int idind = 0; idind < exist.size(); ++idind) {
						if (posIds.contains(exist.get(idind))) {
							++tr.Existing.Key;
						}
						else {
							++tr.Existing.Value;
						}
					}
					tr.Nonexisting.Key = posIds.size() - tr.Existing.Key;
					tr.Nonexisting.Value = negIds.size() - tr.Existing.Value;
					tr.IsPositive = tr.Existing.Key > tr.Existing.Value ? true : false;
					double ig = ELib.GetIG(counts, tr.Existing, tr.Nonexisting);
					tr.Value = (float)ig;					
				}
			}
		}
		return result;
	}
	
	private static boolean compareTokens(EToken textToken, 
			EToken patternToken) {
		if (patternToken.HideText) {
			if (patternToken.POS.equals(textToken.POS)) {
				return true;
			}
		}
		else {
			if (patternToken.Text.equals(textToken.Text)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isSubTree(EToken mainAbs, EToken childAbs) {
		ArrayList<EToken> comaChildren = new ArrayList<>(mainAbs.Children);
		for (int chchind = 0; chchind < childAbs.Children.size(); ++chchind) {
			int maind = 0;
			EToken chchT = childAbs.Children.get(chchind);
			while (maind < comaChildren.size()) {
				if (compareTokens(comaChildren.get(maind), chchT)) {
					boolean check = isSubTree(comaChildren.get(maind), chchT);
					if (check) {
						break;
					}
				}
				++maind;
			}
			if (maind == comaChildren.size()) {
				return false;
			}
			comaChildren.remove(maind);			
		}
		return true;
	}
	
	private static void addRootToken(ArrayList<EToken> mainAbstractTree) {
//		this method doesn't fully revise the tree (RootValues and TokenOrders are left without change). 
//		It only works for finding subtrees
		EToken root = new EToken();
		root.Text = EVar.SynTreeRoot;
		root.RootValue = 0;
		ArrayList<EToken> subRoots = ETree.GetRoots(mainAbstractTree);		
		for (int ind = 0; ind < subRoots.size(); ++ind) {
			EToken cur = subRoots.get(ind);
			root.Children.add(cur);
			cur.Root = root;
		}
		mainAbstractTree.add(0, root);
	}
	
	public static boolean exists(ArrayList<EToken> mainAbstractTree, 
			ArrayList<EToken> childAbstractTree, boolean addRootTokenToMain) {
		EToken chRoot = ETree.GetRootFirst(childAbstractTree);
		ArrayList<EToken> cpMain = mainAbstractTree;
		if (addRootTokenToMain) {
			cpMain = EToken.getCopy(mainAbstractTree);
			addRootToken(cpMain);
		}
		for (int mind = 0; mind < cpMain.size(); ++mind) {
			if (compareTokens(cpMain.get(mind), chRoot)) {
				boolean check = isSubTree(cpMain.get(mind), chRoot);
				if (check) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static ArrayList<ETree> removeSubTrees(ArrayList<ETree> trs) {
		ArrayList<ETree> result = new ArrayList<>();
		for (int chind = 0; chind < trs.size(); ++chind) {
			int paind = 0;
			while (paind < trs.size()) {
				if (paind != chind) {
					if (trs.get(paind).subTrees.contains(trs.get(chind).SExp)) {
						break;
					}
				}
				++paind;
			}
			if (paind == trs.size()) {
				result.add(trs.get(chind));
			}
		}
		return result;
	}
	
	public static ArrayList<ETree> findTrees(ETweet tw, ArrayList<ETree> trs,
			boolean removeSubs) throws Exception {
		String twStructText = ETree.GetTreeText(tw);
		ArrayList<EToken> twAbsStruct = ETree.getAbstractTokens(twStructText);
		ArrayList<ETree> result = new ArrayList<>();
		for (int tind = 0; tind < trs.size(); ++tind) {
			ETree curTr = trs.get(tind);
			boolean check = exists(twAbsStruct, curTr.AbstractTokens, false);
			if (check) {
				result.add(curTr);
			}
		}
		if (removeSubs) {
			result = removeSubTrees(result);
		}
		return result;
	}
	
}
