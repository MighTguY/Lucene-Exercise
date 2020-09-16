package indexing;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.junit.Assert;

import java.io.IOException;
import java.nio.file.Paths;

public class IndexingTest {

  private final static Version version = Version.LUCENE_6_6_0;
  private static final String INDEX_DIR = "/tmp/luceneidx_indexing_test1";
  private static boolean simpleTextCodec = true;
  private static IndexWriter indexWriter;

  public IndexingTest() throws Exception {
    indexWriter = getIndexWriter();
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
    IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer())
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

  public static void closeWriter() throws IOException {
    indexWriter.close();
  }

  public static void main(String args[]) throws Exception {
    IndexingTest t = new IndexingTest();
    t.addDocsTest();
    // t.deleteDocsTest();
//    t.updateDocsTest();
    //t.testMerge();
    closeWriter();
  }

  public void addDocsTest() throws IOException {

    Document doc = new Document();
    doc.add(new TextField("id", "1", Field.Store.YES));
    doc.add(new SortedDocValuesField("doc_s_dV", new BytesRef("123")));
    doc.add(new DoubleDocValuesField("doc_d_dV", 1.23));
    doc.add(new TextField("author", "kitty cat", Field.Store.NO));
    doc.add(new TextField("email", "kitty@cat.com", Field.Store.YES));
    doc.add(new TextField("specials", "13e12exoxoe45e66", Field.Store.YES));
    doc.add(new TextField("specials_synonyms", "green is my favourite Elephant", Field.Store.YES));
    doc.add(new TextField("specials_stop", "hi i am not your friend browny brother", Field.Store.YES));
    doc.add(new TextField("specials_stem", "hi its a big day for us to stay together ", Field.Store.YES));
    doc.add(new TextField("specials_multiple", "hi its a big day for us to stay together as lucky is the steming " +
        "word hie lucene" +
        " ", Field.Store
        .YES));
    indexWriter.addDocument(doc);
    indexWriter.addDocument(updatedDoc());
    indexWriter.commit();


    IndexReader indexReader = DirectoryReader.open(getDirectory("dir"));
    Assert.assertEquals(1, indexReader.numDocs());
    Assert.assertEquals(indexReader.numDeletedDocs(), 0);
    System.out.println("All assetion passed");
  }

  void deleteDocsTest() throws IOException {

    Assert.assertEquals(1, indexWriter.numDocs());

    IndexReader indexReader = DirectoryReader.open(getDirectory("dir"));
    Query query = new TermQuery(new Term("email", "kitty"));
    query = new TermQuery(new Term("id", "1"));
    printSearchResults(10, query, indexReader);
    //long val = writer.tryDeleteDocument(indexReader,0);
    long val = indexWriter.deleteDocuments(query);


    Assert.assertTrue(indexWriter.hasDeletions());
    indexWriter.commit();
    Assert.assertEquals(0, indexWriter.numDocs());
    System.out.println("All assetion passed");
    indexWriter.close();

  }

  void updateDocsTest() throws IOException {
    IndexReader indexReader = DirectoryReader.open(getDirectory("dir"));
    IndexSearcher searcher = new IndexSearcher(indexReader);
    Query query = new TermQuery(new Term("id", "1"));
    TopDocs docs = searcher.search(query, 10);

    if (docs.totalHits > 0) {
      Document doc = searcher.doc(docs.scoreDocs[0].doc);
      doc.removeField("id");
      doc.add(new TextField("id", "2", Field.Store.YES));
      indexWriter.updateDocument(new Term("email", "kitty"), doc);
     // indexWriter.updateDocuments()
      indexWriter.commit();
    }

    printSearchResults(10, query, indexReader);
    indexReader.close();
    indexReader = DirectoryReader.open(getDirectory("dir"));
    query = new TermQuery(new Term("id", "2"));
    printSearchResults(10, query, indexReader);

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

  public Document updatedDoc() {
    Document doc = new Document();
    doc.add(new TextField("id", "2", Field.Store.YES));
    doc.add(new TextField("author", "logan cat", Field.Store.YES));
    doc.add(new TextField("email", "logan@cat.com", Field.Store.YES));
    doc.add(new SortedDocValuesField("doc_s_dV", new BytesRef("1243")));
    doc.add(new DoubleDocValuesField("doc_d_dV", 3.23));
    doc.add(new TextField("email", "logan2@cat.com", Field.Store.YES));
    doc.add(new TextField("specials", "13e12exoxoe45e66", Field.Store.YES));
    doc.add(new TextField("specials_synonyms", "red is my favourite Elephant", Field.Store.YES));
    doc.add(new TextField("specials_stop", "hi i am not your friend redy brother", Field.Store.YES));
    doc.add(new TextField("specials_stem", "hi its a big day for us to stayed together ", Field.Store.YES));
    doc.add(new TextField("specials_multiple", "hi its a big day for us to stay together as lucky is the steming " +
        "word hie lucene" +
        " ", Field.Store
        .YES));
    return doc;
  }

  public void testMerge() throws IOException {
    Document doc = updatedDoc();
    indexWriter.addDocument(doc);
    indexWriter.commit();

    System.out.println(indexWriter.numDocs());
    indexWriter.forceMerge(1);

  }
}
