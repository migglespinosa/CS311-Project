//import java.io.*;
//import java.util.Scanner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.Random;

public class RevenuePredictor{

  static Map<Integer,Double> cpiMap = new HashMap<Integer, Double>();
  static List<Map<String,Integer>> movieList = new ArrayList<Map<String,Integer>>();
  static List<Map<String,Integer>> movieListAdjusted = new ArrayList<Map<String,Integer>>();
  static List<Map<String,Integer>> movieListFormatted = new ArrayList<Map<String,Integer>>();
  static Map<String,Integer> movieListAverages = new HashMap<String,Integer>();
  static List<Map<String,Integer>> testMovieList = new ArrayList<Map<String,Integer>>();
  static List<Map<String,Integer>> testMovieListAdjusted = new ArrayList<Map<String,Integer>>();
  static List<Map<String,Integer>> testMovieListFormatted = new ArrayList<Map<String,Integer>>();
  static int[] missingYears = new int[]{2015, 2016, 2017, 2018, 2019, 2020};
  static double[] missingCPIs = new double[]{233.707, 236.916, 242.839, 247.876, 251.712, 257.971};
  static double learningRate = 0.2;
  //static Map<String,Integer> randWeights = new HashMap<String,Integer>();

  public static void main(String[] args) throws Exception{

    initializeMovies(movieList, 0, 4000);
    initializeCPI();
    createAdjusted(movieList, movieListAdjusted);
    createAverages();
    formatData(movieList, movieListAdjusted, movieListFormatted);
    initializeMovies(testMovieList, 4001, 5000);
    createAdjusted(testMovieList, testMovieListAdjusted);
    formatData(testMovieList, testMovieListAdjusted, testMovieListFormatted);
    Map<String,Double> testWeights = perceptronLearning();
    evaluate(testWeights);
    //System.out.println("movieListFormatted: "+movieListFormatted);

  }

  public static double adjustInflation(int val, int year){

    double cpi = cpiMap.get(year);
    double valAdjusted = (100/cpi)*val;

    return valAdjusted;
  }

  public static int compareActor1(int val){
    if(val >= movieListAverages.get("actor_1_avg")){
      return 1;
    }
    else{
      return 0;
    }
  }
  public static int compareActor2(int val){
    if(val >= movieListAverages.get("actor_2_avg")){
      return 1;
    }
    else{
      return 0;
    }
  }

  public static int compareActor3(int val){
    if(val >= movieListAverages.get("actor_3_avg")){
      return 1;
    }
    else{
      return 0;
    }
  }

  public static int compareBudget(int val){
    if(val >= movieListAverages.get("budget_avg")){
      return 1;
    }
    else{
      return 0;
    }
  }

  public static int compareCritic(int val){
    if(val >= movieListAverages.get("critic_avg")){
      return 1;
    }
    else{
      return 0;
    }
  }

  public static int compareDirector(int val){
    if(val >= movieListAverages.get("director_avg")){
      return 1;
    }
    else{
      return 0;
    }
  }

  public static int compareGross(int val){
    if(val >= movieListAverages.get("gross_avg")){
      return 1;
    }
    else{
      return 0;
    }
  }

  public static int compareUsers(int val){
    if(val >= movieListAverages.get("voted_users_avg")){
      return 1;
    }
    else{
      return 0;
    }
  }

  public static boolean containsNull(String[] lineArray){

    int[] indexList = new int[]{2, 4, 5, 7, 8, 12, 22, 23, 24};

    for(int i = 0; i < indexList.length; i++){
      if(lineArray[indexList[i]].isEmpty()){
        return true;
      }
      try {
        int element = Integer.parseInt(lineArray[indexList[i]]);
      } catch (NumberFormatException e) {
        return true;
      }
    }
    return false;
  }

  public static void createAdjusted(List<Map<String,Integer>> MovieList, List<Map<String,Integer>> MovieListAdjusted){

    int movieSize = MovieList.size();
    for(int i = 0; i < movieSize; i++){

      Map<String,Integer> record = new HashMap<String,Integer>(MovieList.get(i));

      int year = record.get("year");
      int adjustedBudget = (int)adjustInflation(record.get("budget"), year);
      int adjustedGross = (int)adjustInflation(record.get("gross"), year);

      record.replace("budget", adjustedBudget);
      record.replace("gross", adjustedGross);

      MovieListAdjusted.add(0, record);
    }
  }

