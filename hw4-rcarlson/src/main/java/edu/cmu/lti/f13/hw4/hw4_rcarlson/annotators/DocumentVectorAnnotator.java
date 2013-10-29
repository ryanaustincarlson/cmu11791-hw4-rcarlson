package edu.cmu.lti.f13.hw4.hw4_rcarlson.annotators;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f13.hw4.hw4_rcarlson.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_rcarlson.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_rcarlson.utils.Utils;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {
  
  private TokenizerFactory<Word> tokenizerFactory = PTBTokenizerFactory.newTokenizerFactory();

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
		}

	}
	/**
	 * 
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();
		//System.out.println("docText: " + docText);
    Tokenizer<Word> tokenizer = tokenizerFactory.getTokenizer(new StringReader(docText));
    
    Map<String, Token> tokensMap = new HashMap<String, Token>();
    for (Word word : tokenizer.tokenize()) {
      String wordText = word.toString().toLowerCase();
      if (tokensMap.containsKey(wordText)) {
        Token token = tokensMap.get(wordText);
        token.setFrequency(token.getFrequency()+1);
      } else {
        Token token = new Token(jcas);
        // record the location of the first instance of the word 
        token.setBegin(doc.getBegin() + word.beginPosition());
        token.setEnd(doc.getBegin() + word.endPosition());
        token.setText(wordText);
        token.setFrequency(1);
        tokensMap.put(wordText, token);
      }
    }
    Collection<Token> tokenCollection = tokensMap.values();
    FSList tokens = Utils.fromCollectionToFSList(jcas, tokenCollection);
    doc.setTokenList(tokens);
	}
}
