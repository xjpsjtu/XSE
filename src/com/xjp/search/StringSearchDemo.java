package com.xjp.search;

import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.io.*;


public class StringSearchDemo {
	public static void main(String[] args) throws IOException, ParseException{
		Analyzer luceneAnalyzer = new StandardAnalyzer();
		Directory index = new RAMDirectory();
		
		IndexWriterConfig config = new IndexWriterConfig(luceneAnalyzer);
		IndexWriter writer = new IndexWriter(index, config);
		addDoc(writer, "Lucene in Action", "193398817");
		addDoc(writer, "Lucene for Dummies", "32080293");
		addDoc(writer, "Managing Gigabytes", "82340092");
		addDoc(writer, "The Art of Computer Science", "92340234");
		writer.close();
		String querystr = args.length > 0 ? args[0] : "lucene";
		Query q = new QueryParser("title", luceneAnalyzer).parse(querystr);
		int hitsPerPage = 10;
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs docs = searcher.search(q, hitsPerPage);
		ScoreDoc[] hits = docs.scoreDocs;
		System.out.println("Found " + hits.length + " hits.");
		for(int i = 0; i < hits.length; i++){
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			System.out.println((i+1) + ". " + d.get("isbn") + "\t" + d.get("title"));
		}
	}
	private static void addDoc(IndexWriter w, String title, String isbn) throws IOException{
		Document doc = new Document();
		doc.add(new TextField("title", title, Field.Store.YES));
		doc.add(new StringField("isbn", isbn, Field.Store.YES));
		w.addDocument(doc);
	}
}
