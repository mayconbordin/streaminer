Streaminer
==========

A collection of algorithms for mining data streams, including frequent itemsets, quantiles, sampling, moving averages, set membership and cardinality.

## Releases

Maven:

```xml
<dependency>
    <groupId>com.github.mayconbordin</groupId>
    <artifactId>streaminer</artifactId>
    <version>1.1.1</version>
</dependency>
```

## [API Documentation](http://mayconbordin.github.io/streaminer/api/)


## Frequent Itemsets

### Algorithms
  - CountSketch [\[1\]](#ref1)
  - CountMinSketch [[2]](#ref2)
  - LossyCounting [[3]](#ref3)
  - Majority [[4]](#ref4)
  - MisraGries [[5]](#ref5)
  - SpaceSaving [[6]](#ref6)
  - StickySampling [[3]](#ref3)
  - RealCounting
  - SimpleTopKCounting
  - TimeDecayCountMinSketch
  - TimeDecayRealCounting
  - AMSSketch
  - CCFCSketch
  - CGT

### Usage

Except for the `CountMinSketchAlt` class, all algorithms implement the `IRichFrequency` interface. Here's an example using the `SpaceSaving` algorithm:

```java
Random r = new Random();
int counters = 20;
double support = 0.01;
double maxError = 0.1;

IRichFrequency<Integer> counter = new SpaceSaving<Integer>(counters, support, maxError);
for (int i=0 i<1000; i++) {
    counter.add(r.nextInt(100), 1);
}

// get the top 10 items
List<CountEntry<Integer>> topk = counter.peek(10);

// print the items
for (CountEntry<Integer> item : topk) {
    System.out.println(item);
}

// get the frequency of a single item
int item = 25;
long freq = counter.estimateCount(item);
System.out.println(item + ": " + freq);
```

### Time Decaying Algorithms

`TimeDecayRealCounting` and `TimeDecayCountMinSketch` are algorithms that use a 
decay function to update the current values of their counts in order to give more
importance to newer values, while older values will slowly fade away.

The decay function implements the `DecayFormula` interface. Currently there are 
three implementations: the exponential (`ExpDecayFormula`), the linear (`LinDecayFormula`), 
and the logarithmic (`LogDecayFormula`).

Those counting algorithms implement a different interface called `ITimeDecayFrequency`,
as both methods for adding and estimating the frequency need an additional argument, 
the timestamp.


## Top-K

### Algorithms

  - StreamSummary [[6]](#ref6)
  - ConcurrentStreamSummary
  - Frequent
  - StochasticTopper

### Usage

The basic usage of a Top-K algorithm is basically the same as the frequent itemset, except that these algorithms do not support the `estimateCount` method.

```java
ITopK<String> counter = new StreamSummary<String>(3);

String[] stream = {"X", "X", "Y", "Z", "A", "B", "C", "X", "X", "A", "C", "A", "A"};
for (String i : stream) {
    counter.add(i);
}

List<CountEntry<String>> topk = counter.peek(3);
for (CountEntry<String> item : topk) {
    System.out.println(item);
}
```


## Quantiles

### Algorithms

  - CKMSQuantiles [[7]](#ref7)
  - Frugal2U [[8]](#ref8)
  - GKQuantiles [[9]](#ref9)
  - MPQuantiles [[10]](#ref10)
  - QDigest [[11]](#ref11)
  - WindowSketchQuantiles [[12]](#ref12)
  - RSSQuantiles [[13]](#ref13)
  - EnsembleQuantiles
  - ExactQuantiles
  - ExactQuantilesAll
  - SimpleQuantiles
  - SumQuantiles
  - TDigest

### Usage

```java
double[] quantiles = new double[]{0.05, 0.25, 0.5, 0.75, 0.95};
IQuantiles<Integer> instance = new Frugal2U(quantiles, 0);

RandomEngine r = new MersenneTwister64(0);
Normal dist = new Normal(100, 50, r);
int numSamples = 1000;
        
for(int i = 0; i < numSamples; ++i) {
    int num = (int) Math.max(0, dist.nextDouble());
    instance.offer(num);
}

for (double q : quantiles) {
    System.out.println(q + ": " + instance.getQuantile(q));
}
```


## Cardinality

### Algorithms

  - AdaptiveCounting [[14]](#ref14)
  - LogLog [[15]](#ref15)
  - HyperLogLog [[16]](#ref16)
  - HyperLogLogPlus [[17]](#ref17)
  - LinearCounting [[18]](#ref18)
  - CountThenEstimate
  - BJKST [[26]](#ref26)
  - FlajoletMartin [[27]](#ref27)
  - KMinCount

### Usage

```java
ICardinality card = new LogLog(8);

for (int i=0; i<100; i++) {
    card.offer(Math.random()*100.0);
}

System.out.println("Cardinality: " + card.cardinality());
```


## Average

### Algorithms

  - MovingAverage
  - ExponentialMovingAverage
  - SimpleEWMA
  - VariableEWMA
  - TEWMA [[25]](#ref25)

### Usage

```java
// create a EWMA with 15 seconds of age for the metrics in the period
IAverage avg = new VariableEWMA(15.0);

for (int i=0; i<100; i++) {
    avg.add(Math.random()*100.0);
    if (i%10 == 0)
        System.out.println("Average: " + avg.getAverage());
}
```


## Membership

### Algorithms

  - BloomFilter [[22]](#ref22)
  - BloomFilterAlt (alternative implementation)
  - CountingBloomFilter [[19]](#ref19)
  - VarCountingBloomFilter (with variable `bucketsPerWord`)
  - DynamicBloomFilter [[20]](#ref20)
  - RetouchedBloomFilter [[21]](#ref21)
  - StableBloomFilter [[23]](#ref23)
  - TimingBloomFilter [[24]](#ref24)
  - ODTDBloomFilter [[28]](#ref28)

### Usage

```java
IFilter bloom = new BloomFilter(1000, 32, Hash.MURMUR_HASH);

for (int i = 0; i < 100; i++) {
    String val = UUID.randomUUID().toString();
    Key k = new Key(val.getBytes());
    bloom.add(k);
    System.out.println(val + " exists? " + bloom.membershipTest(k));
}
```


## Sampling

### Algorithms

  - BernoulliSampler
  - ChainSampler [[29]](#ref29)
  - ReservoirSampler
  - SystematicSampler
  - WRSampler (With Replacement)
  - WeightedRandomSampler
  - L0Sampler [[30]](#ref30)
  - SpaceSavingSampler
  - FrequentSampler

### Usage

```java
// Create a sampler with 30% probability
ISampler sampler = new BernoulliSampler(0.3);

Random rand = new Random();

// Create a dummy stream of ints
List<Integer> stream = new ArrayList<Integer>(1000);
for (int i=0; i<1000; i++)
    stream.add(rand.nextInt(100));

for (Integer tuple : stream) {
    if (sampler.next()) {
        // tuple was sampled, do something
    } else {
        // tuple was ignored, move on
    }
}
```


## Classifiers

### Algorithms

  - Perceptron
  - NaiveBayes
  - NaiveBayesWOP
  - BoundedBayes
  - LossyBayes
  - MultiBayes
  - MultiLossyBayes
  - MultiTopKBayes
  - SticySamplingBayes
  - TopKBayes
  - MajorityClass
  - RandomClassifier
  - MultiRandomClassifier
  - AROWClassifier (Adaptive Regularization of Weight Vectors) [[32]](#ref32)
  - BWinnowClassifier (Balanced Winnow Classifier) [[33]](#ref33)
  - PAClassifier, MultiClassPAClassifier [[34]](#ref34)
  - WinnowClassifier

### Usage

```java
NaiveBayes nb = new NaiveBayes();
nb.setLabelAttribute("play");
            
ICsvListReader listReader = new CsvListReader(
        new FileReader("src/test/resources/golf.csv"), 
        CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

listReader.getHeader(true);

List<String> list;
while( (list = listReader.read()) != null ) {
    Data data = new DataImpl();
    data.put("outlook", list.get(0));
    data.put("temperature", Integer.parseInt(list.get(1)));
    data.put("humidity", Integer.parseInt(list.get(2)));
    data.put("wind", Boolean.parseBoolean(list.get(3)));
    data.put("play", list.get(4));

    nb.learn(data);
}

Data test = new DataImpl();
test.put("outlook", "sunny");
test.put("temperature", "cool");
test.put("humidity", "high");
test.put("windy", "TRUE");

String prediction = nb.predict(test);
System.out.println("Item is: " + test);
System.out.println("Prediction is: " + prediction);
```

## Clustering

### Algorithms
  
  - K-Means
  - BIRCH (Balanced Iterative Reducing and Clustering using Hierarchies) [[31]](#ref31)


## References

`[1]` <a name="ref1"></a>Charikar, Moses, Kevin Chen, and Martin Farach-Colton. "Finding frequent items in data streams." Automata, Languages and Programming. Springer Berlin Heidelberg, 2002. 693-703.

`[2]` <a name="ref2"></a>Cormode, Graham, and S. Muthukrishnan. "An improved data stream summary: the count-min sketch and its applications." Journal of Algorithms 55.1 (2005): 58-75.

`[3]` <a name="ref3"></a>Manku, Gurmeet Singh, and Rajeev Motwani. "Approximate frequency counts over data streams." Proceedings of the 28th international conference on Very Large Data Bases. VLDB Endowment, 2002.

`[4]` <a name="ref4"></a>M. J. Fischer and S. L. Salzberg. "Finding a Majority Among N Votes: Solution to Problem 81-5(Journal of Algorithms, June 1981)", Journal of Algorithms, 3:4, December 1982, pp. 362-380.

`[5]` <a name="ref5"></a>Misra, Jayadev, and David Gries. "Finding repeated elements." Science of computer programming 2.2 (1982): 143-152.

`[6]` <a name="ref6"></a>Metwally, Ahmed, Divyakant Agrawal, and Amr El Abbadi. "Efficient computation of frequent and top-k elements in data streams." Database Theory-ICDT 2005. Springer Berlin Heidelberg, 2005. 398-412.

`[7]` <a name="ref7"></a>Cormode, Graham, et al. "Effective computation of biased quantiles over data streams." Data Engineering, 2005. ICDE 2005. Proceedings. 21st International Conference on. IEEE, 2005.

`[8]` <a name="ref8"></a>Ma, Qiang, S. Muthukrishnan, and Mark Sandler. "Frugal Streaming for Estimating Quantiles." Space-Efficient Data Structures, Streams, and Algorithms. Springer Berlin Heidelberg, 2013. 77-96.

`[9]` <a name="ref9"></a>Greenwald, Michael, and Sanjeev Khanna. "Space-efficient online computation of quantile summaries." ACM SIGMOD Record. Vol. 30. No. 2. ACM, 2001.

`[10]` <a name="ref10"></a>Munro, J. Ian, and Mike S. Paterson. "Selection and sorting with limited storage." Theoretical computer science 12.3 (1980): 315-323.

`[11]` <a name="ref11"></a>Shrivastava, Nisheeth, et al. "Medians and beyond: new aggregation techniques for sensor networks." Proceedings of the 2nd international conference on Embedded networked sensor systems. ACM, 2004.

`[12]` <a name="ref12"></a>Arasu, Arvind, and Gurmeet Singh Manku. "Approximate counts and quantiles over sliding windows." Proceedings of the twenty-third ACM SIGMOD-SIGACT-SIGART symposium on Principles of database systems. ACM, 2004.

`[13]` <a name="ref13"></a>Gilbert, Anna C., et al. "How to summarize the universe: Dynamic maintenance of quantiles." Proceedings of the 28th international conference on Very Large Data Bases. VLDB Endowment, 2002.

`[14]` <a name="ref14"></a>Cai, Min, et al. "Fast and accurate traffic matrix measurement using adaptive cardinality counting." Proceedings of the 2005 ACM SIGCOMM workshop on Mining network data. ACM, 2005.

`[15]` <a name="ref15"></a>Durand, Marianne, and Philippe Flajolet. "Loglog counting of large cardinalities." Algorithms-ESA 2003. Springer Berlin Heidelberg, 2003. 605-617.

`[16]` <a name="ref16"></a>Flajolet, Philippe, et al. "HyperLogLog: the analysis of a near-optimal cardinality estimation algorithm." DMTCS Proceedings 1 (2008).

`[17]` <a name="ref17"></a>Heule, Stefan, Marc Nunkesser, and Alexander Hall. "HyperLogLog in practice: algorithmic engineering of a state of the art cardinality estimation algorithm." Proceedings of the 16th International Conference on Extending Database Technology. ACM, 2013.

`[18]` <a name="ref18"></a>Whang, Kyu-Young, Brad T. Vander-Zanden, and Howard M. Taylor. "A linear-time probabilistic counting algorithm for database applications." ACM Transactions on Database Systems (TODS) 15.2 (1990): 208-229.

`[19]` <a name="ref19"></a>Fan, L., Cao, P., Almeida, J., & Broder, A. Z. (2000). Summary cache: a scalable wide-area web cache sharing protocol. IEEE/ACM Transactions on Networking (TON), 8(3), 281-293.

`[20]` <a name="ref20"></a>Guo, Deke, Jie Wu, Honghui Chen, and Xueshan Luo. "Theory and Network Applications of Dynamic Bloom Filters." In INFOCOM, pp. 1-12. 2006.

`[21]` <a name="ref21"></a>Donnet, Benoit, Bruno Baynat, and Timur Friedman. "Retouched bloom filters: allowing networked applications to trade off selected false positives against false negatives." In Proceedings of the 2006 ACM CoNEXT conference, p. 13. ACM, 2006.

`[22]` <a name="ref22"></a>Bloom, Burton H. "Space/time trade-offs in hash coding with allowable errors." Communications of the ACM 13, no. 7 (1970): 422-426.

`[23]` <a name="ref23"></a>Deng, Fan, and Davood Rafiei. "Approximately detecting duplicates for streaming data using stable bloom filters." Proceedings of the 2006 ACM SIGMOD international conference on Management of data. ACM, 2006.

`[24]` <a name="ref24"></a>Dautrich Jr, Jonathan L., and Chinya V. Ravishankar. "Inferential time-decaying Bloom filters." Proceedings of the 16th International Conference on Extending Database Technology. ACM, 2013.

`[25]` <a name="ref25"></a>Martin, Ruediger, and Michael Menth. "Improving the Timeliness of Rate Measurements." In MMB, pp. 145-154. 2004.

`[26]` <a name="ref26"></a>Bar-Yossef, Ziv, et al. "Counting distinct elements in a data stream." Randomization and Approximation Techniques in Computer Science. Springer Berlin Heidelberg, 2002. 1-10.

`[27]` <a name="ref27"></a>Flajolet, Philippe, and G. Nigel Martin. "Probabilistic counting algorithms for data base applications." Journal of computer and system sciences 31.2 (1985): 182-209.

`[28]` <a name="ref28"></a>Bianchi, Giuseppe, Nico d'Heureuse, and Saverio Niccolini. "On-demand time-decaying bloom filters for telemarketer detection." ACM SIGCOMM Computer Communication Review 41.5 (2011): 5-12.

`[29]` <a name="ref29"></a>Babcock, Brian, Mayur Datar, and Rajeev Motwani. "Sampling from a moving window over streaming data." Proceedings of the thirteenth annual ACM-SIAM symposium on Discrete algorithms. Society for Industrial and Applied Mathematics, 2002.

`[30]` <a name="ref30"></a>Cormode, Graham, Donatella Firmani, Graham Cormode, and Donatella Firmani. "On Unifying the Space of ℓ0-Sampling Algorithms." In ALENEX, pp. 163-172. 2013.

`[31]` <a name="ref31"></a>Zhang, Tian, Raghu Ramakrishnan, and Miron Livny. "BIRCH: an efficient data clustering method for very large databases." ACM SIGMOD Record. Vol. 25. No. 2. ACM, 1996.

`[32]` <a name="ref32"></a>Crammer, Koby, Alex Kulesza, and Mark Dredze. "Adaptive regularization of weight vectors." Advances in Neural Information Processing Systems. 2009.

`[33]` <a name="ref33"></a>Carvalho, Vitor R., and William W. Cohen. "Single-pass online learning: Performance, voting schemes and online feature selection." Proceedings of the 12th ACM SIGKDD international conference on Knowledge discovery and data mining. ACM, 2006.

`[34]` <a name="ref34"></a>Crammer, Koby, Ofer Dekel, Joseph Keshet, Shai Shalev-Shwartz, and Yoram Singer. "Online passive-aggressive algorithms." The Journal of Machine Learning Research 7 (2006): 551-585.

## Similar Libraries

  - **Java**
    - [stream-lib](https://github.com/addthis/stream-lib)
    - [MOA (Massive Online Analysis)](http://code.google.com/p/moa/)
    - [stream-mining](https://bitbucket.org/cbockermann/stream-mining)
    - [jerboa](https://github.com/vandurme/jerboa)
    - [hoidla](https://github.com/pranab/hoidla)
  - **Scala**
    - [algebird](https://github.com/twitter/algebird)
    - [fleet](https://github.com/noelwelsh/fleet)
  - **C/C++**
    - [sketch library](http://hadjieleftheriou.com/sketches/index.html)
    - [StreamSketch](https://github.com/absolute8511/StreamSketch)
    - [StreamingAlgorithms](https://github.com/bmoscon/StreamingAlgorithms)
    - [Streaming-Data-Algorithms](https://github.com/markfuge/Streaming-Data-Algorithms)
    - [scrunch](https://github.com/jkff/scrunch)
    - [MassDAL](http://www.cs.rutgers.edu/~muthu/massdal-code-index.html)
    - [sketches](http://www.cise.ufl.edu/~frusu/code.html)
  - **JavaScript**
    - [node-streamcount](https://github.com/jhurliman/node-streamcount)
    - [node-datastream](https://github.com/mayconbordin/node-datastream)