  public static void createAverages(){

    int movieSize = movieList.size();
    int director_accumulate = 0;
    int actor1_accumulate = 0;
    int actor2_accumulate = 0;
    int actor3_accumulate = 0;
    int critic_accumulate = 0;
    int voted_users_accumulate = 0;
    int budget_accumulate = 0;
    double gross_accumulate = 0;

    for(int i = 0; i < movieSize; i++){

      Map<String,Integer> record = new HashMap<String,Integer>(movieListAdjusted.get(i));

      director_accumulate += record.get("director_facebook_likes");
      actor1_accumulate += record.get("actor_1_facebook_likes");
      actor2_accumulate += record.get("actor_2_facebook_likes");
      actor3_accumulate += record.get("actor_3_facebook_likes");
      critic_accumulate += record.get("num_critic_for_reviews");
      voted_users_accumulate += record.get("num_voted_users");
      budget_accumulate += record.get("budget");
      gross_accumulate += (double)record.get("gross");
    }

    movieListAverages.put("director_avg", director_accumulate/movieSize);
    movieListAverages.put("actor_1_avg", actor1_accumulate/movieSize);
    movieListAverages.put("actor_2_avg", actor2_accumulate/movieSize);
    movieListAverages.put("actor_3_avg", actor3_accumulate/movieSize);
    movieListAverages.put("critic_avg", critic_accumulate/movieSize);
    movieListAverages.put("voted_users_avg", voted_users_accumulate/movieSize);
    movieListAverages.put("budget_avg", budget_accumulate/movieSize);
    movieListAverages.put("gross_avg", (int)gross_accumulate/movieSize);
  }

  public static void formatData(List<Map<String,Integer>> MovieList, List<Map<String,Integer>> MovieListAdjusted, List<Map<String,Integer>> MovieListFormatted){

    int movieSize = MovieList.size();
    for(int i = 0; i < movieSize; i++){
      Map<String,Integer> record = new HashMap<String,Integer>(MovieListAdjusted.get(i));
      Map<String,Integer> formattedRecord = new HashMap<String,Integer>();

      int actor1 = compareActor1(record.get("actor_1_facebook_likes"));
      int actor2 = compareActor2(record.get("actor_2_facebook_likes"));
      int actor3 = compareActor2(record.get("actor_3_facebook_likes"));
      int director = compareDirector(record.get("director_facebook_likes"));
      int gross = compareGross(record.get("gross"));
      int budget = compareBudget(record.get("budget"));
      int voted_users = compareUsers(record.get("num_voted_users"));
      int critics = compareUsers(record.get("num_critic_for_reviews"));

      formattedRecord.put("actor_1_facebook_likes", actor1);
      formattedRecord.put("actor_2_facebook_likes", actor2);
      formattedRecord.put("actor_3_facebook_likes", actor3);
      formattedRecord.put("budget", budget);
      formattedRecord.put("director_facebook_likes", director);
      formattedRecord.put("gross", gross);
      formattedRecord.put("num_critic_for_reviews", critics);
      formattedRecord.put("num_voted_users", voted_users);

      MovieListFormatted.add(formattedRecord);
    }


  }

  public static void initializeCPI() throws Exception{

    String line = "";
    String splitBy = ",";

    BufferedReader br = new BufferedReader(new FileReader("cpi.csv"));
    br.readLine();
    while ((line = br.readLine()) != null){

      String[] lineArray = line.split(splitBy);
      LocalDate date = LocalDate.parse(lineArray[0]);
      int month = date.getMonthValue();
      if(month == 1){
        populateCPI(lineArray);
      }
    }
    populateCPIMissing();
  }

  public static void initializeMovies(List<Map<String,Integer>> MovieList, int initCount, int maxCount) throws Exception{

    String line = "";
    String splitBy = ",";
    int count = initCount;

    BufferedReader br = new BufferedReader(new FileReader("movie_metadata.csv"));
    br.readLine();
    while (count <= maxCount && (line = br.readLine()) != null){

      String[] lineArray = line.split(splitBy);

      if(!containsNull(lineArray)){
        populateMovieList(lineArray, MovieList);
        count++;
      }
    }
  }

