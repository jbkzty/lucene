package com.jibingkun.lucene;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *     TermRangeQuery是用于支付串范围查询的，字符串比较大小其实是比较的ASC码值，即ASC码范围的查询
 * 因此对于中文应用来说，其商业用途不是很大。一般都是数字范围的查询 ： NumericRangeQuery
 * @author junjin4838
 * @date 2016年8月15日
 * @version 1.0
 */
public class TermRangeQueryTest {
	
	public static void main(String[] args) throws IOException {
		
		String directoryPath = "/Users/junjin4838/git/lucene-put-index";
		
		String fieldName = "contents";
		
		String lowerTermString = "fa";
		
		String upperTermString = "fi";
		
		Query query = new TermRangeQuery(fieldName, new BytesRef(lowerTermString), new BytesRef(upperTermString), false, false);
		
		List<Document> list = query(directoryPath,query);
		
		if(list == null || list.size() == 0 ){
			System.out.println("No results found."); 
			return;
		}
		
		 for(Document doc : list) {  
	         String path = doc.get("path");  
	         String content = doc.get("contents");  
	         System.out.println("path:" + path);  
	         System.out.println("contents:" + content);  
	     }  
		
	}
	
	
	public static List<Document> query(String directoryPath,Query query) throws IOException{
		
		IndexSearcher searcher = createIndexSearcher(directoryPath);
		
		TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
		
		ScoreDoc[] docs = topDocs.scoreDocs;
		
		List<Document> docList = new ArrayList<Document>();  
		
		for(ScoreDoc scoreDoc : docs){
			int docID = scoreDoc.doc;
			Document document = searcher.doc(docID);
			docList.add(document);
		}
		
		searcher.getIndexReader().close();
		
		return docList;
	}
	
	
	/**
	 * 创建索引查询器
	 * @param directoryPath  索引目录
	 * @return
	 * @throws IOException 
	 */
	public static IndexSearcher createIndexSearcher(String directoryPath) throws IOException{
		
		return new IndexSearcher(createIndexReader(directoryPath));
		
	}
	
	/**
	 * 创建索引阅读器
	 * @param directoryPath
	 * @return
	 * @throws IOException 
	 */
	public static IndexReader createIndexReader(String directoryPath) throws IOException{
		
		Directory dir = FSDirectory.open(Paths.get(directoryPath));
		
		return DirectoryReader.open(dir);
		
	}
	
	
	
	
	
	

}
