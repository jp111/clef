
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;


/** Simple command-line based search demo. */
public class SearchFiles {

  private SearchFiles() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    String usage =
    "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }

    String index = "index";
    String field = "contents";
    String queries = null;
    int repeat = 0;
    boolean raw = false;
    String queryString = null;
    int hitsPerPage = 10;
    
    for(int i = 0;i < args.length;i++) {
      if ("-index".equals(args[i])) {
        index = args[i+1];
        i++;
      } else if ("-field".equals(args[i])) {
        field = args[i+1];
        i++;
      } else if ("-queries".equals(args[i])) {
        queries = args[i+1];
        i++;
      } else if ("-query".equals(args[i])) {
        queryString = args[i+1];
        i++;
      } else if ("-repeat".equals(args[i])) {
        repeat = Integer.parseInt(args[i+1]);
        i++;
      } else if ("-raw".equals(args[i])) {
        raw = true;
      } else if ("-paging".equals(args[i])) {
        hitsPerPage = Integer.parseInt(args[i+1]);
        if (hitsPerPage <= 0) {
          System.err.println("There must be at least 1 hit per page.");
          System.exit(1);
        }
        i++;
      }
    }

    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    IndexSearcher searcher = new IndexSearcher(reader);
    Analyzer analyzer = new StandardAnalyzer();


    //System.out.println("Searching for: " + query.toString(field));
    BooleanQuery.Builder bQuery = new BooleanQuery.Builder();
    Query query1 = new TermQuery(new Term(field, "IQCODE"));
    MultiPhraseQuery.Builder q2 = new MultiPhraseQuery.Builder();
    q2.add(new Term(field, "informant questionnaire on cognitive decline in the elderly"));
    MultiPhraseQuery query2= q2.build();
    Query query3 = new TermQuery(new Term(field, "IQ code"));
    //Query query4 = new TermQuery(new Term(field, ""));
    //Query query5 = new TermQuery(new Term(field, ""));
    Query query6 = new WildcardQuery(new Term(field, "informant* questionnair*"));
    Query query7 = new WildcardQuery(new Term(field, "dement*"));
    Query query8 = new WildcardQuery(new Term(field, "screening"));
    
    BooleanQuery.Builder b1 = new BooleanQuery.Builder();
    b1.add(query7, BooleanClause.Occur.SHOULD);
    b1.add(query8, BooleanClause.Occur.SHOULD);
    

    //SpanNearQuery.Builder query9 = new SpanNearQuery.Builder(field, 1);
    //query9.addClause(SpanQuery());

    String[] phraseWords = {"informant* questionnair*","dement*"};
    SpanQuery[] queryParts = new SpanQuery[phraseWords.length];
    for (int i = 0; i < phraseWords.length; i++) {
        WildcardQuery wildQuery = new WildcardQuery(new Term(field, phraseWords[i]));
        queryParts[i] = new SpanMultiTermQueryWrapper<WildcardQuery>(wildQuery);
    }
    SpanNearQuery span =  new SpanNearQuery(queryParts,       //words
                             3,                //max distance
                             true              //exact order
    );

    String[] phraseWords1 = {"informant* questionnair*","screening"};
    SpanQuery[] queryParts1 = new SpanQuery[phraseWords1.length];
    for (int i = 0; i < phraseWords1.length; i++) {
        WildcardQuery wildQuery1 = new WildcardQuery(new Term(field, phraseWords1[i]));
        queryParts1[i] = new SpanMultiTermQueryWrapper<WildcardQuery>(wildQuery1);
    }
    SpanNearQuery span1 =  new SpanNearQuery(queryParts1,       //words
                             3,                //max distance
                             true              //exact order
    );

    String[] phraseWords2 = {"screening test*","dement*"};
    SpanQuery[] queryParts2 = new SpanQuery[phraseWords2.length];
    for (int i = 0; i < phraseWords2.length; i++) {
        WildcardQuery wildQuery2 = new WildcardQuery(new Term(field, phraseWords2[i]));
        queryParts2[i] = new SpanMultiTermQueryWrapper<WildcardQuery>(wildQuery2);
    }
    SpanNearQuery span2 =  new SpanNearQuery(queryParts2,       //words
                             2,                //max distance
                             true              //exact order
    );

    String[] phraseWords3 = {"screening test*","alzheimer*"};
    SpanQuery[] queryParts3 = new SpanQuery[phraseWords3.length];
    for (int i = 0; i < phraseWords3.length; i++) {
        WildcardQuery wildQuery3 = new WildcardQuery(new Term(field, phraseWords3[i]));
        queryParts3[i] = new SpanMultiTermQueryWrapper<WildcardQuery>(wildQuery3);
    }
    SpanNearQuery span3 =  new SpanNearQuery(queryParts3,       //words
                             2,                //max distance
                             true              //exact order
    );
    bQuery.add(query1, BooleanClause.Occur.SHOULD);
    bQuery.add(query2, BooleanClause.Occur.SHOULD);
    bQuery.add(query3, BooleanClause.Occur.SHOULD);
    bQuery.add(span, BooleanClause.Occur.SHOULD);
    bQuery.add(span1, BooleanClause.Occur.SHOULD);
    bQuery.add(span2, BooleanClause.Occur.SHOULD);
    bQuery.add(span3, BooleanClause.Occur.SHOULD);