  public static void populateCPI(String[] lineArray){

    //Map<Integer,Double> record = new HashMap<Integer, Double>();
    LocalDate date = LocalDate.parse(lineArray[0]);
    int year = date.getYear();
    double consumerIndex = Double.parseDouble(lineArray[1]);

    //System.out.println("Year: "+year);

    cpiMap.put(year, consumerIndex);
    //cpiList.add(0, record);

  }

  public static void populateCPIMissing(){

    int missingLength = missingYears.length;

    for(int i = 0; i < missingLength; i++){
      Map<Integer,Double> record = new HashMap<Integer, Double>();
      cpiMap.put(missingYears[i], missingCPIs[i]);
    }
  }

  public static void populateMovieList(String[] lineArray, List<Map<String,Integer>> MovieList){

    Map<String,Integer> record = new HashMap<String,Integer>();

    record.put("num_critic_for_reviews", Integer.parseInt(lineArray[2]));
    record.put("director_facebook_likes", Integer.parseInt(lineArray[4]));
    record.put("actor_3_facebook_likes", Integer.parseInt(lineArray[5]));
    record.put("actor_1_facebook_likes", Integer.parseInt(lineArray[7]));
    record.put("gross", Integer.parseInt(lineArray[8]));
    record.put("num_voted_users", Integer.parseInt(lineArray[12]));
    record.put("budget", Integer.parseInt(lineArray[22]));
    record.put("year", Integer.parseInt(lineArray[23]));
    record.put("actor_2_facebook_likes", Integer.parseInt(lineArray[24]));

    MovieList.add(0, record);
  }

  //------------------*** PERCEPTRON LEARNING ***----------------------------//

  public static boolean allClassified(Map<String,Double>randWeights){

    int i = 0;
    while(i < movieListFormatted.size()){
      if(!mappingWorks(movieListFormatted.get(i), randWeights)){
        return false;
      }
      i++;
    }
    return true;
  }

  public static Map<String,Double> createRandWeights(){

    Random rand = new Random();
    Map<String,Double> randWeights = new HashMap<String,Double>();

    randWeights.put("actor_1_facebook_likes", rand.nextDouble()*rand.nextInt(50));
    randWeights.put("actor_2_facebook_likes", rand.nextDouble()*rand.nextInt(50));
    randWeights.put("actor_3_facebook_likes", rand.nextDouble()*rand.nextInt(50));
    randWeights.put("budget", rand.nextDouble()*rand.nextInt(50));
    randWeights.put("director_facebook_likes", rand.nextDouble()*rand.nextInt(50));
    randWeights.put("gross", rand.nextDouble()*rand.nextInt(50));
    randWeights.put("num_critic_for_reviews", rand.nextDouble()*rand.nextInt(50));
    randWeights.put("num_voted_users", rand.nextDouble()*rand.nextInt(50));
    randWeights.put("threshold", rand.nextDouble()*rand.nextInt(50));

    return randWeights;
  }

  public static Double findSigma(Map<String,Integer> movie, Map<String,Double>weights) {
      double WIzero = weights.get("threshold")*(-1);
      double WIactor1 = weights.get("actor_1_facebook_likes")*Double.valueOf(movie.get("actor_1_facebook_likes"));
      double WIactor2 = weights.get("actor_2_facebook_likes")*Double.valueOf(movie.get("actor_2_facebook_likes"));
      double WIactor3 = weights.get("actor_3_facebook_likes")*Double.valueOf(movie.get("actor_3_facebook_likes"));
      double WIbudget = weights.get("budget")*Double.valueOf(movie.get("budget"));
      double WIdirector = weights.get("director_facebook_likes")*Double.valueOf(movie.get("director_facebook_likes"));
      double WIcritics = weights.get("num_critic_for_reviews")*Double.valueOf(movie.get("num_critic_for_reviews"));
      double WIusers = weights.get("num_voted_users")*Double.valueOf(movie.get("num_voted_users"));

      double sigma = WIzero+WIactor1+WIactor3+WIbudget+WIdirector+WIcritics+WIusers;
      return sigma;
  }

