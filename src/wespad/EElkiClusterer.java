package wespad;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;

public class EElkiClusterer {

	public static EClusterer Train(ArrayList<ETweet> trainWithLoadedFeatures,
			EClusterParam param) throws Exception {
		EClusterer result = new EClusterer();
		buildAttributes(trainWithLoadedFeatures, result);
		Database list = getDatabase(trainWithLoadedFeatures, result);
		
//		KMeansInitialization<NumberVector> init = new FirstKInitialMeans<>();
		long seed = 0;
		RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(
		  new RandomFactory(seed));
		
		KMeansLloyd<NumberVector> kmeans = new KMeansLloyd<NumberVector>(
				EuclideanDistanceFunction.STATIC, param.K, 100, init);
		result.Eclu = kmeans.run(list);
		return result;
	}
	
	public static EPair<Integer, Double>  GetBestLabel(EClusterer clu, ETweet tw) 
			throws Exception {
		ArrayList<EPair<Integer, Double>> list = GetLabels(clu, tw);
		return list.get(0);
	}

	public static ArrayList<EPair<Integer, Double>> GetLabels(EClusterer clu, 
			ETweet tw) throws Exception {
		double[] vals = new double[clu.EattrIndex.size()];
		fillVector(tw, vals, clu);
	    Vector nv = new Vector(vals);
	    EuclideanDistanceFunction df = EuclideanDistanceFunction.STATIC;
	    List<Cluster<KMeansModel>> clus = clu.Eclu.getAllClusters();
		ArrayList<EPair<Integer, Double>>  result = new ArrayList<>();
	    for (int cind = 0; cind < clus.size(); ++cind) {
	    	double dist = df.distance(nv, clus.get(cind).getModel().getMean());
	    	result.add(new EPair<Integer, Double>(cind, dist));
	    }
	    result.sort((ll, rr) -> Double.compare(ll.Value, rr.Value));
	    return result;
	}
	
	private static void buildAttributes(ArrayList<ETweet> list, EClusterer clu) 
			throws Exception {
		ArrayList<String> feats = getfeatureList(list);
		for (int find = 0; find < feats.size(); ++find) {
			clu.EattrIndex.put(feats.get(find), find);
		}
	}

	private static ArrayList<String> getfeatureList(ArrayList<ETweet> list) {
		HashSet<String> set = new HashSet<>();
		for (int twind = 0; twind < list.size(); ++twind) {
			ETweet tw = list.get(twind);
			String[] gs = tw.Feats.keySet().toArray(new String[0]);
			for (int gInd = 0; gInd < gs.length; ++gInd) {
				String[] fs = tw.Feats.get(gs[gInd]).keySet().toArray(new String[0]);
				for (int fInd = 0; fInd < fs.length; ++fInd) {
					String featName = gs[gInd] + "$" + fs[fInd];
					set.add(featName);
				}
			}
		}
		ArrayList<String> result = new ArrayList<>(set);
		return result;
	}

	private static Database getDatabase(ArrayList<ETweet> list, EClusterer clu) 
			throws Exception {
		double[][] values = new double[list.size()][clu.EattrIndex.size()];
		for (int twind = 0; twind < list.size(); ++twind) {
			fillVector(list.get(twind), values[twind], clu);			
		}
		DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(values);
		Database result = new StaticArrayDatabase(dbc, null);
		result.initialize();
		return result;
	}

	private static void fillVector(ETweet tw, double[] values, EClusterer clu) {
		String[] gs = tw.Feats.keySet().toArray(new String[0]);
		for (int gInd = 0; gInd < gs.length; ++gInd) {
			String[] fs = tw.Feats.get(gs[gInd]).keySet().toArray(new String[0]);
			for (int fInd = 0; fInd < fs.length; ++fInd) {
				double fv = tw.Feats.get(gs[gInd]).get(fs[fInd]);
				String featName = gs[gInd] + "$" + fs[fInd];
				if (clu.EattrIndex.containsKey(featName)) {
					int alpInd = clu.EattrIndex.get(featName);
					values[alpInd] = fv;
				}
			}
		}
	}
	
}
