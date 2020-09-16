package analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;

public class ECharacterAnalyser extends Analyzer {

  private final Version version;

  public ECharacterAnalyser(final Version version) {
    this.version = version;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    Tokenizer tokenizer = new ECharacterTokenizer();
    TokenStream filter = new LowerCaseFilter(tokenizer);
//  TokenStream stream = new LowerCaseFilter(new ECharacterTokenizer()) ;

    return new TokenStreamComponents(tokenizer, filter);
  }
}