  public static boolean mappingWorks(Map<String,Integer> movie, Map<String,Double>randWeights){

    double label = movie.get("gross");
    double sigma = findSigma(movie, randWeights);

    if(label == 1){
      if(sigma >= 0){
        return true;
      }
      else{
        return false;
      }
    }
    else{
      if(sigma < 0){
        return true;
      }
      else{
        return false;
      }
    }
  }

public static Map<String,Double> perceptronLearning(){

    Map<String,Double> randWeights = createRandWeights();
    // System.out.println("randWeights: "+randWeights);
    int maxEpochs = 5;
    int currentEpoch = 0;
    while(!allClassified(randWeights) && currentEpoch < maxEpochs){
        System.out.println("-----------Epoch: "+currentEpoch+"----------");
        int i = 0;
        while(i < movieListFormatted.size()){
            System.out.println("movie index: "+i);
            if(!mappingWorks(movieListFormatted.get(i), randWeights)){
            randWeights = fixWeights(randWeights, i);
            }
            i++;
        }
        currentEpoch++;
    }
    return randWeights;
}

  public static Map<String,Double> fixWeights(Map<String,Double> oldWeights, int movieIndex){

      Map<String,Integer> movie = movieListFormatted.get(movieIndex);
      double target = movie.get("gross");
      double output = 5;
      if (findSigma(movie, oldWeights) >= 0) {
          output = 1;
      } else {
          output = 0;
      }

      double newZero = oldWeights.get("threshold") + (learningRate*(-1) * (target-output));
      double actor1NewWeight = oldWeights.get("actor_1_facebook_likes") + (learningRate * Double.valueOf(movie.get("actor_1_facebook_likes") * (target-output)));
      double actor2NewWeight = oldWeights.get("actor_2_facebook_likes") + (learningRate * Double.valueOf(movie.get("actor_2_facebook_likes") * (target-output)));
      double actor3NewWeight = oldWeights.get("actor_3_facebook_likes") + (learningRate * Double.valueOf(movie.get("actor_3_facebook_likes") * (target-output)));
      double budgetNewWeight = oldWeights.get("budget") + (learningRate * Double.valueOf(movie.get("budget") * (target-output)));
      double directorNewWeight = oldWeights.get("director_facebook_likes") + (learningRate * Double.valueOf(movie.get("director_facebook_likes") * (target-output)));
      double criticsNewWeight = oldWeights.get("num_critic_for_reviews") + (learningRate * Double.valueOf(movie.get("num_critic_for_reviews") * (target-output)));
      double usersNewWeight = oldWeights.get("num_voted_users") + (learningRate * Double.valueOf(movie.get("num_voted_users") * (target-output)));

      Map<String,Double> newWeights = new HashMap<String,Double>();
      newWeights.put("actor_1_facebook_likes",actor1NewWeight);
      newWeights.put("actor_2_facebook_likes",actor2NewWeight);
      newWeights.put("actor_1_facebook_likes",actor1NewWeight);
      newWeights.put("budget", budgetNewWeight);
      newWeights.put("director_facebook_likes",directorNewWeight);
      newWeights.put("num_critic_for_reviews",criticsNewWeight);
      newWeights.put("num_voted_users",usersNewWeight);

      return newWeights;
  }

  public static void evaluate(Map<String,Double> testingWeights){
      double posClassified = 0;
      double posCorrect = 0;
      double negClassified = 0;
      double negCorrect = 0;

      int movieIndex = 0;
      while (movieIndex < testMovieListFormatted.size()) {
          Map<String,Integer> movie = testMovieListFormatted.get(movieIndex);
          if (movie.get("gross") == 1) {
              posClassified ++;
              if (mappingWorks(movie, testingWeights)) {
                  posCorrect ++;
              }
          } else {
              negClassified ++;
              if (mappingWorks(movie, testingWeights)) {
                  negCorrect ++;
              }
          }

          movieIndex++;
      }
      double accuracy = (posCorrect+negCorrect) / (posClassified+negClassified);
      double posPrecision = posCorrect / posClassified;
      double negPrecision = negCorrect / negClassified;

      System.out.println("Accuracy: "+(posCorrect+negCorrect)+"/"+(posClassified+negClassified)+"= "+(accuracy*100)+"%");
      System.out.println("Precision (pos): "+posCorrect+"/"+posClassified+"= "+(posPrecision*100)+"%");
      System.out.println("Precision (neg): "+negCorrect+"/"+negClassified+"= "+(negPrecision*100)+"%");
  }

  // TO DO:
  // Make sure the following functions work for test data as well, and update
  // main method accordingly. Also fix main method to run evaluate as well.







}
