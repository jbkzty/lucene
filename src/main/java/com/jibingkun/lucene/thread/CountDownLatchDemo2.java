package com.jibingkun.lucene.thread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * <p>
 * Title: CountDownLatchDemo.java
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author junjin4838
 * @date 2016年8月17日
 * @version 1.0
 */
public class CountDownLatchDemo2 {

	final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void main(String[] args) throws InterruptedException {
		// 两个工人的协作
		Worker worker1 = new Worker("zhang san", 5000);
		Worker worker2 = new Worker("li si", 8000);
		worker1.start();
		worker2.start();
		System.out.println("all work done at " + sdf.format(new Date()));
	}

	static class Worker extends Thread {
		String workerName;
		int workTime;

		public Worker(String workerName, int workTime) {
			this.workerName = workerName;
			this.workTime = workTime;
		}

		public void run() {
			System.out.println("Worker " + workerName + " do work begin at "+ sdf.format(new Date()));
			doWork();// 工作了
			System.out.println("Worker " + workerName + " do work complete at "+ sdf.format(new Date()));

		}

		private void doWork() {
			try {
				Thread.sleep(workTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
