package analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class AnalyzerTest {
  private final static Version version = Version.LUCENE_6_6_0;
  private static final String INDEX_DIR = "/tmp/luceneidx_analyzer_test";
  private boolean simpleTextCodec = true;

  public static Directory getDirectory(String name) throws IOException {
    if (name.equals("ram")) {
      return new RAMDirectory();
    } else if (name.equals("dir")) {
      return FSDirectory.open(Paths.get(INDEX_DIR));
    }
    return null;
  }

  public static void main(String args[]) throws Exception {
    new AnalyzerTest().testAnalyzer();
  }

  public void initDocs(Directory index, IndexWriterConfig config) throws IOException {
    IndexWriter writer = new IndexWriter(index, config);

    Document doc = new Document();
    doc.add(new TextField("author", "kitty cat", Field.Store.YES));
    doc.add(new TextField("email", "kitty@cat.com", Field.Store.YES));
    doc.add(new TextField("email", "kitty2@cat.com", Field.Store.YES));
    doc.add(new TextField("specials", "13e12exoxoe45e66", Field.Store.YES));
    doc.add(new TextField("specials_synonyms", "green is my favourite Elephant", Field.Store.YES));
    doc.add(new TextField("specials_stop", "hi i am not your friend browny brother", Field.Store.YES));
    doc.add(new TextField("specials_stem", "hi its a big day for us to stay together girls boys", Field.Store.YES));
    doc.add(new TextField("specials_multiple", "hi its a big day for us to stay together as lucky is the steming " +
        "word hie lucene" +
        " ", Field.Store
        .YES));
    writer.addDocument(doc);
    writer.commit();
    writer.close();

  }

  public void addAnalyser(Map<String, Analyzer> analyzerPerField) {

    analyzerPerField.put("email", new KeywordAnalyzer());
    analyzerPerField.put("specials", new ECharacterAnalyser(version));
    analyzerPerField.put("specials_stop", new StopAnalyzer());
    analyzerPerField.put("specials_synonyms", new SynonymAnalyzer());
    analyzerPerField.put("specials_stem", new StemmerAnalyzer());
    analyzerPerField.put("specials_multiple", new MultipleAnalyzer());

  }

  public IndexWriterConfig getWriterConfig() {
    Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
    addAnalyser(analyzerPerField);
    PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(
        new StandardAnalyzer(), analyzerPerField);
    IndexWriterConfig config = new IndexWriterConfig(analyzer)
        .setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    if (simpleTextCodec) {
      Codec textCodec = new SimpleTextCodec();
      config.setCodec(textCodec);
    }
    return config;
  }

  public void testAnalyzer() throws IOException, ParseException {
    Directory index = getDirectory("dir");
    IndexWriterConfig config = getWriterConfig();
    initDocs(index, config);
    int limit = 20;
    try (IndexReader reader = DirectoryReader.open(index)) {

      Query query = new TermQuery(new Term("email", "kitty@cat.com"));
      printSearchResults(limit, query, reader);

      query = new TermQuery(new Term("specials", "xoxo"));
      printSearchResults(limit, query, reader);

      query = new TermQuery(new Term("author", "cat"));
      printSearchResults(limit, query, reader);

      query = new TermQuery(new Term("specials_synonyms", "color"));
      printSearchResults(limit, query, reader);

      query = new TermQuery(new Term("specials_synonyms", "animal"));
      printSearchResults(limit, query, reader);

      query = new TermQuery(new Term("specials_stop", "brother"));
      printSearchResults(limit, query, reader);

//
      query = new TermQuery(new Term("specials_stem", "dai"));
      printSearchResults(limit, query, reader);
//
      query = new TermQuery(new Term("specials_multiple", "lucky"));
      printSearchResults(limit, query, reader);
//
      query = new QueryParser("specials_stem", new StemmerAnalyzer()).parse("specials_stem:day");
      printSearchResults(limit, query, reader);
    }

    index.close();

  }

  private void printSearchResults(
      final int limit, final Query query,
      final IndexReader reader) throws IOException {
    IndexSearcher searcher = new IndexSearcher(reader);
    TopDocs docs = searcher.search(query, limit);

    System.out.println(docs.totalHits + " found for query: " + query);

    for (final ScoreDoc scoreDoc : docs.scoreDocs) {
      System.out.println(searcher.doc(scoreDoc.doc));
    }
  }
}

