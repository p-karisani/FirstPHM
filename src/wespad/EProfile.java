package wespad;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EProfile {
	public boolean IsVerified;
	public String JoinTime;
	public int MediaCount;
	public int TweetCount;
	public int FollowingCount;
	public int FollowerCount;
	public int LikeCount;
	public String Userid;
	public String Username;
	public String Location;
	public String Website;
	public String Description;
	public ArrayList<ETweet> HomeTweets;
	
	public EProfile() {
		
	}
	
	public EProfile(String line, ArrayList<ETweet> homeTweets) {
		HomeTweets = homeTweets;
		String[] tokens = line.split("\t");
		int ind = 0;
		IsVerified = Boolean.parseBoolean(tokens[ind]);
		++ind;
		JoinTime = tokens[ind];
		++ind;
		MediaCount = Integer.parseInt(tokens[ind]);
		++ind;
		TweetCount = Integer.parseInt(tokens[ind]);
		++ind;
		FollowingCount = Integer.parseInt(tokens[ind]);
		++ind;
		FollowerCount = Integer.parseInt(tokens[ind]);
		++ind;
		LikeCount = Integer.parseInt(tokens[ind]);
		++ind;
		Userid = tokens[ind];
		++ind;
		Username = tokens[ind].equals("-") ? null : tokens[ind];
		++ind;
		Location = tokens[ind].equals("-") ? null : tokens[ind];
		++ind;
		Website = tokens[ind].equals("-") ? null : tokens[ind];
		++ind;
		if (ind < tokens.length) {
			Description = tokens[ind].equals("-") ? null : tokens[ind];
			++ind;
		}
}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(IsVerified);
		sb.append("\t");
		sb.append(JoinTime);
		sb.append("\t");
		sb.append(MediaCount);
		sb.append("\t");
		sb.append(TweetCount);
		sb.append("\t");
		sb.append(FollowingCount);
		sb.append("\t");
		sb.append(FollowerCount);
		sb.append("\t");
		sb.append(LikeCount);
		sb.append("\t");
		sb.append(Userid);
		sb.append("\t");
		sb.append(Username == null ? "-" : Username);
		sb.append("\t");
		sb.append(Location == null ? "-" : Location);
		sb.append("\t");
		sb.append(Website == null ? "-" : Website);
		sb.append("\t");
		sb.append(Description == null ? "-" : Description);
		sb.append("\t");
		return sb.toString();
	}
	
	public static ArrayList<EProfile> load(String filePath, 
			String nullableHomeDirPath) throws Exception {
		ArrayList<EProfile> result = new ArrayList<>();
		List<String> lines = Files.readAllLines(Paths.get(filePath), 
				Charset.forName("UTF-8"));
		for (int ind = 0; ind < lines.size(); ++ind) {
			EProfile pr = new EProfile(lines.get(ind), null);
			if (nullableHomeDirPath != null) {
				File hm = new File(nullableHomeDirPath + 
						pr.Userid.replace("@", ""));
				if (hm.exists()) {
					pr.HomeTweets = ETweet.load(nullableHomeDirPath + 
							pr.Userid.replace("@", ""), ETagLoadType.StoredTags, false);
					ETweet.clearExtraInfo(pr.HomeTweets, true);
				}
			}
			result.add(pr);
		}
		return result;
	}
	
}
