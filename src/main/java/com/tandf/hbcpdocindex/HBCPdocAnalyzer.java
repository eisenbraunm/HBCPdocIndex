package com.tandf.hbcpdocindex;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;



import java.io.IOException;
import java.io.Reader;

//I have based this on the standard analyser in lucene core org.apache.lucene.analysis.standard;

public final class HBCPdocAnalyzer extends StopwordAnalyzerBase {
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
    private int maxTokenLength;

    public HBCPdocAnalyzer(CharArraySet stopWords) {
        super(stopWords);
        this.maxTokenLength = 255;
    }

    public HBCPdocAnalyzer() {
        this(CharArraySet.EMPTY_SET);
    }

    public HBCPdocAnalyzer(Reader stopwords) throws IOException {
        this(loadStopwordSet(stopwords));
    }

    public void setMaxTokenLength(int length) {
        this.maxTokenLength = length;
    }

    public int getMaxTokenLength() {
        return this.maxTokenLength;
    }

    protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
       // StandardTokenizer src = new StandardTokenizer();
        Tokenizer  src = new WhitespaceTokenizer();

     //   src.setMaxTokenLength(this.maxTokenLength);
        TokenStream tok1 = new org.apache.lucene.analysis.LowerCaseFilter(src);// this was strange on the lucene one
        TokenStream tok2 = new StopFilter(tok1, this.stopwords);
        TokenStream tok3 = new HTMLstripFilter(tok2);
        return new Analyzer.TokenStreamComponents((r) -> {
         //   src.setMaxTokenLength(this.maxTokenLength);
            src.setReader(r);
        }, tok3);
    }

    protected TokenStream normalize(String fieldName, TokenStream in) {
        return new LowerCaseFilter(in);
    }
}
