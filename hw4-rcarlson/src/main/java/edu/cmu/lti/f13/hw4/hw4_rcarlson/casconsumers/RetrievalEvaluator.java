package edu.cmu.lti.f13.hw4.hw4_rcarlson.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f13.hw4.hw4_rcarlson.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_rcarlson.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_rcarlson.utils.Utils;

public class RetrievalEvaluator extends CasConsumer_ImplBase {

  public List<RetrievalEvaluatorDocument> documents;

  public void initialize() throws ResourceInitializationException {
    documents = new ArrayList<RetrievalEvaluatorDocument>();
  }

  /**
   * TODO :: 1. construct the global word dictionary 2. keep the word frequency for each sentence
   */
  @Override
  public void processCas(CAS aCas) throws ResourceProcessException {

    JCas jcas;
    try {
      jcas = aCas.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();

    if (it.hasNext()) {
      Document doc = (Document) it.next();

      // Make sure that your previous annotators have populated this in CAS
      FSList fsTokenList = doc.getTokenList();
      ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);

      RetrievalEvaluatorDocument evalDoc = new RetrievalEvaluatorDocument();
      evalDoc.qId = doc.getQueryID();
      evalDoc.isQuery = doc.getRelevanceValue() == 99;
      evalDoc.isRelevant = doc.getRelevanceValue() == 1;
      evalDoc.text = doc.getText();

      // Do something useful here
      Map<String, Integer> wordFrequencies = new HashMap<String, Integer>();
      for (Token token : tokenList) {
        wordFrequencies.put(token.getText(), token.getFrequency());
      }
      evalDoc.wordFrequencies = wordFrequencies;

      documents.add(evalDoc);
    }
  }

  /**
   * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2. Compute the MRR metric
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {

    super.collectionProcessComplete(arg0);

    int current_qID = -1;
    Map<String, Integer> currentQueryVector = null;
    String currentQueryText = null;
    for (RetrievalEvaluatorDocument document : documents) {
      if (document.qId != current_qID) {
        current_qID = document.qId;
        if (!document.isQuery) {
          throw new IOException("Query not found as anticipated! Malformed doc list!");
        }
        currentQueryVector = document.wordFrequencies;
        currentQueryText = document.text;
        document.score = 1000d;
      } else {
        double similarity = computeCosineSimilarity(currentQueryVector, document.wordFrequencies);
        double textLenFeature = computeTextLenFeature(currentQueryText, document.text);
        double weight = .5;
        document.score = weight * similarity + (1-weight) * textLenFeature;
//        document.score = similarity;
      }
    }

    Collections.sort(documents);
//    for (RetrievalEvaluatorDocument doc : documents)
//    {
//      System.out.println(doc);
//    }

    double metric_mrr = compute_mrr();
    System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
  }
  
  private double computeTextLenFeature(String queryText, String documentText) {
    return 1./Math.abs(queryText.length() - documentText.length()); 
  }

  /**
   * 
   * @return cosine_similarity
   */
  private double computeCosineSimilarity(Map<String, Integer> queryVector,
          Map<String, Integer> docVector) {
    double cosine_similarity = 0.0;
    
    TreeSet<String> vocab = new TreeSet<String>(queryVector.keySet());
    vocab.addAll(docVector.keySet());
    
    double numerator = 0;
    double queryMagnitude = 0;
    double docMagnitude = 0;
    
    for (String token : vocab) {
      double queryValue = queryVector.containsKey(token) ? queryVector.get(token) : 0;
      double docValue = docVector.containsKey(token) ? docVector.get(token) : 0;
      
      numerator += queryValue * docValue;
      queryMagnitude += queryValue * queryValue;
      docMagnitude += docValue * docValue;
    }
    
    queryMagnitude = Math.sqrt(queryMagnitude);
    docMagnitude = Math.sqrt(docMagnitude);
    
    cosine_similarity = numerator / (queryMagnitude * docMagnitude);
    
    return cosine_similarity;
  }

  /**
   * 
   * @return mrr
   */
  private double compute_mrr() {
    double metric_mrr = 0.0;
    
    int current_qID = -1;
    int num_qID = 0;
    int currentRank = 0;
    for (RetrievalEvaluatorDocument document : documents) {
      if (document.qId != current_qID) {
        current_qID = document.qId;
        num_qID++;
        currentRank = 0;
      } else if (document.isRelevant) {
        System.out.println("score: " + document.score + "\trank=" + currentRank + "\trel="
                + document.isRelevant + " qid=" + document.qId);
        metric_mrr += 1 / (1.d * currentRank);
      }
      currentRank++;
    }
    
    metric_mrr /= num_qID;

   return metric_mrr;
  }

}
