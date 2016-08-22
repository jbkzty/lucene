package com.jibingkun.lucene.score;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

/**
 * 评分机制是Lucene的核心部分之一，Lucene是按照默认的打分机制对每个Document进行打分，然后按照分数进行降序排序
 * 
 * -- BooleanQuery即所有的子语句按照布尔关系合并
 * -- 树的叶子节点中(最基本的是TermQuery,也即表示一个词)
 * 
 * 域权重对评分的影响
 * @author junjin4838
 *
 */
public class FieldBootTest {
	
	public static void main(String[] args) throws IOException, ParseException {
		
		RAMDirectory directory = new RAMDirectory();
		
		Analyzer analyzer = new StandardAnalyzer();
		
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);
		
		IndexWriter writer = new IndexWriter(directory,config);
		
		Document doc1 = new Document();
		Field f1 = new TextField("title", "Java, hello world!",Store.YES);  
		doc1.add(f1);
		writer.addDocument(doc1);
		
		Document doc2 = new Document();
		Field f2 = new TextField("title", "Java, I like it!",Store.YES);  
		f2.setBoost(100);
		doc2.add(f2);
		writer.addDocument(doc2);
		
		Document doc3 = new Document();
		Field f3 = new TextField("title", "Java, cat like it!",Store.YES);  
		doc3.add(f3);
		writer.addDocument(doc3);
		
		writer.close();
		
		IndexReader reader = DirectoryReader.open(directory);
		
		IndexSearcher search = new IndexSearcher(reader);
		
		//QueryParser 的使用
		String queryString = "+(+java* -hello)(cat* dog)";
		QueryParser parser = new QueryParser("title",analyzer);  
		Query query = parser.parse(queryString);
		
		/**
		 * 查询 --search(createNormalizedWeight(wrapFilter(query, filter)), null, n)
		 * step1 : 创建weight树，计算term weight
		 * step2 : 创建score以及SumScore树，为合并倒排表做准备
		 * step3 : 用SumScore进行倒排表合并
		 * step4 : 收集文档结果集合以及计算打分
		 * 
		 *   public Weight createNormalizedWeight(Query query) throws IOException {
		 *   
		 *      //重写Query对象树
         *      query = rewrite(query);
         *      
         *      //创建Weight对象树
         *      Weight weight = query.createWeight(this);
         *      
         *      //计算 Term Weight 分数
         *      float v = weight.getValueForNormalization();
         *      float norm = getSimilarity().queryNorm(v);
         *      if (Float.isInfinite(norm) || Float.isNaN(norm)) {
         *        norm = 1.0f;
         *      }
         *      weight.normalize(norm, 1.0f);
         *      
         *      return weight;
         *   }
		 */
		TopDocs topDocs = search.search(query, Integer.MAX_VALUE);
		
		ScoreDoc[] docs = topDocs.scoreDocs;
		
		if(docs == null || docs.length == 0){
			System.out.println("No results for this query.");  
			return;
		}
		
		for(ScoreDoc scoreDoc : docs){
			int docId = scoreDoc.doc;
			float score = scoreDoc.score;
			Document document = search.doc(docId);
			String title = document.get("title");
			
			System.out.println("docId:" + docId);  
            System.out.println("title:" + title);  
            System.out.println("score:" + score); 
            
            System.out.println("\n");  
			
		}
		
		reader.close();
		directory.close();
			
	}

}
