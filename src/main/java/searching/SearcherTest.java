package searching;

import analysis.SynonymAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.junit.Assert;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class SearcherTest {
  private final static Version version = Version.LUCENE_6_6_0;
  private static final String INDEX_DIR = "/tmp/luceneidx_searching_test";
  private static boolean simpleTextCodec = true;
  private static IndexWriter indexWriter;
  private static IndexReader indexReader;

  public SearcherTest() throws Exception {
    indexWriter = getIndexWriter();

  }

  private static IndexReader getIndexReader() throws IOException {
    Directory index = getDirectory("dir");
    return DirectoryReader.open(index);
  }

  public static Directory getDirectory(String name) throws IOException {
    if (name.equals("ram")) {
      return new RAMDirectory();
    } else if (name.equals("dir")) {
      return FSDirectory.open(Paths.get(INDEX_DIR));
    }
    return null;
  }

  public static IndexWriterConfig getWriterConfig() {
    Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
    analyzerPerField.put("specials_synonyms", new SynonymAnalyzer());
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

  public static IndexWriter getIndexWriter() throws IOException {
    Directory index = getDirectory("dir");
    IndexWriterConfig config = getWriterConfig();
    return new IndexWriter(index, config);

  }

  public static void main(String args[]) throws Exception {
    SearcherTest test = new SearcherTest();
    test.addDocs();
    test.search();
  // test.sortExample();
  }

  public void addDocs() throws IOException {

    Document doc = new Document();
    doc.add(new TextField("id", "g", Field.Store.YES));
    doc.add(new SortedDocValuesField("id_sort", new BytesRef("9")));
    doc.add(new TextField("author", "kitty cat", Field.Store.YES));
    doc.add(new TextField("email", "kitty@cat.com", Field.Store.YES));
    doc.add(new TextField("email", "kitty2@cat.com", Field.Store.YES));
    doc.add(new TextField("specials", "13e12exoxoe45e66", Field.Store.YES));
    doc.add(new TextField("specials_synonyms", "green Elephant is my favourite Elephant", Field.Store.YES));
    doc.add(new TextField("specials_stop", "hi i am not your friend browny brother", Field.Store.YES));
    doc.add(new TextField("specials_stem", "hi its a big day for us to stay together ", Field.Store.YES));
    doc.add(new TextField("specials_multiple", "hi its a big day for us to stay together as lucky is the steming " +
        "word hie lucene" +
        " ", Field.Store
        .YES));
    indexWriter.addDocument(doc);
    indexWriter.addDocument(newDOc());
    indexWriter.commit();


    IndexReader indexReader = DirectoryReader.open(getDirectory("dir"));
    Assert.assertEquals(2, indexReader.numDocs());
    Assert.assertEquals(indexReader.numDeletedDocs(), 0);
    System.out.println("Docs added ");
  }

  public Document newDOc() {
    Document doc = new Document();
    doc.add(new TextField("id", "1234", Field.Store.YES));
    doc.add(new SortedDocValuesField("id_sort", new BytesRef("2")));
    doc.add(new TextField("author", "logan cat", Field.Store.YES));
    doc.add(new TextField("email", "logan@cat.com", Field.Store.YES));
    doc.add(new TextField("email", "logan2@cat.com", Field.Store.YES));
    doc.add(new TextField("specials", "13e12exoxoe45e66", Field.Store.YES));
    doc.add(new TextField("specials_synonyms", "red is my favourite Elephant hello", Field.Store.YES));
    doc.add(new TextField("specials_stop", "hi i am not your friend redy brother", Field.Store.YES));
    doc.add(new TextField("specials_stem", "hi its a big day for us to stayed together ", Field.Store.YES));
    doc.add(new TextField("specials_multiple", "hi its a big day for us to stay together as lucky is the steming " +
        "word hie lucene" +
        " ", Field.Store
        .YES));
    return doc;
  }

  public void search() throws IOException, ParseException {
    indexReader = getIndexReader();
//    searchByTemQuery();
//    searchByTemRangeQuery();
//    searchByPrefixQuery();
//    searchByPhraseQuery();
//    searchByWildCardQuery();
//    searchByFuzzyQuery();
//    searchByMatchAllDocs();
    searchByUserQuery();

  }

  private void searchByUserQuery() throws IOException, ParseException {
    System.out.println(new Object() {
    }.getClass().getEnclosingMethod().getName());
    Query query = new QueryParser("specials_synonyms", new SynonymAnalyzer()).parse("specials_synonyms:((elephant " +
        "AND favourite) NOT hello)");
    printSearchResults(20, query, indexReader);
  }

  private void searchByMatchAllDocs() throws IOException {
    System.out.println(new Object() {
    }.getClass().getEnclosingMethod().getName());
    Query query = new MatchAllDocsQuery();
    printSearchResults(20, query, indexReader);
  }

  private void searchByFuzzyQuery() throws IOException {
    System.out.println(new Object() {
    }.getClass().getEnclosingMethod().getName());
    Query query = new FuzzyQuery(new Term("author", "cut"));
    printSearchResults(20, query, indexReader);
  }

  private void searchByWildCardQuery() throws IOException {
    System.out.println(new Object() {
    }.getClass().getEnclosingMethod().getName());
    Query query = new WildcardQuery(new Term("author", "?it*"));
    printSearchResults(20, query, indexReader);
  }

  private void searchByPhraseQuery() throws IOException {
    System.out.println(new Object() {
    }.getClass().getEnclosingMethod().getName());
    PhraseQuery.Builder builder = new PhraseQuery.Builder();
    PhraseQuery query = builder.setSlop(10).
        add(new Term("specials_synonyms", "red"))
        .add(new Term("specials_synonyms", "animal"))
        .build();
    printSearchResults(20, query, indexReader);

  }

  private void searchByPrefixQuery() throws IOException {
    System.out.println(new Object() {
    }.getClass().getEnclosingMethod().getName());
    Term term = new Term("id", "12");
    PrefixQuery query = new PrefixQuery(term);
    printSearchResults(20, query, indexReader);
  }

  private void searchByTemRangeQuery() throws IOException {
    System.out.println(new Object() {
    }.getClass().getEnclosingMethod().getName());
    TermRangeQuery query = new TermRangeQuery("id", new BytesRef("0"), new BytesRef("2"),
        true, true);
    printSearchResults(20, query, indexReader);
  }

  private void searchByTemQuery() throws IOException {
    System.out.println(new Object() {
    }.getClass().getEnclosingMethod().getName());

    Term t = new Term("author", "kitty");
    Query query = new TermQuery(t);
    printSearchResults(20, query, indexReader);

  }

  private void printSearchResults(
      final int limit, final Query query,
      final IndexReader reader) throws IOException {
    IndexSearcher searcher = new IndexSearcher(reader);
    TopDocs docs = searcher.search(query, limit);

    System.out.println(docs.totalHits + " found for query: " + query);

    for (final ScoreDoc scoreDoc : docs.scoreDocs) {

      System.out.println("DOC ID:" + scoreDoc.doc + ":" + searcher.doc(scoreDoc.doc));
    }
  }

  private IndexSearcher getSearrcher() throws IOException {
    IndexReader indexReader = DirectoryReader.open(getDirectory("dir"));
    IndexSearcher searcher = new IndexSearcher(indexReader);
    return searcher;
  }

  public void sortExample() throws IOException {
    reverseOrderSortField();
    incOrderSortField();
    indexSortOrder();
    relevanceSort();
  }

  private void relevanceSort() throws IOException {
    System.out.println(new Object() {
    }.getClass().getEnclosingMethod().getName());
    Query query = new TermQuery(new Term("specials_synonyms", "Elephant"));
    query = new BoostQuery(query, 10f);
    IndexSearcher searcher = getSearrcher();
    TopDocs topDocs = searcher.search(query, 10);
    printTopDocs(topDocs, searcher);
  }

  private void incOrderSortField() throws IOException {
    System.out.println(new Object() {
    }.getClass().getEnclosingMethod().getName());
    Query query = new MatchAllDocsQuery();
    IndexSearcher searcher = getSearrcher();
    TopDocs topDocs = searcher.search(query, 10, new Sort(new SortField("id_sort", SortField.Type.STRING, false)));
    printTopDocs(topDocs, searcher);
  }

  private void indexSortOrder() throws IOException {
    System.out.println(new Object() {
    }.getClass().getEnclosingMethod().getName());
    Query query = new MatchAllDocsQuery();
    IndexSearcher searcher = getSearrcher();
    TopDocs topDocs = searcher.search(query, 10, Sort.INDEXORDER);
    printTopDocs(topDocs, searcher);
  }

  public void reverseOrderSortField() throws IOException {
    System.out.println(new Object() {
    }.getClass().getEnclosingMethod().getName());
    Query query = new MatchAllDocsQuery();
    IndexSearcher searcher = getSearrcher();
    TopDocs topDocs = searcher.search(query, 10, new Sort(new SortField("id_sort", SortField.Type.STRING, true)));
    printTopDocs(topDocs, searcher);
  }

  void printTopDocs(TopDocs topDocs, IndexSearcher searcher) throws IOException {
    if (topDocs.totalHits > 0) {
      for (ScoreDoc scdoc : topDocs.scoreDocs) {
        System.out.println("DOCID-" + scdoc.doc + " || SCORE " + scdoc.score + " : " + searcher.doc(scdoc.doc));
      }
    } else {
      System.out.println("0 Results found");
    }
  }

}
