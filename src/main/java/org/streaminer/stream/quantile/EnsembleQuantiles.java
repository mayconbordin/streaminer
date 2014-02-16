package org.streaminer.stream.quantile;

import org.streaminer.util.distance.CosineDistance;
import org.streaminer.util.distance.LinearDistance;
import org.streaminer.util.distance.SquaredDistance;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class EnsembleQuantiles implements IQuantiles<Double> {
    private GKQuantiles activeBlock;
    private LinkedList<Double> sampleBlock;
    private int chunkSize;
    private int maxEnsembleSize;
    private double sampleRatio;
    private LinkedList<SingleModel> ensemble;
    private String updateMode;
    private String similarityMeasure;
    private double epsilon;

    /**
     * Use this String at {@link #setUpdateMode(String)} for always replacing the oldest model
     * with a new model. 
     */
    public static final String REPLACE_OLDEST_MODEL = "replaceOldest";
    
    /**
     * Use this String at {@link #setUpdateMode(String)} for replacing the most dissimilar model 
     * compared to the new model. Squared distance is used to determine similarity.
     */
    public static final String REPLACE_MOST_DISSIMILAR_MODEL = "replaceMostDissimilar";
    
    /**
     * Use this String at {@link #setUpdateMode(String)} for replacing a random model.
     */
    public static final String REPLACE_RANDOM_MODEL = "replaceRandom";
    
    /**
     * Use this String at {@link #setUpdateMode(String)} for using a sample of the stream to determine
     * which model of the ensemble will be replaced. The sample is used to create quantiles of parts of
     * the stream that are handled in different chunks. You can set the sample rate with {@link #setSampleRatio(double)}.
     * The similarity is computed by squared distance. 
     */
    public static final String REPLACE_SAMPLED_MOST_DISSIMILAR_MODEL = "replaceSampledMostDissimilar";
    
    /**
     * use this String at {@link #setUpdateMode(String)} for merging the eldest and second eldest model
     * in order to decrease the ensemble size by one.
     */
    public static final String MERGE_OLDEST_MODELS = "mergeOldest";
    
    /**
     * use this String at {@link #setUpdateMode(String)} for merging the most similar models of the ensemble
     * instead of deleting one. Similarity is defined by squared distance.
     */
    public static final String MERGE_MOST_SIMILAR_MODELS = "mergeMostSimilar";
    
    /**
     * use this String at {@link #setUpdateMode(String)} for merging the most dissimilar models of the ensemble.
     * Similarity is defined by squared distance.
     */
    public static final String MERGE_MOST_DISSIMILAR_MODELS = "mergeMostDissimilar";

    /**
     * this strategy merges the new, full bucket into the next one that is, the one
     * which has not been merged for the longest time
     */
    public static final String MERGE_ROUND_ROBIN = "mergeRoundRobin";


    /* -----------------------------
     * Similarity Measures
     * TODO write JavaDOC
     */
    public static final String EUCLIDEAN_DISTANCE = "euclidean distance";

    public static final String COSINE_DISTANCE = "cosine distance";

    public static final String MANHATTAN_DISTANCE = "manhattan distance";
    
    public EnsembleQuantiles(){
        this( 0.01f );
    }

    /**
     * @param epsilon <code>double</code> that represents the error bound.
     */
    public EnsembleQuantiles(double epsilon) {
        if (epsilon <= 0 || epsilon >= 1) {
            throw new RuntimeException("An appropriate epsilon value must lay between 0 and 1.");
        }
        
        this.epsilon = epsilon;
        this.activeBlock = new GKQuantiles(epsilon);
        this.sampleBlock = new LinkedList<Double>();
        this.ensemble = new LinkedList<SingleModel>();
        
        setChunkSize(250000);
        setEnsembleSize(5);
        
        this.updateMode = REPLACE_MOST_DISSIMILAR_MODEL;
        this.sampleRatio = 0.5d;
        this.similarityMeasure = EUCLIDEAN_DISTANCE;
    }
    
    public void setEpsilon( Double epsilon ){
        this.epsilon = epsilon;
        this.activeBlock = new GKQuantiles(epsilon);
        this.sampleBlock = new LinkedList<Double>();
        this.ensemble = new LinkedList<SingleModel>();
    }
        
    @Override
    public void offer(Double value) {
        activeBlock.offer(value);
		
        if (updateMode.equals(REPLACE_SAMPLED_MOST_DISSIMILAR_MODEL) && addToSampleBlock()) {
            sampleBlock.addLast(value);

            if (sampleBlock.size() > chunkSize){
                sampleBlock.removeFirst();
            }
        }

        if (activeBlock.getCount() == chunkSize){
            updateEnsemble();
            activeBlock = new GKQuantiles(epsilon);
        }
    }

    @Override
    public Double getQuantile(double q) throws QuantilesException {
        LinkedList<Double> summary = getSummary();
        Double wantedRank = Math.floor(q * summary.size());

        try {
            return summary.get(wantedRank.intValue());
        } catch (Exception e) {
            return Double.NaN;
        }
    }
    
    /**
     * Resets the size of a single chunk.
     * @param chunkSize - an appropriate <code>int</code> value
     */
    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    /**
     * Returns current chunk size.
     * @return the size of a single chunk
     */
    public int getChunkSize() {
        return chunkSize;
    }



    /**
     * Resets the maximum number of chunks in the ensemble. Excessive chunks will be removed.
     * @param maxEnsembleSize - an <code>int</code> value
     */
    public void setEnsembleSize(Integer maxEnsembleSize) {
        this.maxEnsembleSize = maxEnsembleSize;

        while (ensemble.size() > maxEnsembleSize){
            ensemble.removeFirst();
        }
    }

    /**
     * Returns the number of chunks that are managed in a ensemble
     * @return the maximum number of chunks stored in the ensemble
     */
    public int getEnsembleSize() {
        return maxEnsembleSize;
    }

    /**
     * Returns the currently used update mode
     * @return a {@link String} that contains the classifier of the current update mode
     */
    public String getUpdateMode() {
        return updateMode;
    }

    /**
     * Use this method to change the default kind of update (i.e. replacing the model with greatest squared distance to the new
     * model). There are seven different kinds of updating the ensemble (represented by <code>final static String</code>s):
     * <ul>
     * <li> {@link #REPLACE_MOST_DISSIMILAR_MODEL}: determining which model has furthest squared distance to the new one and replacing it (default)
     * <li> {@link #REPLACE_OLDEST_MODEL}: replacing the oldest value
     * <li> {@link #REPLACE_RANDOM_MODEL}: replacing a random model
     * <li> {@link #REPLACE_SAMPLED_MOST_DISSIMILAR_MODEL}: sampling the stream to get a reference model that contains elements that are handled in different chunks. again
     * the squared distance is used to determine which model will be replaced
     * <li> {@link #MERGE_OLDEST_MODELS}: merging the oldest model to decrease the number of models of the ensemble
     * <li> {@link #MERGE_MOST_SIMILAR_MODELS}: merging the most similar models in respect to squared distance
     * <li> {@link #MERGE_MOST_DISSIMILAR_MODELS}: merging the most dissimilar models in respect to squared distance
     * </ul>
     * @param updateMode Please use final static strings listed above to enable an update mode.
     */
    public void setUpdateMode(String um ) {
        String updateMode = um;
        if (um.indexOf( "_" ) > 0 ) {
            String[] tok = um.split( "_" );
            String mode = tok[0].toLowerCase();
            for (int i = 1; i < tok.length; i++) {
                String cur = tok[i].toLowerCase();
                mode = mode + cur.substring( 0, 1).toUpperCase() + cur.substring( 1 );
            }
            updateMode = mode;
        }

        if (updateMode.equals(REPLACE_OLDEST_MODEL) || updateMode.equals(REPLACE_RANDOM_MODEL)
                    || updateMode.equals(REPLACE_MOST_DISSIMILAR_MODEL) || updateMode.equals(REPLACE_SAMPLED_MOST_DISSIMILAR_MODEL)
                    || updateMode.equals(MERGE_OLDEST_MODELS) || updateMode.equals(MERGE_MOST_SIMILAR_MODELS)
                    || updateMode.equals(MERGE_MOST_DISSIMILAR_MODELS ) || updateMode.equals( MERGE_ROUND_ROBIN ) ) {

            this.updateMode = updateMode;	
        }
        //TODO Runtime Exception?
        else {
            //System.out.println("Wrong parameter value '" + updateMode + "'! Haven't change the upate mode.");
        }
    }

    /**
     * Returns current sample rate
     * @return <code>double</code> value specifying the current sample rate.
     */
    public double getSampleRatio() {
        return sampleRatio;
    }

    /**
     * Specify a {@link Double} value to set the sampling rate. Please note that a sample rate of 0 will
     * result in no sampling, so each time the reference vector will be the new model. If you set the sample
     * rate to a value greater or equal to 1 the elements will rather be saved than sampled.
     * @param sampleRatio
     */
    public void setSampleRatio(Double sampleRatio) {
        if (sampleRatio < 0) {
            sampleRatio = 0.0d;
        }
        if (sampleRatio > 1) {
            sampleRatio = 1.0d;
        }
        this.sampleRatio = sampleRatio;
    }

    public void setSimilarityMeasure (String similarityMeasure){
        if (similarityMeasure.equals(COSINE_DISTANCE) || similarityMeasure.equals(EUCLIDEAN_DISTANCE) 
                    || similarityMeasure.equals(MANHATTAN_DISTANCE)){
            this.similarityMeasure = similarityMeasure;
        }
        //TODO Runtime Exception?
    }

    public String getSimilarityMeasure(){
        return similarityMeasure;
    }
    
    /**
     * Every time a chunk gets full, the ensemble must be updated. If the ensemble doesn't consist of 
     * <code>maxEnsembleSize</code> chunks, the newest chunk will be added to the ensemble without removing 
     * any, of course.<br>
     * For more details on update methods see {@link #setUpdateMode(String)}. 
     */
    private void updateEnsemble() {
        SingleModel newModel = this.getNewModel();

        if (this.ensemble.size() < this.maxEnsembleSize) {
                this.ensemble.addLast(newModel);
        } else {
            if( this.updateMode.equals( MERGE_ROUND_ROBIN ) ){
                this.mergeRoundRobinModels( newModel );
                return;
            }

            if (this.updateMode.equals(REPLACE_OLDEST_MODEL)){
                this.replaceOldestModel(newModel);
            }

            if (this.updateMode.equals(REPLACE_RANDOM_MODEL)){
                this.replaceRandomModel(newModel);
            }

            if (this.updateMode.equals(REPLACE_MOST_DISSIMILAR_MODEL)){
                this.replaceMostDissimilarModel(newModel);
            }

            if (this.updateMode.equals(REPLACE_SAMPLED_MOST_DISSIMILAR_MODEL)){
                this.replaceSampledMostDissimilarModel(newModel);
            }

            if (this.updateMode.equals(MERGE_OLDEST_MODELS)){
                this.mergeOldestModels(newModel);
            }

            if (this.updateMode.equals(MERGE_MOST_SIMILAR_MODELS)){
                this.mergeMostSimilarModels(newModel);
            }

            if (this.updateMode.equals(MERGE_MOST_DISSIMILAR_MODELS)){
                this.mergeMostDissimilarModels(newModel);
            }
        }
    }

    /**
     * Updating by replacing the oldest model
     * @param newModel
     */
    private void replaceOldestModel(SingleModel newModel){
        this.ensemble.removeFirst();
        this.ensemble.addLast(newModel);
    }

    /**
     * Updating by replacing a random model
     * @param newModel
     */
    private void replaceRandomModel(SingleModel newModel){
        Random random = new Random();
        this.ensemble.remove( random.nextInt(this.ensemble.size()) );
        this.ensemble.addLast(newModel);
    }

    /**
     * Updating by replacing the most dissimilar model
     * @param newModel
     */
    private void replaceMostDissimilarModel(SingleModel newModel){
        SingleModel worstModel = this.getModelWithLowestSimilarityTo(newModel);

        this.ensemble.remove(worstModel);
        this.ensemble.addLast(newModel);
    }

    /**
     * Updating by replacing the most similar model in respect to a sample of the stream
     * @param newModel
     */
    private void replaceSampledMostDissimilarModel(SingleModel newModel){
        LinkedList<Double> quantiles = new LinkedList<Double>();
        Double phi = epsilon;

        while (phi < 1) {
            Double nextQuantile = phi * this.sampleBlock.size();

            try {
                quantiles.add(this.sampleBlock.get(nextQuantile.intValue()));
            } catch (IndexOutOfBoundsException e) {
                phi = 1.0d;
                quantiles = newModel.getQuantiles();
            }

            phi += epsilon;
        }
        SingleModel sample = new SingleModel(quantiles);
        SingleModel worstModel = this.getModelWithLowestSimilarityTo(sample);

        this.ensemble.remove(worstModel);
        this.ensemble.addLast(newModel);
    }

    /**
     * Updating by merging the eldest and second eldest model
     * @param newModel
     */
    private void mergeOldestModels(SingleModel newModel){
        SingleModel mergedModel = this.mergeModels(this.ensemble.get(0), this.ensemble.get(1));

        this.ensemble.removeFirst();
        this.ensemble.removeFirst();
        this.ensemble.addFirst(mergedModel);
        this.ensemble.addLast(newModel);
    }

    /**
     * Updating by merging the eldest and second eldest model
     * @param newModel
     */
    private void mergeRoundRobinModels(SingleModel newModel){
        SingleModel mergedModel = this.mergeModels(this.ensemble.get(0), this.ensemble.get(1));

        this.ensemble.removeFirst();
        this.ensemble.removeFirst();
        this.ensemble.addLast(mergedModel);
        this.ensemble.addLast(newModel);
    }

    /**
     * Updating by merging the most similar models
     * @param newModel
     */
    private void mergeMostSimilarModels(SingleModel newModel){
        LinkedList<LinkedList<Double>> allModels = new LinkedList<LinkedList<Double>>();

        for (int i = 0; i < this.ensemble.size(); i++){
            allModels.add(this.ensemble.get(i).getQuantiles());
        }

        LinkedList<LinkedList<Double>> mergePair = (LinkedList<LinkedList<Double>>) SquaredDistance.getPairWithSmallestDistance(allModels);

        if (this.similarityMeasure.equals(COSINE_DISTANCE)){
            mergePair = (LinkedList<LinkedList<Double>>) CosineDistance.getPairWithSmallestDistance(allModels);
        }
        if (this.similarityMeasure.equals(MANHATTAN_DISTANCE)){
            mergePair = (LinkedList<LinkedList<Double>>) LinearDistance.getPairWithSmallestDistance(allModels);
        }

        SingleModel mergedOne = new SingleModel(mergePair.getFirst());
        SingleModel mergedTwo = new SingleModel(mergePair.getLast());
        SingleModel mergedModel = this.mergeModels(mergedOne, mergedTwo);

        //int removed = 0;
        Iterator<SingleModel> it = ensemble.iterator();
        while (it.hasNext()) {
            SingleModel cur = it.next();
            if( cur.equals( mergedOne ) || cur.equals( mergedTwo ) ){
                it.remove();
                //removed++;
            }
        }
        /*
        for (int i = 0; i < this.ensemble.size(); i++){
                if (this.ensemble.get(i).equals(mergedOne) || this.ensemble.get(i).equals(mergedTwo)){
                        //this.ensemble.remove(i);
                        removed++;
                }
        }
         */
        this.ensemble.add(mergedModel);
        this.ensemble.add(newModel);
    }

    /**
     * Updating by merging the most dissimilar models
     * @param newModel
     */
    private void mergeMostDissimilarModels(SingleModel newModel){
        LinkedList<LinkedList<Double>> allModels = new LinkedList<LinkedList<Double>>();

        for (int i = 0; i < this.ensemble.size(); i++){
            allModels.add(this.ensemble.get(i).getQuantiles());
        }

        LinkedList<LinkedList<Double>> mergePair = (LinkedList<LinkedList<Double>>) SquaredDistance.getPairWithFurthestDistance(allModels);

        if (this.similarityMeasure.equals(COSINE_DISTANCE)) {
            mergePair = (LinkedList<LinkedList<Double>>) CosineDistance.getPairWithFurthestDistance(allModels);
        }
        if (this.similarityMeasure.equals(MANHATTAN_DISTANCE)) {
            mergePair = (LinkedList<LinkedList<Double>>) LinearDistance.getPairWithFurthestDistance(allModels);
        }

        SingleModel mergedOne = new SingleModel(mergePair.getFirst());
        SingleModel mergedTwo = new SingleModel(mergePair.getLast());
        SingleModel mergedModel = this.mergeModels(mergedOne, mergedTwo);

        for (int i = 0; i < this.ensemble.size(); i++) {
            if (this.ensemble.get(i).equals(mergedOne) || this.ensemble.get(i).equals(mergedTwo)){
                this.ensemble.remove(i);
            }
        }

        this.ensemble.add(mergedModel);
        this.ensemble.add(newModel);
    }

    /**
     * The maximum error parameter for a chunk is bounded by epsilon. I.e. a estimated rank must not differ more than
     * epsilon * (number of elements in the chunk) ranks from the exact rank. We achieve this goal by constructing a
     * summary that contains all equidistant quantiles, starting with epsilon, 2*epsilon, 3*epsilon, and so on.
     * @return a {@link SingleModel} containing the equidistant quantiles from epsilon to 1
     */
    private SingleModel getNewModel(){
        LinkedList<Double> quantiles = new LinkedList<Double>();
        Double phi = epsilon;

        while (phi < 1) {
            quantiles.add(this.activeBlock.getQuantile(phi));
            phi += epsilon;
        }

        return (new SingleModel(quantiles));
    }

    /**
     * Computes the similarity of all chunks in the ensemble compared to the <code>comparator</code>.
     * @param comparator
     * @return {@link SingleModel} containing the worst chunk in the ensemble in respect to squared 
     * distance to <code>comparator</code>
     */
    private SingleModel getModelWithLowestSimilarityTo(SingleModel comparator){
        LinkedList<LinkedList<Double>> oldQuantiles = new LinkedList<LinkedList<Double>>();

        for (int i = 0; i < this.ensemble.size(); i++) {
            oldQuantiles.add(ensemble.get(i).getQuantiles());
        }

        LinkedList<Double> toBeRemovedQuantiles = SquaredDistance.getFarestVector(oldQuantiles, comparator.getQuantiles());
        if (this.similarityMeasure.equals(COSINE_DISTANCE)) {
            toBeRemovedQuantiles = CosineDistance.getFarestVector(oldQuantiles, comparator.getQuantiles());
        }
        if (this.similarityMeasure.equals(MANHATTAN_DISTANCE)) {
            toBeRemovedQuantiles = LinearDistance.getFarestVector(oldQuantiles, comparator.getQuantiles());
        }

        SingleModel toBeRemoved = new SingleModel(toBeRemovedQuantiles);

        for (int i = 0; i < this.ensemble.size(); i++) {
            if (ensemble.get(i).equals(toBeRemoved)){
                return ensemble.get(i);
            }
        }
        return toBeRemoved;
    }

    /**
     * If sampling is enabled (i.e. an update mode using sampling is active) this method determines
     * whether a new stream element gets added to the sample block or not.
     * @return <code>true</code> if a new element should be part of the sample block or else <code>false</code>
     */
    private boolean addToSampleBlock(){
        Random random = new Random();

        if (random.nextDouble() <= this.sampleRatio) {
            return true;
        }
        return false;
    }

    /**
     * Merges two given models to a single model. In detail {@link Vector#mean(java.util.List, java.util.List)}
     * is used.
     * @param first - {@link SingleModel} containing a vector.
     * @param second - {@link SingleModel} containing a vector.
     * @return {@link SingleModel} that containing the resulting vector.
     */
    private SingleModel mergeModels(SingleModel first, SingleModel second){
        LinkedList<Double> fst = first.getQuantiles();
        LinkedList<Double> snd = second.getQuantiles();
        LinkedList<Double> merged = new LinkedList<Double>();

        for(int i = 0; i < fst.size() && i < snd.size(); i++)
            merged.add(0.5 * (fst.get(i) + snd.get(i)));

        return new SingleModel(merged);
    }


    public String toString(){
        StringBuffer s = new StringBuffer();
        s.append( getClass().getCanonicalName() );
        s.append( " {" );
        s.append( " updateMode=" + this.getUpdateMode() );
        s.append( ", epsilon=" + epsilon );
        s.append( ", chunkSize=" + this.getChunkSize() );
        s.append( ", ensembleSize=" + this.getEnsembleSize() );
        s.append( " }" );
        return s.toString();
    }
    
    /**
     * Constructs a summary taking into account the ensemble and the current active block.
     * @return a {@link List} of {@link Double} containing a sorted list of quantiles. 
     */
    private LinkedList<Double> getSummary(){
        LinkedList<Double> summary = new LinkedList<Double>();

        for (int i = 0; i < this.ensemble.size(); i++) {
            summary.addAll(this.ensemble.get(i).getQuantiles());
        }

        Double phi = epsilon;

        // Due to avoid Double.NaN and to many absolute min/max values in the summary
        if (this.activeBlock.getCount() > 1 / epsilon) {
            while (phi < 1){
                summary.add(this.activeBlock.getQuantile(phi));
                phi += epsilon;
            }
        }

        while (summary.contains(null)) {
            summary.remove(null);
        }

        Collections.sort(summary);
        return summary;
    }
    
    /**
     * Wrapper to avoid ugly <code>LinkedList&ltLinkedList&ltDouble&gt&gt</code> constructs.
     */
    private class SingleModel implements Serializable {
        private static final long serialVersionUID = -8462870855147396071L;
        private LinkedList<Double> quantiles;

        public SingleModel (LinkedList<Double> quantiles) {
            this.quantiles = quantiles;
        }

        public LinkedList<Double> getQuantiles() {
            return this.quantiles;
        }

        public boolean equals(SingleModel model) {
            for (int i = 0; i < this.quantiles.size(); i++) {
                if (!(this.quantiles.get(i).equals(model.getQuantiles().get(i)))) {
                    return false;
                }
            }
            return true;
        }
    }
}
