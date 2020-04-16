package com.test.lucene;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.test.lucene.Analyzer.AnalAndSim;
import com.test.lucene.Analyzer.symanalyzer;
import com.test.lucene.Utility.IRUtils;
import com.test.lucene.qexpansion.ExpandQuery;
import com.test.lucene.qexpansion.ScorePair;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 搜索
 */
public class IndexSearch {
    public static String[] QUERY_FIELDS = { "title", "text" };
    static IRUtils irutils = new IRUtils();
    static DirectoryReader directoryReader;
    private static String QueryMethod = "default";
    // create IndexSearcher
    static IndexSearcher searcher;
    public static void main(final String[] args) {

        if(args.length==0||args[0].equals("-ExpQuery"))
            ;
        else if(args[0].equals("-NormalQuery"))
            QueryMethod = "Normal";
        else
            throw new IllegalArgumentException("please type in a valid argument");

        Directory directory;
        AnalAndSim anals = new AnalAndSim();
        long startTime = System.currentTimeMillis();
        System.out.println("Querying the topics please wait...");
        try {
            // retrieve the path of index
            directory = FSDirectory.open(Paths.get("index"));
            // open the index dir
            directoryReader = DirectoryReader.open(directory);
            // create IndexSearcher
            searcher = new IndexSearcher(directoryReader);
            // tokenization strategy
            searcher.setSimilarity(anals.getSimilarity());
            Analyzer analyzer = anals.getAnalyzer();
            ArrayList<QueryResult> allResults = querySearch(searcher, analyzer, directoryReader);
            IRUtils.WriteResults(allResults, "res");
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("%d seconds took for querying.", (int)((endTime - startTime) / 1000));
    }

    public static ArrayList<QueryResult> querySearch(IndexSearcher searcher, Analyzer analyzer,
            DirectoryReader directoryReader) {
        Map<String, Float> bstParameter = new HashMap<String, Float>();
        bstParameter.put("title", 1f);
        bstParameter.put("text", 10f);
        QueryParser parser = new MultiFieldQueryParser(QUERY_FIELDS, analyzer, bstParameter);
        int counter = 1;
        final ArrayList<QueryResult> allResults = new ArrayList<QueryResult>();
        ArrayList<ArrayList<String>> qryArr;

        QueryParser symParser = new MultiFieldQueryParser(QUERY_FIELDS, new symanalyzer(), bstParameter);
        try {
            qryArr = irutils.getArrQueries();
            for (int j = 0; j < qryArr.size(); j++) {
                System.out.println("Currently querying for Topic: " + (401 + j));
                BooleanQuery.Builder query = null;
                if(QueryMethod.equals("default"))
                    query = getQuery(parser, qryArr, j, symParser);
//                    query = getQuery1(parser, qryArr, j);
                else if(QueryMethod.equals("Normal"))
                    query = getQuery1(parser, qryArr, j);

                TopDocs results = searcher.search(query.build(), 1000);
                ScoreDoc[] hits = results.scoreDocs;
                final int num = (int) Math.min(results.totalHits, 1000);
                // Set idSet=new HashSet();
                for (int i = 0; i < num; i++) {
                    final int indexDocNo = hits[i].doc;
                    final Document value = directoryReader.document(indexDocNo);
                    // final String content = value.get(QUERY_FIELDS[0]);
                    final String content = value.get("docno");
                    // idSet.add(content);
                    final QueryResult qrs = new QueryResult(counter, qryArr.get(j).get(5), content, i + 1,
                            hits[i].score);
                    allResults.add(qrs);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return allResults;
    }

    private static BooleanQuery.Builder getQuery(QueryParserBase parser, ArrayList<ArrayList<String>> qryarr, int indexOfQuery,
            QueryParser symParser) throws ParseException {
        Query titleQuery = parser.parse(QueryParser.escape(qryarr.get(indexOfQuery).get(0)));
        Query descQuery = parser.parse(QueryParser.escape(qryarr.get(indexOfQuery).get(1)));
        Query relQuery = null;
        if (qryarr.get(indexOfQuery).get(2) != null && qryarr.get(indexOfQuery).get(2).length() != 0) {
            relQuery = parser.parse(QueryParser.escape(qryarr.get(indexOfQuery).get(2)));
        }
        Query mustQuery = null;
        if (qryarr.get(indexOfQuery).get(4) != null && qryarr.get(indexOfQuery).get(4).length() != 0) {
            mustQuery = parser.parse(QueryParser.escape(qryarr.get(indexOfQuery).get(4)));
        }
        // Query countryQuery = null;
        Query countrySymQuery= null;
        if (qryarr.get(indexOfQuery).get(6) != null && qryarr.get(indexOfQuery).get(6).length() != 0) {
            countrySymQuery = symParser.parse(QueryParser.escape(qryarr.get(indexOfQuery).get(6)));
            // countryQuery = parser.parse(QueryParser.escape(qryarr.get(indexOfQuery).get(6)));
        }
        Query simTitleQuery = symParser.parse(QueryParser.escape(qryarr.get(indexOfQuery).get(0)));
        
        
        BooleanQuery.Builder query = new BooleanQuery.Builder();
        query.add(new BoostQuery(titleQuery, 4.1f), BooleanClause.Occur.SHOULD);
        query.add(new BoostQuery(descQuery, 5f), BooleanClause.Occur.SHOULD);
        query.add(new BoostQuery(simTitleQuery, 8f), BooleanClause.Occur.SHOULD);
        if (relQuery != null) {
            query.add(new BoostQuery(relQuery, 2.5f), BooleanClause.Occur.SHOULD);
        }
        // if (negquery!=null)
        // {
        // query.add(new BoostQuery(negquery, 1f), BooleanClause.Occur.MUST_NOT);
        // }
        if (mustQuery != null) {
            query.add(new BoostQuery(mustQuery, 10.5f), BooleanClause.Occur.SHOULD);
            // Query simmustquery = symparser.parse(QueryParser.escape(qryarr.get(indexOfQuery).get(4)));
            // query.add(new BoostQuery(simmustquery, 2f), BooleanClause.Occur.SHOULD);
        }
        if (countrySymQuery != null) {
            // query.add(new BoostQuery(countryquery, 4f), BooleanClause.Occur.SHOULD);
            query.add(new BoostQuery(countrySymQuery, 7.5f), BooleanClause.Occur.SHOULD);
        }  
        ExpandQuery eq=new ExpandQuery();
		try {
            BooleanQuery.Builder tmpq = query;
            // BooleanQuery.Builder tmpq = getquery1(parser, qryarr, indexOfQuery);
            List<ScorePair> eqarr = eq.expandQtfidf(searcher, directoryReader, tmpq);
            for (int i = 0; i < eqarr.size(); i++) {
                ScorePair pair = eqarr.get(i);
                if (pair.docString.length()<3){
                    continue;
                }

                Query eQuery = parser.parse(QueryParser.escape(pair.docString));
                query.add(new BoostQuery(eQuery, (float) pair.score), BooleanClause.Occur.SHOULD);


                eqarr = eq.expandQ(searcher, directoryReader, tmpq);
                for ( i = 0; i < eqarr.size(); i++) {
                     pair = eqarr.get(i);
                    if (pair.docString.length()<3){
                        continue;
                    }
                    eQuery = parser.parse(QueryParser.escape(pair.docString));
                    query.add(new BoostQuery(eQuery, (float) pair.getBoost()), BooleanClause.Occur.SHOULD);
                    }
                
		}
    } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
        return query;
    }


    private static BooleanQuery.Builder getQuery1(QueryParserBase parser, ArrayList<ArrayList<String>> qryArr,
                                                  int indexOfQuery) throws ParseException {
        Query titlequery = parser.parse(QueryParser.escape(qryArr.get(indexOfQuery).get(0)));
        Query descQuery = parser.parse(QueryParser.escape(qryArr.get(indexOfQuery).get(1)));
        Query relQuery=null;
        if (qryArr.get(indexOfQuery).get(2)!=null && qryArr.get(indexOfQuery).get(2).length()!=0)
        {
            relQuery = parser.parse(QueryParser.escape(qryArr.get(indexOfQuery).get(2)));
        }
        Query mustquery=null;
        if (qryArr.get(indexOfQuery).get(4)!=null && qryArr.get(indexOfQuery).get(4).length()!=0)
        {
            mustquery = parser.parse(QueryParser.escape(qryArr.get(indexOfQuery).get(4)));
        }
        BooleanQuery.Builder query = new BooleanQuery.Builder();
        query.add(new BoostQuery(titlequery, 1.5f), BooleanClause.Occur.SHOULD);
        query.add(new BoostQuery(descQuery, 2.1f), BooleanClause.Occur.SHOULD);
        if (relQuery!=null)
        {
            query.add(new BoostQuery(relQuery, 0.5f), BooleanClause.Occur.SHOULD);
        }
        if (mustquery!=null)
        {
            query.add(new BoostQuery(mustquery, 2.5f), BooleanClause.Occur.MUST);
        }
        return query;
    }
}