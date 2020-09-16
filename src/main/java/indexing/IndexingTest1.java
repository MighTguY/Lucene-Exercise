package indexing;

import java.nio.file.Paths;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;


public class IndexingTest1 {
  protected long[] ids = {1, 2};
  protected String[] country = {"Netherlands", "Italy"};
  protected String[] contents = {"Amsterdam has lots of bridges lucky ",
      "Venice has lots of canals lucky"};
  protected String[] city = {"Amsterdam", "Venice"};
  protected String[] city_other = {"Venice", "Delhi"};
  private Directory directory;
  private static final String INDEX_DIR = "/tmp/luceneidx_indexing_test12";

  public static void main(String args[]) throws Exception {
    IndexingTest1 t = new IndexingTest1();
    t.init();
    t.indexReaderTest();
  }

  private IndexWriter getWriter() throws IOException {
    IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
    Codec textCodec = new SimpleTextCodec();
    config.setCodec(textCodec);
    return new IndexWriter(directory, config);
  }

  private IndexSearcher getSearrcher() throws IOException {
    IndexReader indexReader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(indexReader);
    return searcher;
  }

  public void init() throws Exception {
    directory = FSDirectory.open(Paths.get(INDEX_DIR));
    IndexWriter writer = getWriter();
    ;

    FieldType storedField = new FieldType();
    storedField.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    storedField.setStored(true);
    storedField.setTokenized(true);

    FieldType storeTermVectorsField = new FieldType();
    storeTermVectorsField.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
    storeTermVectorsField.setStored(true);
    storeTermVectorsField.setOmitNorms(true);
    storeTermVectorsField.setTokenized(true);
    storeTermVectorsField.setStoreTermVectors(true);

    FieldType textField = new FieldType();
    textField.setIndexOptions(IndexOptions.DOCS);
    textField.setStored(true);
    textField.setTokenized(true);
    textField.setStoreTermVectors(true);
    textField.setStoreTermVectorPositions(true);
    textField.setStoreTermVectorOffsets(true);
    textField.setStoreTermVectorPayloads(true);

    FieldType docValueFields = new FieldType();
    docValueFields.setIndexOptions(IndexOptions.DOCS);
    docValueFields.setStored(true);

    docValueFields.setTokenized(true);
    docValueFields.setStoreTermVectors(true);
    docValueFields.setStoreTermVectorPositions(true);
    docValueFields.setStoreTermVectorOffsets(true);
    docValueFields.setStoreTermVectorPayloads(true);
    docValueFields.setDocValuesType(DocValuesType.NUMERIC);

    FacetsConfig config = new FacetsConfig();
    config.setMultiValued("city", true);
    config.setIndexFieldName("city", "city_f");

    for (int i = 0; i < ids.length; i++) {
      Document doc = new Document();
      doc.add(new NumericDocValuesField("id", ids[i]));
      doc.add(new Field("country", country[i],
          storedField));
      doc.add(new Field("contents", contents[i],
          storeTermVectorsField));
      doc.add(new SortedSetDocValuesFacetField("city", city[i]));
      doc.add(new SortedSetDocValuesFacetField("city", city_other[i]));

//      doc.add(new Field("city", city[i],
//          textField));
//      doc.add(new Field("city", city_other[i],
//          textField));
      writer.addDocument(config.build(doc));
    }
    writer.close();

  }

  public void indexReaderTest() throws IOException {
    IndexSearcher searcher = getSearrcher();
    Term t = new Term("contents", "lots");
    Query query = new TermQuery(t);
    TopDocs topDocs = searcher.search(query, 10, new Sort(new SortField("id", SortField.Type.INT, true)));
    System.out.println("Data");
    if (topDocs.totalHits > 0) {
      for (ScoreDoc scdoc : topDocs.scoreDocs) {
        System.out.println(searcher.doc(scdoc.doc));
      }
    }
    FacetsCollector fc = new FacetsCollector();
    FacetsCollector.search(searcher, query, 10, fc);
    SortedSetDocValuesReaderState state =
        new DefaultSortedSetDocValuesReaderState(searcher.getIndexReader(), "city_f");

    Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
    FacetResult result = facets.getTopChildren(10, "city");
    for (int i = 0; i < result.childCount; i++) {
      LabelAndValue lv = result.labelValues[i];
      System.out.println(String.format("%s (%s)", lv.label, lv.value));
    }

  }
}
