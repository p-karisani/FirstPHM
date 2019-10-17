package wespad;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class EContext {
	public int Iteration;	
	public String TweetPath = null;
	public ETagLoadType LoadType = ETagLoadType.None;
	
	public ArrayList<ETweet> AllTweets;
	public ArrayList<ArrayList<ETweet>> AllTweetsByQuery;
	public ArrayList<ETweet> CurrentTweets;

	public ArrayList<ETweet> UnlabeledAllTweets;
	public ArrayList<ArrayList<ETweet>> UnlabeledAllTweetsByQuery;
	public ArrayList<ETweet> UnlabeledCurrentTweets;
	
	public EContext(int contextId, ArrayList<ETweet> allTweets, 
			ArrayList<ETweet> unlabeledAllTweetsNullable) throws Exception {
		Iteration = contextId;
		AllTweets = allTweets;
		AllTweetsByQuery = ETweet.categorizeByQuery(AllTweets);
		CurrentTweets = ETweet.copyList(AllTweets, true, false);
		if (unlabeledAllTweetsNullable != null) {
			SetUnlabeledTweets(unlabeledAllTweetsNullable);
		}
		else {
			SetUnlabeledTweets(null);
		}
	}
	
	public EContext(int contextId, String tweetPath, String profilePathNullable, 
			String homeDirPathNullable, String unlabeledNullable,
			ETagLoadType type) throws Exception {
		Iteration = contextId;
		TweetPath = tweetPath;
		LoadType = type;
		
		int lastChar = tweetPath.length() - 1;
		if (tweetPath.charAt(lastChar) != '$') {
			AllTweets = ETweet.load(tweetPath, type, false);
		}
		else {
			AllTweets = new ArrayList<>();
			String trainPath = tweetPath.substring(0, lastChar) + "-train";
			if (Files.exists(Paths.get(trainPath))) {
				ArrayList<ETweet> temp = ETweet.load(trainPath, type, false);
				ETweet.SetSetType(temp, ESetType.Train);
				AllTweets.addAll(temp);
			}
			String devPath = tweetPath.substring(0, lastChar) + "-dev";
			if (Files.exists(Paths.get(devPath))) {
				ArrayList<ETweet> temp = ETweet.load(devPath, type, false);
				ETweet.SetSetType(temp, ESetType.Dev);
				AllTweets.addAll(temp);
			}
			String testPath = tweetPath.substring(0, lastChar) + "-test";
			if (Files.exists(Paths.get(testPath))) {
				ArrayList<ETweet> temp = ETweet.load(testPath, type, false);
				ETweet.SetSetType(temp, ESetType.Test);
				AllTweets.addAll(temp);
			}
		}

//		AllTweets = ETweet.removeTweetsByLabel(AllTweets, -1);
//		ETweet.ShuffleTweets(AllTweets, 123123);

		AllTweetsByQuery = ETweet.categorizeByQuery(AllTweets);
		CurrentTweets = ETweet.copyList(AllTweets, true, false);
		ETweet.clearExtraInfo(AllTweets, false);
		if (profilePathNullable != null && homeDirPathNullable != null) {
			ETweetInfo.Load(CurrentTweets, homeDirPathNullable, profilePathNullable);
		}
		if (unlabeledNullable != null) {
			SetUnlabeledTweets(ETweet.load(unlabeledNullable, type, false));
		}
		else {
			SetUnlabeledTweets(null);
		}
		ELib.GarbageCollector();
	}

	private void SetUnlabeledTweets(ArrayList<ETweet> list) {
		if (list != null) {
			UnlabeledAllTweets = list;
			UnlabeledAllTweetsByQuery = new ArrayList<>();
			for (int ind = 0; ind < AllTweetsByQuery.size(); ++ind) {
				String q = AllTweetsByQuery.get(ind).get(0).Query;
				ArrayList<ETweet> tws = ETweet.getTweetsByQuery(UnlabeledAllTweets, q);
				ETweet.ShuffleTweets(tws, 123123);
				UnlabeledAllTweetsByQuery.add(tws);
			}
			UnlabeledCurrentTweets = ETweet.copyList(UnlabeledAllTweets, true, false);
		}
		else {
			UnlabeledAllTweets = new ArrayList<>();
			UnlabeledAllTweetsByQuery = new ArrayList<>();
			for (int ind = 0; ind < AllTweetsByQuery.size(); ++ind) {
				String q = AllTweetsByQuery.get(ind).get(0).Query;
				UnlabeledAllTweetsByQuery.add(new ArrayList<>());
			}
			UnlabeledCurrentTweets = new ArrayList<>();
		}
	}
	
	public void SetCurrentTweets(ArrayList<ETweet> curTweets, 
			ArrayList<ETweet> unlabeledCurTweetsNullable) {
		CurrentTweets = ETweet.copyList(curTweets, true, false);
		if (unlabeledCurTweetsNullable == null) {
			UnlabeledCurrentTweets = new ArrayList<>();
		}
		else {
			UnlabeledCurrentTweets = ETweet.copyList(unlabeledCurTweetsNullable, true, false);
		}
	}
	
}