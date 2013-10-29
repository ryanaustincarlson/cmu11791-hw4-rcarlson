package edu.cmu.lti.f13.hw4.hw4_rcarlson.casconsumers;

import java.util.Map;

/**
 * This is just a helpful struct to store info the retrieval evaluator needs.
 * 
 * @author Ryan Carlson (rcarlson)
 */

public class RetrievalEvaluatorDocument implements Comparable<RetrievalEvaluatorDocument> {
  public Integer qId;

  public boolean isQuery;
  public boolean isRelevant;
  public String text;
  public Map<String, Integer> wordFrequencies;
  public Double score;

  @Override
  public int compareTo(RetrievalEvaluatorDocument o) {
    if (qId.equals(o.qId)) {
      return -1 * score.compareTo(o.score);
    }
    return qId.compareTo(o.qId);
  }

  @Override
  public String toString() {
    return "qID: " + qId + ", isQuery: " + isQuery + ", isRelevant: " + isRelevant + ", score: "
            + score + "\n\ttext: " + text + " (len=" + text.length() + ")\n\twordFrequencies: " + wordFrequencies + "\n";
  }
}
