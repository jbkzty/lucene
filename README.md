# lucene






2. Lucene写索引的并发操作
     有个重要的方法：
     final ThreadState perThread = flushControl.obtainAndLock();
    ------------------------------------------------------------
     ThreadState obtainAndLock() {
         final ThreadState perThread = perThreadPool.getAndLock(Thread.currentThread(), documentsWriter);
         boolean success = false;
         try {
            if (perThread.isInitialized() && perThread.dwpt.deleteQueue != documentsWriter.deleteQueue) {
               addFlushableState(perThread);
             }
         success = true;
         return perThread;
    } finally {
        if (!success) { 
         perThreadPool.release(perThread);
        }
      }
     }
    ------------------------------------------------------------
