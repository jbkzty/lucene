package com.jibingkun.lucene.util;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * Lucene索引读写器/查询器 单例 获取工具类
 * 
 * @author junjin4838
 * @date 2016年8月17日
 * @version 1.0
 */
public class LuceneManager {
	
	private volatile static LuceneManager singleton;

	private volatile static IndexWriter writer;
	
	private volatile static IndexReader read;
	
	private volatile static IndexSearcher searcher;

	private final Lock writeLock = new ReentrantLock();

	private static ThreadLocal<IndexWriter> writerLocal = new ThreadLocal<IndexWriter>();
	
	private LuceneManager(){
		
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static LuceneManager getLuceneManager(){
		if(singleton == null){
			synchronized(LuceneManager.class){
				if(singleton == null){
					singleton = new LuceneManager();
				}
			}
		}
		return singleton;
	}

	/**
	 * 获取IndexWriter单例对象
	 * @param dir
	 * @param config
	 * @return
	 */
	public IndexWriter getIndexWriter(Directory dir, IndexWriterConfig config) {
		
		if (dir == null) {
			throw new IllegalArgumentException("Directory can not be null.");
		}
		
		if (config == null) {
			throw new IllegalArgumentException("IndexWriterConfig can not be null.");
		}

		try {
			writeLock.lock();
			
			writer = writerLocal.get();

			if (null != writer) {
				return writer;
			}

			if (null == writer) {
              // 如果索引文件被锁，则抛出异常
			  if (IndexWriter.isLocked(dir)) {
				throw new LockObtainFailedException("Directory of index had been locked.");
			  }
			  writer = new IndexWriter(dir, config);
			  writerLocal.set(writer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			writeLock.unlock();
		}
		return writer;
	}
	
	/**
	 * 获取IndexWriter对象[可能为空]
	 * @return
	 */
	public IndexWriter getIndexWriter(){
		return writer;
	}
	
	/**
	 * 获取IndexReader对象
	 * @param dir
	 * @param enableNRTReader 是否开启NRTReader 
	 * @return
	 */
	public IndexReader getIndexReader(Directory dir,boolean enableNRTReader){
		
		if (dir == null) {
			throw new IllegalArgumentException("Directory can not be null.");
		}
		
		try {
			if(null == read) {
				read = DirectoryReader.open(dir);
			}else {
				if(enableNRTReader && read instanceof DirectoryReader) { 
					//开启近实时Reader,能立即看到动态添加/删除的索引变化 
					read = DirectoryReader.openIfChanged((DirectoryReader)read);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return read;
	}
	
	/**
	 * 获取IndexReader对象(默认不开启NRTReader)
	 * @param dir
	 * @return
	 */
	public IndexReader getIndexReader(Directory dir){
		return getIndexReader(dir,false);
	}
	
	/**
	 * 获取IndexSearcher对象
	 * @param reader  IndexReader对象实例 
	 * @param executor  开启多线程查询
	 * @return
	 */
	public IndexSearcher getIndexSearcher(IndexReader reader,ExecutorService executor){
		
		if (reader == null) {
			throw new IllegalArgumentException("Directory can not be null.");
		}
		
		if(searcher == null){
			searcher = new IndexSearcher(reader);
		}
		
		return searcher;
	}
	
	 /** 
     * 获取IndexSearcher对象(不支持多线程查询) 
     * @param reader    IndexReader对象实例 
     * @return 
     */  
    public IndexSearcher getIndexSearcher(IndexReader reader) {  
        return getIndexSearcher(reader, null);  
    }  
    
    /**
     * 关闭IndexWriter
     * @param writer
     */
    public void closeIndexWriter(IndexWriter writer){
    	if(null != writer){
    		try {
				writer.close();
				writer = null;
				writerLocal.remove();
			} catch (IOException e) {
				e.printStackTrace();
			} 
    	}
    }

}
