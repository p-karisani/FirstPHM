The code of WESPAD model introduced in:

Payam Karisani and Eugene Agichtein. Did You Really Just Have a Heart Attack?: Towards Robust Detection of Personal Health Mentions in Social Media. In Proceedings of the 2018 World Wide Web Conference (WWW ’18). https://arxiv.org/abs/1802.09130


This code is a part of bigger project, what is posted here might be slightly different from what is described in the paper.


Dependencies (needed in the JAVA_PATH):
- commons-lang3-3.5.jar (Apache Commons)
- mallet.jar (Mallet API)
- mallet-deps.jar
- stanford-corenlp-3.9.2.jar (Stanford Core-nlp)
- ark-tweet-nlp-0.3.2.jar (ARK tweeter parser)
- elki-0.7.1.jar (Elki API)
- spmf.jar (SPMF data mining API)


Dependencies (Accessed through code, see EVAR.java within the src directory):
- FREQuent Tree miner
- ARK pre-trained model

<br/><br/>

- There are also a small set of files needed for parsing tweets and loading word embeddings placed in the data directory.
- The format of the embedding file should be the regular word2vec format—first line contains vocab size and dimension length.
- A sample of input data is also placed in the data directory: “tweets.txt” and “tweets.txt-tags”.
- The format of tweets.txt is: tweet id, label, user id, date, likes, retweets, replies, topic, tweet content.
- Labels are: 1 as negative and 3 as positive.
- tweets.txt-tags contains the parsed tweets. The tweet texts should be parsed by “TweeboParser”.
- The project constants are set in Evar.java. A sample code is placed in MainThread.java.
- The PHM dataset is in the dataset directory.
