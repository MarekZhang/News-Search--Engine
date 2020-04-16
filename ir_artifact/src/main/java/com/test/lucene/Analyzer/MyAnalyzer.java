package com.test.lucene.Analyzer;

import com.test.lucene.Utility.IRUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class MyAnalyzer extends Analyzer {
	private List<String> stopWords;
	public MyAnalyzer() {
		// super(stopWords);
	}

	@Override
	protected TokenStreamComponents createComponents(String s) {
		StandardTokenizer tokenizer = new StandardTokenizer();
		TokenStream filter = new LowerCaseFilter(tokenizer);
		filter = new FlattenGraphFilter(new WordDelimiterGraphFilter(filter,
				WordDelimiterGraphFilter.SPLIT_ON_NUMERICS | WordDelimiterGraphFilter.GENERATE_WORD_PARTS
						| WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS | WordDelimiterGraphFilter.PRESERVE_ORIGINAL,
				null));
		// try {
		// 	WordnetSynonymParser parser = new WordnetSynonymParser(true, false, new StandardAnalyzer(CharArraySet.EMPTY_SET));
		// 	FileReader wordnetReader = new FileReader(IRUtils.absPathpro);
		// 	parser.parse(wordnetReader);
		// 	SynonymMap synonymMap = parser.build();
		// 	// filter= new SynonymFilter(filter, synonymMap, false);        
		// 	filter = synonymMap.fst == null ? filter : new FlattenGraphFilter(new SynonymGraphFilter(filter, synonymMap, true));
		// 	} catch (IOException | ParseException e) {
		// 		// TODO Auto-generated catch block
		// 		e.printStackTrace();
		// }
		filter = new StandardFilter(filter);
		filter = new TrimFilter(filter);
		filter = new PorterStemFilter(filter);
        filter = new EnglishPossessiveFilter(filter);
        //filter = new StopFilter(filter, stopwords);
        filter = new KStemFilter(filter);
		//return new TokenStreamComponents(tokenizer, filter);
        filter = new StopFilter(filter, getListOfStopWords());
		filter = new StopFilter(filter, StandardAnalyzer.ENGLISH_STOP_WORDS_SET);
		filter = new SnowballFilter(filter, "English");
		return new TokenStreamComponents(tokenizer, filter);
	}

	private CharArraySet getListOfStopWords() {
		if (stopWords == null) {
			stopWords = new ArrayList<>();
			try {
				File file = new File(IRUtils.absPathstop);
				try (BufferedReader br = new BufferedReader(new FileReader(file))) {
					String line;
					while ((line = br.readLine()) != null) {
						stopWords.add(line.trim());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return new CharArraySet(stopWords, true);
	}
}