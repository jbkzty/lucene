package com.jibingkun.lucene.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author junjin4838
 * @date 2016年8月18日
 * @version 1.0
 */
public class MultiThreadIndexTest {
	
	public static void main(String[] args) {
		
		int threadCount = 5;
		
		ExecutorService pool = Executors.newFixedThreadPool(threadCount);
		
		CountDownLatch countDownLatch1 = new CountDownLatch(1);
		
		CountDownLatch countDownLatch2 = new CountDownLatch(threadCount);
		
		for (int i = 0; i < threadCount; i++) {
			Runnable runnable = new IndexCreator("/Users/junjin4838/solr_home/doc"+(i+1),"/Users/junjin4838/solr_home/luceneDir"+(i+1),threadCount,countDownLatch1,countDownLatch2);
			pool.execute(runnable);
		}
		
		countDownLatch1.countDown();
		
		System.out.println("开始创建索引"); 
		
		//等待所有线程都完成 
		try {
			countDownLatch2.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}  
		
		System.out.println("所有线程都创建索引完毕"); 
		
		//释放线程池资源  
        pool.shutdown();
        
	}

}
