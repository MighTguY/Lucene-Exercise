package analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.CharsRefBuilder;

public class SynonymAnalyzer extends Analyzer {

  private static void addSynonym(String input, String output, SynonymMap.Builder builder) {
    final CharsRef inputWords = SynonymMap.Builder.join(input.split(" "), new CharsRefBuilder());
    final CharsRef outputWords = SynonymMap.Builder.join(output.split(" "), new CharsRefBuilder());
    builder.add(inputWords, outputWords, true);
  }

  private SynonymMap getSynonymMap() {
    SynonymMap synMap = null;
    try {
      SynonymMap.Builder builder = new SynonymMap.Builder(true);
      addSynonym("dark sea green", "color", builder);
      addSynonym("green", "color", builder);
      addSynonym("dark sea", "color", builder);
      addSynonym("elephant", "animal", builder);
      synMap = builder.build();
    } catch (Exception ex) {
      System.out.print("error occured");
    }
    return synMap;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    Tokenizer tokenizer = new WhitespaceTokenizer();
    SynonymMap synMap = getSynonymMap();
    TokenStream stream = new SynonymGraphFilter(tokenizer, synMap, true);
    return new TokenStreamComponents(tokenizer, new RemoveDuplicatesTokenFilter(stream));
  }
}
