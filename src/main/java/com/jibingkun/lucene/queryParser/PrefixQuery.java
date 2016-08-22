package com.jibingkun.lucene.queryParser;

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
import org.apache.lucene.queryparser.surround.parser.ParseException;
import org.apache.lucene.queryparser.surround.parser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

public class PrefixQuery {
	
	public static void main(String[] args) throws IOException, ParseException {
		
		RAMDirectory directory = new RAMDirectory();
		
		Analyzer analyzer = new StandardAnalyzer();
		
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);
		
		IndexWriter writer = new IndexWriter(directory,config);
		
		Document doc1 = new Document();
		Field f1 = new TextField("title", "javahello",Store.YES); 
		doc1.add(f1);
		writer.addDocument(doc1);
		
		Document doc2 = new Document();
		Field f2 = new TextField("title", "javaRuby",Store.YES); 
		doc2.add(f2);
		writer.addDocument(doc2);
		
		writer.close();
		
        IndexReader reader = DirectoryReader.open(directory);
		
		IndexSearcher search = new IndexSearcher(reader);
		
		QueryParser query = new QueryParser();
		query.parse("java*");
		
		
       
		
		
		
		
	}

}
