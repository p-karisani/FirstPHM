package wespad;

import java.net.InetAddress;

public class EVar {

	public static String MachineCurrent;
	public static String CurrentPath;
	public static String CurrentUser;
	public static String ProjPath;
	public static String ProjPathSecondary;
	public static String ToolsPath;
	public static String SharedPath;
	
	static {
		try {
			CurrentUser = System.getProperty("user.name");
			MachineCurrent = InetAddress.getLocalHost().getHostName();
			CurrentPath = System.getProperty("user.dir") + "/";
			String[] toks = EVar.CurrentPath.split("/");
			
			ProjPath = "/mnt/01D266CBD57B0850/Research/HealthEventDetection/";
			ProjPathSecondary = ProjPath;
			ToolsPath = "/mnt/01D266CBD57B0850/Research/Shared/tools/";
			SharedPath = "/mnt/01D266CBD57B0850/Research/Shared/";
		}
		catch (Exception e) {
			System.out.println("machine name cannot be resolved");
		}
	}
	
	public static String SynTreeRoot = "$S";

	public static int[] LblEventHealth = new int[] {2, 3};
	public static int[] LblNonEventHealth = new int[] {-1, 0, 1};
	
    public static String Results = ProjPath + "result/";
	public static String Dataset = ProjPathSecondary + "dataset/";
	public static String EnglishWords = Dataset + "english.txt";
	public static String SpanishWords = Dataset + "spanish.txt";
	public static String StopWords = Dataset + "stopwords.txt";

	public static String Embedword2vecDir = SharedPath + "word2vec/";
	public static String Embedword2vec300Extracted = 
			Dataset + "EmbeddingData/GoogleNews-vectors-negative300.txt";

	public static String POSModel = ToolsPath + "ark-tweet-nlp-0.3.2/models/model.20120919";		
	public static String FREQTPath = ToolsPath + "freqt-0.22/freqt";

}
