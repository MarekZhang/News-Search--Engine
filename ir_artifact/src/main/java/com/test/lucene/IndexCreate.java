package com.test.lucene;

import com.test.lucene.Analyzer.AnalAndSim;
import com.test.lucene.Utility.IRUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Create Index
 */
public class IndexCreate {

    private static Analyzer analyzer;
    static Similarity similarity;
    private final static AnalAndSim anals=new AnalAndSim();

    public static void main(final String[] args) throws IOException {
        if(args.length == 0){
            analyzer = anals.getAnalyzer();
            similarity = new BM25Similarity();
        }
        //analyzer = new EnglishAnalyzer();
        //analyzer = new StandardAnalyzer();
        //customized similarity
//        similarity = anals.getSimilarity();
//        similarity = new BooleanSimilarity();

        long startTime = System.currentTimeMillis();
        //delete all previous index----------------
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
        Directory directory = FSDirectory.open(Paths.get("index"));
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        indexWriter.deleteAll();
        indexWriter.close();
        //------------------------------------------
        final IndexCreate ic=new IndexCreate();
        ic.writeIndex(IRUtils.absPathLA,"DOCNO","HEADLINE","TEXT");
        ic.writeIndex(IRUtils.absPathFedRegister,"DOCNO","DOCTITLE", "TEXT");
        ic.writeIndex(IRUtils.absPathFT,"DOCNO","HEADLINE","TEXT");
        ic.writeIndex(IRUtils.absPathFB,"DOCNO","TI","TEXT");
        long endTime = System.currentTimeMillis();
        System.out.printf("All indexes created, it takes %d seconds", (int)((endTime - startTime) / 1000));
    }
    public void writeIndex(String path, String id, String title, String text) {
        List<Document> FRDoc;
        final IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
        indexWriterConfig.setSimilarity(similarity);
        Directory directory;
        IndexWriter indexWriter;
        try {
            System.out.println("Creating index for: " + path);
            directory = FSDirectory.open(Paths.get("index"));
            indexWriter = new IndexWriter(directory, indexWriterConfig);
            FRDoc =IRUtils.loadFedRegisterDocs(path,id,title,text);
            indexWriter.addDocuments(FRDoc);
            indexWriter.commit();

            indexWriter.close();
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}