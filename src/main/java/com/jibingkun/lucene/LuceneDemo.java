package com.jibingkun.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 读取硬盘文件，创建索引
 * 
 * @author junjin4838
 * @date 2016年8月15日
 * @version 1.0
 */
public class LuceneDemo {

	public static void main(String[] args) throws IOException {

		String indexPath = "/Users/junjin4838/git/lucene-put-index";

		String dirPath = "/Users/junjin4838/git/lucene-index";

		createIndex(dirPath, indexPath);
	}

	/**
	 * 创建索引
	 * 
	 * @param dirPath
	 *            文件存放的位置
	 * @param indexPath
	 *            索引存放的位置
	 * @throws IOException
	 */
	private static void createIndex(String dirPath, String indexPath)
			throws IOException {
		createIndex(dirPath, indexPath, false);
	}

	/**
	 * @param dirParh
	 *            文件存放的位置
	 * @param indexPath
	 *            索引存放的位置
	 * @param createOrAppend
	 *            始终创建索引/不存在则追加索引
	 * @throws IOException
	 */
	private static void createIndex(String dirParh, String indexPath,
			Boolean createOrAppend) throws IOException {

		long start = System.currentTimeMillis();

		Directory dir = FSDirectory.open(Paths.get(indexPath));
		
		Path docDirPath = Paths.get(dirParh);

		Analyzer analyzer = new StandardAnalyzer();

		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);

		if (createOrAppend) {
			indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		} else {
			indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		}

		IndexWriter writer = new IndexWriter(dir, indexWriterConfig);

		indexDocs(writer, docDirPath);

		writer.close();

		long end = System.currentTimeMillis();

		System.out.println("Time consumed:" + (end - start) + " ms");

	}

	/**
	 * @param writer
	 *            索引写入器
	 * @param path
	 *            文件路径
	 * @throws IOException
	 */
	private static void indexDocs(final IndexWriter writer, Path path)
			throws IOException {
		boolean isHavaFiles = Files.isDirectory(path, new LinkOption[0]);

		long lastModifiedTime = Files.getLastModifiedTime(path,
				new LinkOption[0]).toMillis();

		// 如果哟目录，则查找目录下面的文件
		if (isHavaFiles) {
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
			indexDoc(writer, path, lastModifiedTime);
		}
	}

	/**
	 * 读取文件创建索引
	 * 
	 * @param writer
	 *            索引写入器
	 * @param file
	 *            文件路径
	 * @param lastModified
	 *            文件最后一次修改时间
	 * @throws IOException
	 */
	private static void indexDoc(IndexWriter writer, Path file,
			long lastModified) throws IOException {

		InputStream stream = Files.newInputStream(file, new LinkOption[0]);

		BufferedReader read = new BufferedReader(new InputStreamReader(stream,
				StandardCharsets.UTF_8));

		Document doc = new Document();

		// 添加Field域
		Field pathField = new StringField("path", file.toString(),
				Field.Store.YES);
		Field longField = new LongField("modified", lastModified,
				Field.Store.NO);
		Field textField = new TextField("contents", read);
		doc.add(pathField);
		doc.add(longField);
		doc.add(textField);

		if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
			System.out.println("adding " + file);
			writer.addDocument(doc);
		} else {
			System.out.println("updating " + file);
			writer.updateDocument(new Term("path", file.toString()), doc);
		}

		writer.commit();

	}

}
