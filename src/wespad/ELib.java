package wespad;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.time.StopWatch;

public class ELib {
	public static PrintStream StandardError;
	public static Random RND = null;
	private static HashSet<String> englishWords = new HashSet<>();
	private static HashSet<String> spanishWords = new HashSet<>();
	private static HashSet<String> stopWords = new HashSet<>();
	public static StopWatch stopW = new StopWatch();
	private static HashSet<String> delSet = new HashSet<>();
	private static String delimiters = 
			" ['\"“”‘’]+|[.?!,…]+|[:;]+(?:--+|―|—|~|–|=)@^*&%$#{}<>/\\~";

	static {
		RND = new Random(System.currentTimeMillis());
		for (int ind = 0; ind < delimiters.length(); ++ind) {
			if (!delSet.contains(delimiters.charAt(ind))) {
				delSet.add(delimiters.charAt(ind) + "");
			}
		}
		try {
			List<String> eng = Files.readAllLines(
					Paths.get(EVar.EnglishWords), 
					Charset.forName("UTF-8"));
			for (int ind = 0; ind < eng.size(); ++ind) {
				englishWords.add(eng.get(ind));
			}
			List<String> spa = Files.readAllLines(
					Paths.get(EVar.SpanishWords), 
					Charset.forName("UTF-8"));
			for (int ind = 0; ind < spa.size(); ++ind) {
				spanishWords.add(spa.get(ind));
			}
			List<String> stw = Files.readAllLines(
					Paths.get(EVar.StopWords), 
					Charset.forName("UTF-8"));
			for (int ind = 0; ind < stw.size(); ++ind) {
				stopWords.add(stw.get(ind).toLowerCase());
			}
		}
		catch (Exception err) {
			err.printStackTrace(System.out);
		}
	}

	public static void printlnTrimForScreen(Object text) throws Exception {
		print(false, text, true, true);
	}

	public static void println(Object text) throws Exception {
		print(false, text, true, false);
	}

	public static void print(Object text) throws Exception {
		print(false, text, false, false);
	}

	public static void printlnForce(Object text) throws Exception {
		print(true, text, true, false);
	}

	private static void print(boolean force, Object text, boolean newLine, 
			boolean trimForScreen) throws Exception {
		PrintStream temp = new PrintStream(new FileOutputStream(EVar.CurrentPath + "output.txt", true));
		
		String fileVersion = text.toString();
		String screenVersion = text.toString();
		
		if (trimForScreen) {
			String[] lines = screenVersion.split("\n");
			StringBuilder cp = new StringBuilder();
			for (int lind = 0; lind < lines.length; ++lind) {
				String curLine = lines[lind].substring(0, Math.min(200, lines[lind].length()));
				if (curLine.length() < lines[lind].length()) {
					curLine += " ...";
				}
				cp.append(curLine + "\n");
			}
			screenVersion = cp.toString();
		}
		
//		if (force)
		{
			if (newLine) {
				if (text != null) {
					temp.println(fileVersion);
					System.out.println(screenVersion);
				}
				else {
					temp.println();
					System.out.println();
				}
			}
			else {
				temp.print(fileVersion);
				System.out.print(screenVersion);
			}
		}
		
		temp.close();
	}
	
	public static String removeURL(String text) {
		String[] tokens = text.split(" ");
		StringBuilder result = new StringBuilder();
		for (int ind = 0; ind < tokens.length; ++ind) {
			if (tokens[ind].indexOf(".") >= 0 &&
					tokens[ind].indexOf("/") >= 0) {
				continue;
			}
			result.append(tokens[ind] + " ");
		}
		return result.toString().trim();
	}

	public static boolean isStopword(String word) {
		if (stopWords.contains(word.toLowerCase())) {
			return true;
		}
		return false;
	}
	
	public static String getTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
//		LocalDateTime now = LocalDateTime.now();
		String result = dtf.format(now);
		return result;
	}
	
	public static void GarbageCollector() {
		System.gc();
	}
	
	public static boolean isDelimiter(String token) {
		return delSet.contains(token);
	}
	
	public static double getInfoForIG(EPair<Double, Double> counts) {
		double posTerm = 0;
		double negTerm = 0;
		if (counts.Key + counts.Value > 0) {
			if (counts.Key > 0) {
				double prob = (counts.Key) / (counts.Key + counts.Value);
				posTerm = prob * Log2(prob);
			}
			if (counts.Value > 0) {
				double prob = (counts.Value) / (counts.Key + counts.Value);
				negTerm = prob * Log2(prob);
			}
		}
		double result = -1 * posTerm - 1 * negTerm;
		return result;
	}
	
	public static double Log2(double val) {
		return Math.log(val) / Math.log(2);
	}

	public static double GetIG(EPair<Double, Double> countsAll, 
			EPair<Double, Double> countsExi, EPair<Double, Double> countsNonexi) {
		double trainSize = countsAll.Key + countsAll.Value;
		double exiSize = countsExi.Key + countsExi.Value;
		double nonexiSize = countsNonexi.Key + countsNonexi.Value;
		double infoAll = getInfoForIG(countsAll);
		double infoExi = getInfoForIG(countsExi);
		double infoNonexi = getInfoForIG(countsNonexi);
		
		double infoExiTerm = (exiSize / trainSize) * infoExi;
		double infoNonexiTerm = (nonexiSize / trainSize) * infoNonexi;
		double infoTerm = infoExiTerm + infoNonexiTerm;
		
		double result = infoAll - infoTerm;
		return result;
	}

	public static EPair<Double, Double> getPosNegCount(ArrayList<ETweet> list, 
			ELblConf lbl) {
		EPair<Double, Double> result = new EPair<Double, Double>(0d, 0d);
		for (int ind = 0; ind < list.size(); ++ind) {
			ETweet tw = list.get(ind);
			if (lbl.PosNewLbl.isSource(tw.Label)) {
				++result.Key;
			}
			else if (lbl.NegNewLbl.isSource(tw.Label)) {
				++result.Value;
			}			
		}
		return result;
	}
		
	public static String normalizeTimeToStore(String time) {
		if (!Character.isDigit(time.charAt(0))) {
			String[] tokens = time.split(" ");
			String result = tokens[3] + " - " + tokens[2] + " " + tokens[1] + " " + tokens[5];
			return result;
		}
		else {
			return time;
		}
	}

	public static void Pass() {
		
	}
	
	public static int GetUnifiedSeed(int offset) {
		return 123123 + offset;
		
//		return 23543 + offset;
		
//		return 122343 + offset;
//		return 12568743 + offset;
//		return 1124243 + offset;
//		return 3462343 + offset;
//		return 7890343 + offset;
	}

}
