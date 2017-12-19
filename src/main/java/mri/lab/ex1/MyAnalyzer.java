/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mri.lab.ex1;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author pierpaolo
 */
public class MyAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldname, Reader reader) {
        WhitespaceTokenizer wtok=new WhitespaceTokenizer(reader);
        LowerCaseFilter lowf=new LowerCaseFilter(wtok);
        PorterStemFilter porter=new PorterStemFilter(lowf);
        StopFilter sf=new StopFilter(porter, CharArraySet.EMPTY_SET);
        return new TokenStreamComponents(wtok, sf);
    }
    
}
