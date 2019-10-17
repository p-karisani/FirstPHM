package wespad;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

public class ETweetInfo {
	
	public String ProfDesc = "";
	public ArrayList<EToken> ProfDescToks = new ArrayList<>();
	public ArrayList<ETweet> PreTweets = new ArrayList<>();
	public ArrayList<ETweet> NexTweets = new ArrayList<>();
	
	@Override
	public String toString() {
		return "Pre: " + PreTweets.size() + " Nex: " + NexTweets.size() + "\t" + ProfDesc;
	}
	
	public static void Load(ArrayList<ETweet> list, String homeDir, 
			String profileFile) throws Exception {
		loadProfiles(list, profileFile);
		loadHomes(list, homeDir);
//		System.out.print("");
		for (int lind = 0; lind < list.size(); ++lind) {
			ETweet tw = list.get(lind);
			if (tw.Info != null) {
				if (tw.Info.ProfDesc.length() > 0) {
					tw.Info.ProfDescToks = EFeat1Gram.getTokensWithPOSTagger(tw.Info.ProfDesc, true);
				}
				if (tw.Info.PreTweets.size() > 0) {
					EFeat1Gram.loadTokens(tw.Info.PreTweets);
					EFeat1Gram.loadTokens(tw.Info.NexTweets);
				}
			}
			else {
				tw.Info = new ETweetInfo();
			}
		}
//		System.out.print("");
	}
	
	private static void loadProfiles(ArrayList<ETweet> list, String profileFile) 
			throws Exception {
		ArrayList<EProfile> prfLst = EProfile.load(profileFile, null);
		Hashtable<String, EProfile> prfTbl = new Hashtable<>();
		for (int pind = 0; pind < prfLst.size(); ++pind) {
			prfTbl.put(prfLst.get(pind).Userid, prfLst.get(pind));
		}
		for (int tind = 0; tind < list.size(); ++tind) {
			ETweet tw = list.get(tind);
			EProfile prf = prfTbl.get(tw.Userid);
			if (prf != null && prf.Description != null) {
				tw.Info = new ETweetInfo();
				tw.Info.ProfDesc = prf.Description.trim();
			}
		}
	}
	
	private static void loadHomes(ArrayList<ETweet> list, String homeDir) 
			throws Exception {
		int hasCount = 0;
		for (int tind = 0; tind < list.size(); ++tind) {
			ETweet tw = list.get(tind);
			String homeFile = homeDir + tw.Userid;
			if (Files.exists(Paths.get(homeFile))) {
				boolean res = loadHome(tw, homeFile);
				if (res) {
					++hasCount;
				}
			}
		}
//		System.out.println(hasCount);
	}
	
	private static boolean loadHome(ETweet tw, String homeFile) throws Exception {
		ArrayList<String> lines = new ArrayList<>();
		Scanner scan = new Scanner(new File(homeFile), StandardCharsets.UTF_8.name());
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			lines.add(line);
		}
		scan.close();
		int lind = 0;
		while (lind < lines.size()) {
			String line = lines.get(lind);
			int tbind = line.indexOf("\t");
			if (tbind >= 0) {
				String twid = line.substring(0, tbind);
				if (tw.Tweetid.equals(twid)) {
					break;
				}
			}
			else {
//				System.out.println(line);
			}
			++lind;
		}
		if (lind < lines.size()) {
			if (tw.Info == null) {
				tw.Info = new ETweetInfo();
			}
			ETweet pre = getPreTweet(lines, lind - 1);
			if (pre != null) {
				pre.Label = tw.Label;
				tw.Info.PreTweets.add(pre);
			}
			ETweet nex = getNextTweet(lines, lind + 1);
			if (nex != null) {
				nex.Label = tw.Label;
				tw.Info.NexTweets.add(nex);
			}
			return true;
		}
		return false;
	}
	
	private static ETweet getPreTweet(ArrayList<String> lines, int cind) {
		for (int ind = cind; 0 <= ind; --ind) {
			try {
				ETweet et = new ETweet(lines.get(ind));
				return et;
			}
			catch (Exception e) {
//				System.out.println(lines.get(ind));
			}
		}
		return null;
	}

	private static ETweet getNextTweet(ArrayList<String> lines, int cind) {
		for (int ind = cind; ind < lines.size(); ++ind) {
			try {
				ETweet et = new ETweet(lines.get(ind));
				return et;
			}
			catch (Exception e) {
//				System.out.println(lines.get(ind));
			}
		}
		return null;
	}
	
}
