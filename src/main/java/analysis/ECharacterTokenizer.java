package analysis;

import org.apache.lucene.analysis.util.CharTokenizer;

public class ECharacterTokenizer extends CharTokenizer {

  @Override
  protected boolean isTokenChar(int character) {
    return 'e' != character;
  }
}
