package com.xjp.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * This application creates an Apache Lucene Index in a folder and adds files
 * into this index based on the input of the user
 */
public class TextFileSearchDemo {
	private static StandardAnalyzer analyzer = new StandardAnalyzer();
	private IndexWriter writer;
	private ArrayList<File> queue = new ArrayList<File>();
	public static void main(String[] args) throws IOException{
		analyzer.setVersion(Version.LUCENE_6_0_0);
		/*
		 * Enter the directory to create a index there
		 */
		System.out.println("Enter the path where the index will be created: (e.g. /tmp/index or c:\\temp\\index)");
		String indexLocation = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s = br.readLine();
		TextFileSearchDemo indexer = null;
		try{
			indexLocation = s;
			indexer = new TextFileSearchDemo(s);
		}catch(Exception e){
			System.out.println("Cannot cerate index..." + e.getMessage());
			System.exit(-1);
		}
		/*
		 * Add the file to the index
		 * read input from user until he enters q for quit
		 */
		while(!s.equalsIgnoreCase("q")){
			try{
				System.out.println("Enter the full path to add into the index(q=quit)");
				System.out.println("Acceptable file types: .xml, .htm, .html, .txt");
				s = br.readLine();
				if(s.equalsIgnoreCase("q")){
					break;
				}
				//try to add file into the index
				indexer.indexFileOrDirectory(s);
			}catch(Exception e){
				System.out.println("Error indexing " + s + " : " + e.getMessage());
			}
		}
		//After adding, we always have to call the closeIndex, otherwise the index is not created
		indexer.closeIndex();
		/*
		 * query and search the file
		 */
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation).toPath()));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		
		s = "";
		while(!s.equalsIgnoreCase("q")){
			TopScoreDocCollector collector = TopScoreDocCollector.create(5);
			try{
				System.out.println("Enter the search query (q=quit)");
				s = br.readLine();
				if(s.equalsIgnoreCase("q")){
					break;
				}
				Query q = new QueryParser("contents", analyzer).parse(s);
				System.out.println("Now the query is " + q.toString());
				System.out.println("Now the searcher is " + searcher.toString());
				searcher.search(q, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;
				//display the results
				System.out.println("Found " + hits.length + " hits.");
				for(int i = 0; i < hits.length; i++){
					int docId = hits[i].doc;
					Document d = searcher.doc(docId);
					System.out.println((i+1) + ". " + d.get("path") + " score = " + hits[i].score);
				}
			}catch(Exception e){
				System.out.println("Error searching " + s + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	/**
	 * Constructor
	 * @param indexDir the name of the folder in which the index should be created
	 * @throws java.io.IOException when exception creating index
	 */
	TextFileSearchDemo(String indexDir) throws IOException{
		FSDirectory dir = FSDirectory.open(new File(indexDir).toPath());
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		writer = new IndexWriter(dir, config);
	}
	/**
	 * Indexes a file or directory
	 * @param fileName the name of a text file or a folder we wish to add to the index
	 * @throws java.io.IOException when exception
	 */
	public void indexFileOrDirectory(String fileName) throws IOException{
		addFiles(new File(fileName));
		int originalNumDocs = writer.numDocs();
		for(File f : queue){
			FileReader fr = null;
			try{
				Document doc = new Document();
				fr = new FileReader(f);
				doc.add(new TextField("contents", fr));
				doc.add(new StringField("path", f.getPath(), Field.Store.YES));
				doc.add(new StringField("filename", f.getName(), Field.Store.YES));
				writer.addDocument(doc);
				System.out.println("Added: " + f);
			}catch(Exception e){
				System.out.println("Could not add: " + f);
			}finally{
				fr.close();
			}
		}
		int newNumDocs = writer.numDocs();
		System.out.println("");
		System.out.println("**************");
		System.out.println((newNumDocs - originalNumDocs) + " documents added. ");
		System.out.println("**************");
		queue.clear();
	}
	private void addFiles(File file) {
		// TODO Auto-generated method stub
		if(!file.exists()){
			System.out.println(file + " does not exist.");
		}
		if(file.isDirectory()){
			for(File f : file.listFiles()){
				addFiles(f);
			}
		}else{
			String filenames = file.getName().toLowerCase();
			if(filenames.endsWith(".htm") || filenames.endsWith(".html") || filenames.endsWith(".txt") || filenames.endsWith(".xml")){
				queue.add(file);
			}else{
				System.out.println("Skipped " + filenames);
			}
		}
	}
	/**
	 * Close the index
	 * @throws java.io.IOException when exception closing
	 */
	public void closeIndex() throws IOException{
		writer.close();
	}
}
