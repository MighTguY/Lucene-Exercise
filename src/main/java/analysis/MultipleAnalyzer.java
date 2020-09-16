package analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.CharsRefBuilder;

public class MultipleAnalyzer extends Analyzer {
  private static final String[] SPECIALWORD_STOP_WORDS = {
      "abstract", "implements", "extends", "null", "new",
      "switch", "case", "default", "synchronized",
      "do", "if", "else", "break", "continue", "this",
      "assert", "for", "transient",
      "final", "static", "catch", "try",
      "throws", "throw", "class", "finally", "return",
      "const", "native", "super", "while", "import",
      "package", "true", "false"};
  private static final String[] ENGLISH_STOP_WORDS = {
      "a", "an", "and", "are", "as", "at", "be", "but",
      "by", "for", "if", "in", "into", "is", "it",
      "no", "not", "of", "on", "or", "s", "such",
      "that", "the", "their", "then", "there", "these",
      "they", "this", "to", "was", "will", "with", "lucky"};
  private CharArraySet specialStopSet;
  private CharArraySet englishStopSet;

  public MultipleAnalyzer() {
    super();
    specialStopSet = StopFilter.makeStopSet(SPECIALWORD_STOP_WORDS);
    englishStopSet = StopFilter.makeStopSet(ENGLISH_STOP_WORDS);
  }

  private static void addSynonym(String input, String output, SynonymMap.Builder builder) {
    final CharsRef inputWords = SynonymMap.Builder.join(input.split(" "), new CharsRefBuilder());
    final CharsRef outputWords = SynonymMap.Builder.join(output.split(" "), new CharsRefBuilder());
    builder.add(inputWords, outputWords, true);
  }

  private SynonymMap getSynonymMap() {
    SynonymMap synMap = null;
    try {
      SynonymMap.Builder builder = new SynonymMap.Builder(true);
      addSynonym("lucene", "solr", builder);
      addSynonym("lucene", "elasticsearch", builder);
      addSynonym("train", "study", builder);
      addSynonym("sea green", "color", builder);
      synMap = builder.build();
    } catch (Exception ex) {
      System.out.print("error occured");
    }
    return synMap;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    Tokenizer tokenizer = new StandardTokenizer();
    TokenStream tokenStream = new StopFilter(new LowerCaseFilter(new StandardFilter(tokenizer)),
        englishStopSet);
    SynonymMap synMap = getSynonymMap();
    TokenStream stream = new SynonymGraphFilter(tokenStream, synMap, true);
    return new TokenStreamComponents(tokenizer, new RemoveDuplicatesTokenFilter(stream));
  }
}
