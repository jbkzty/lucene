package com.jiibngkun.lucene.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CountDownLatch;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.jibingkun.lucene.util.LuceneUtils;

/**
 * @author junjin4838
 * @date 2016年8月17日
 * @version 1.0
 */
public class IndexCreator implements Runnable {

	/** 需要读取的文件存放目录 */
	private String docPath;

	/** 索引文件存放目录 */
	private String luceneDir;

	private int threadCount;

	private final CountDownLatch countDownLatch1;

	private final CountDownLatch countDownLatch2;

	public IndexCreator(String docPath, String luceneDir, int threadCount,
			CountDownLatch countDownLatch1, CountDownLatch countDownLatch2) {
		super();
		this.docPath = docPath;
		this.luceneDir = luceneDir;
		this.threadCount = threadCount;
		this.countDownLatch1 = countDownLatch1;
		this.countDownLatch2 = countDownLatch2;
	}

	public void run() {

		IndexWriter writer = null;

		try {
		    countDownLatch1.await();
			Directory dir = FSDirectory.open(Paths.get(luceneDir));
			Analyzer analyzer = LuceneUtils.analyzer;
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			config.setOpenMode(OpenMode.CREATE_OR_APPEND);
			writer = LuceneUtils.getIndexWriter(dir, config);
			indexDocs(writer, Paths.get(docPath));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			LuceneUtils.closeIndexWriter(writer);
			countDownLatch2.countDown();
		}
	}

	/**
	 * 
	 * @param writer
	 *            索引写入器
	 * @param path
	 *            文件路径
	 * @throws IOException
	 */
	public static void indexDocs(final IndexWriter writer, Path path)
			throws IOException {
		// 如果是目录，查找目录下的文件
		if (Files.isDirectory(path, new LinkOption[0])) {
			System.out.println("directory");
			Files.walkFileTree(path, new SimpleFileVisitor<Object>() {
				@Override
				public FileVisitResult visitFile(Object file,
						BasicFileAttributes attrs) throws IOException {
					Path path = (Path) file;
					System.out.println(path.getFileName());
					indexDoc(writer, path, attrs.lastModifiedTime().toMillis());
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer, path,
					Files.getLastModifiedTime(path, new LinkOption[0])
							.toMillis());
		}
	}

	/**
	 * 读取文件，创建索引
	 * 
	 * @param writer
	 * @param path
	 * @param lastModified
	 * @throws IOException
	 */
	public static void indexDoc(IndexWriter writer, Path path, long lastModified)
			throws IOException {

		InputStream inputStream = Files.newInputStream(path, new OpenOption[0]);

		Document doc = new Document();

		Field pathField = new StringField("path", path.toString(),
				Field.Store.YES);

		Field longField = new LongField("modified", lastModified,
				Field.Store.YES);

		Field textField = new TextField("contents",
				intputStream2String(inputStream), Field.Store.YES);

		doc.add(pathField);
		doc.add(longField);
		doc.add(textField);

		if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
			System.out.println("adding " + path);
			writer.addDocument(doc);
		} else {
			System.out.println("updating " + path);
			writer.updateDocument(new Term("path", path.toString()), doc);
		}

		writer.commit();

	}

	/**
	 * InputStream转换成String
	 * 
	 * @param is
	 * @return
	 */
	public static String intputStream2String(InputStream is) {
		BufferedReader bufferReader = null;
		StringBuilder stringBuilder = new StringBuilder();
		String line;

		try {
			bufferReader = new BufferedReader(new InputStreamReader(is,
					StandardCharsets.UTF_8));
			while ((line = bufferReader.readLine()) != null) {
				stringBuilder.append(line + "\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferReader != null) {
				try {
					bufferReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return stringBuilder.toString();
	}

}
