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

public class RevenuePredictor{

  static Map<Integer,Double> cpiMap = new HashMap<Integer, Double>();
  static List<Map<String, Integer>> movieList = new ArrayList<Map<String, Integer>>();
  static List<Map<String, Integer>> movieListAdjusted = new ArrayList<Map<String, Integer>>();
  static int[] missingYears = new int[]{2015, 2016, 2017, 2018, 2019, 2020};
  static double[] missingCPIs = new double[]{233.707, 236.916, 242.839, 247.876, 251.712, 257.971};

  public static void main(String[] args) throws Exception{

    initializeMovies();
    initializeCPI();
    createAdjusted();
    //System.out.println("cpiMap: "+cpiMap);
    //System.out.println("movieList: "+movieList);
    System.out.println("movieListAdjusted: "+movieListAdjusted);

  }

  public static double adjustInflation(int val, int year){
    System.out.println("val: "+val);
    double cpi = cpiMap.get(year);
    System.out.println("cpi: "+cpi);
    double valAdjusted = (100/cpi)*val;
    System.out.println("valAdjusted: "+valAdjusted);
    return valAdjusted;
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

  public static void createAdjusted(){

    int movieSize = movieList.size();
    for(int i = 0; i < movieSize; i++){

      Map<String,Integer> record = new HashMap<String, Integer>(movieList.get(i));

      int year = record.get("year");
      int adjustedBudget = (int)adjustInflation(record.get("budget"), year);
      int adjustedGross = (int)adjustInflation(record.get("gross"), year);

      record.replace("budget", adjustedBudget);
      record.replace("gross", adjustedGross);

      System.out.println("adjustedBudget: "+adjustedBudget);
      //System.out.println("adjustedGross: "+adjustedGross);

      movieListAdjusted.add(0, record);
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

  public static void initializeMovies() throws Exception{

    String line = "";
    String splitBy = ",";
    int count = 0;

    BufferedReader br = new BufferedReader(new FileReader("movie_metadata.csv"));
    br.readLine();
    while (count <= 4000 && (line = br.readLine()) != null){

      String[] lineArray = line.split(splitBy);

      if(!containsNull(lineArray)){
        populateMovieList(lineArray);
        count++;
      }
    }
  }

  public static void populateCPI(String[] lineArray){

    //Map<Integer,Double> record = new HashMap<Integer, Double>();
    LocalDate date = LocalDate.parse(lineArray[0]);
    int year = date.getYear();
    double consumerIndex = Double.parseDouble(lineArray[1]);

    System.out.println("Year: "+year);

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

  public static void populateMovieList(String[] lineArray){

    Map<String,Integer> record = new HashMap<String, Integer>();

    record.put("num_critic_for_reviews", Integer.parseInt(lineArray[2]));
    record.put("director_facebook_likes", Integer.parseInt(lineArray[4]));
    record.put("actor_3_facebook_likes", Integer.parseInt(lineArray[5]));
    record.put("actor_1_facebook_likes", Integer.parseInt(lineArray[7]));
    record.put("gross", Integer.parseInt(lineArray[8]));
    record.put("num_voted_users", Integer.parseInt(lineArray[12]));
    record.put("budget", Integer.parseInt(lineArray[22]));
    record.put("year", Integer.parseInt(lineArray[23]));
    record.put("actor_2_facebook_likes", Integer.parseInt(lineArray[24]));

    movieList.add(0, record);
  }
}