    //bQuery.add(b1.build(), BooleanClause.Occur.SHOULD);
    //bQuery.add(query6, BooleanClause.Occur.SHOULD);
    //SpanQuery john   = new SpanTermQuery(new Term("content", "john"));
    //SpanQuery kerry  = new SpanTermQuery(new Term("content", "kerry"));

    //SpanQuery johnKerry = new SpanNearQuery(new SpanQuery[] {query7, query8}, 0, true);
    System.out.println(bQuery.build().toString());
    
    TopDocs results = searcher.search(bQuery.build(), 150);
    Date end = new Date();
    ScoreDoc[] hits = results.scoreDocs;
    int numTotalHits = results.totalHits;

    String FILENAME = "/home/devil/research/CLEF/ehealth/task2/dataset/pubmed.res";


    int i = 1;
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) 
    {

      String content = "";
      for (ScoreDoc h : hits) 
      {
        Document doc = searcher.doc(h.doc);
        String path = doc.get("path");
        String[] path_words=path.split("/");
        System.out.println(path_words[path_words.length-1]+" score="+h.score);

        content="CD010771 "+"NF "+path_words[path_words.length-1]+" "+i++ +" "+ h.score+" pubmed\n";

        bw.write(content);
      }

    }
    catch (IOException e) 
    {
        e.printStackTrace();
    }
        //doPagingSearch(in, searcher, bQuery.build(), hitsPerPage, raw, queries == null && queryString == null);
      reader.close();

      
  }

  /**
   * This demonstrates a typical paging search scenario, where the search engine presents 
   * pages of size n to the user. The user can then go to the next page if interested in
   * the next hits.
   * 
   * When the query is executed for the first time, then only enough results are collected
   * to fill 5 result pages. If the user wants to page beyond this limit, then the query
   * is executed another time and all hits are collected.
   * 
   */
  public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, 
   int hitsPerPage, boolean raw, boolean interactive) throws IOException {

    // Collect enough docs to show 5 pages
    TopDocs results = searcher.search(query, 5 * hitsPerPage);
    ScoreDoc[] hits = results.scoreDocs;
    
    int numTotalHits = results.totalHits;
    System.out.println(numTotalHits + " total matching documents");

    int start = 0;
    int end = Math.min(numTotalHits, hitsPerPage);

    while (true) {
      if (end > hits.length) {
        System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
        System.out.println("Collect more (y/n) ?");
        String line = in.readLine();
        if (line.length() == 0 || line.charAt(0) == 'n') {
          break;
        }

        hits = searcher.search(query, numTotalHits).scoreDocs;
      }
      
      end = Math.min(hits.length, start + hitsPerPage);
      
      for (int i = start; i < end; i++) {
        if (raw) {                              // output raw format
          System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
          continue;
        }

        Document doc = searcher.doc(hits[i].doc);
        String path = doc.get("path");
        if (path != null) {
          System.out.println((i+1) + ". " + path);
          String title = doc.get("title");
          if (title != null) {
            System.out.println("   Title: " + doc.get("title"));
          }
        } else {
          System.out.println((i+1) + ". " + "No path for this document");
        }

      }

      if (!interactive || end == 0) {
        break;
      }

      if (numTotalHits >= end) {
        boolean quit = false;
        while (true) {
          System.out.print("Press ");
          if (start - hitsPerPage >= 0) {
            System.out.print("(p)revious page, ");  
          }
          if (start + hitsPerPage < numTotalHits) {
            System.out.print("(n)ext page, ");
          }
          System.out.println("(q)uit or enter number to jump to a page.");
          
          String line = in.readLine();
          if (line.length() == 0 || line.charAt(0)=='q') {
            quit = true;
            break;
          }
          if (line.charAt(0) == 'p') {
            start = Math.max(0, start - hitsPerPage);
            break;
          } else if (line.charAt(0) == 'n') {
            if (start + hitsPerPage < numTotalHits) {
              start+=hitsPerPage;
            }
            break;
          } else {
            int page = Integer.parseInt(line);
            if ((page - 1) * hitsPerPage < numTotalHits) {
              start = (page - 1) * hitsPerPage;
              break;
            } else {
              System.out.println("No such page");
            }
          }
        }
        if (quit) break;
        end = Math.min(numTotalHits, start + hitsPerPage);
      }
    }
  }
}
