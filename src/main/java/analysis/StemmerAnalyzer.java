package analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.tartarus.snowball.ext.KpStemmer;
import org.tartarus.snowball.ext.PorterStemmer;


public class StemmerAnalyzer extends Analyzer {

  public StemmerAnalyzer() {
    super();

  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    Tokenizer tokenizer = new WhitespaceTokenizer();
    TokenStream sfilter = new SnowballFilter(tokenizer, new PorterStemmer());
    return new TokenStreamComponents(tokenizer, sfilter);
  }
}
