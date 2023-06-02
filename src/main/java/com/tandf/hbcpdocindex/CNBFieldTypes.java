package com.tandf.hbcpdocindex;

import org.apache.lucene.document.*;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;


public class CNBFieldTypes {
    //Don't be confused between a StringField and TextField. Although both the fields contain textual data, there are major differences between these two fields.
    // A StringField is not tokenized and it's a good tool for exact match and sorting.
    // A TextField is tokenized and it's useful for storing any unstructured text for indexing.
    // When you pass the text into an Analyzer for indexing, a TextField is what's used to store the text content.

    public static FieldType fieldTextViewNoTok() {
        //string field
        // This is the field setting for field (no tokenization searchable and stored).
        FieldType fieldTextViewOnly = new FieldType();
        fieldTextViewOnly.setOmitNorms( true );

        fieldTextViewOnly.setIndexOptions( IndexOptions.DOCS );
        fieldTextViewOnly.setStored( true );
        fieldTextViewOnly.setTokenized( false );
        fieldTextViewOnly.setStoreTermVectors( false );
        fieldTextViewOnly.setStoreTermVectorPositions( false );
        fieldTextViewOnly.freeze();
        return fieldTextViewOnly;
    }



    public static FieldType fieldTextTokenizeSearch() {
        // This is the field setting for field (tokenization, searchable, and stored).
        FieldType fieldTextTokenizeSearch= new FieldType();
        fieldTextTokenizeSearch.setOmitNorms( true );
        fieldTextTokenizeSearch.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        fieldTextTokenizeSearch.setStored( true );
        fieldTextTokenizeSearch.setTokenized( true);
        fieldTextTokenizeSearch.setDocValuesType(DocValuesType.NONE);
        fieldTextTokenizeSearch.setStoreTermVectors( false );
        fieldTextTokenizeSearch.setStoreTermVectorPositions( false );
        fieldTextTokenizeSearch.freeze();
        return fieldTextTokenizeSearch;
    }

    public static FieldType fieldTextSearchNotStored() {
        //this is like a textfield without storing in the index to save space
        // This is the field setting for field (tokenization, searchable, and not stored).
        FieldType fieldTextSearchNotStored= new FieldType();
        fieldTextSearchNotStored.setOmitNorms( true );
        fieldTextSearchNotStored.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        fieldTextSearchNotStored.setStored( false );
        fieldTextSearchNotStored.setTokenized( true);
        fieldTextSearchNotStored.setStoreTermVectors( false );
        fieldTextSearchNotStored.setStoreTermVectorPositions( false );
        fieldTextSearchNotStored.setDocValuesType(DocValuesType.NONE);
        fieldTextSearchNotStored.freeze();
        return fieldTextSearchNotStored;
    }



    /**
     * Gets each field per driver key and adds to the Document
     *
     * Depending on what you need you have to add multiple fields for multiple purposes. If you have a numeric value as long and you like to do range queries and sort you would do something like this:
     *
     * // for range queries
     * new LongPoint(field, value);
     * // for storing the value
     * new StoredField(field, value);
     * // for sorting / scoring
     * new NumericDocValuesField(field, value);
     *
     * https://stackoverflow.com/questions/62589849/lucene-longpoint-range-search-doesnt-work
     */

    public static boolean addNumericFieldSearch(Document doc, String field, Object fieldValue) {
        Class<?> clazz = fieldValue.getClass();
        if (clazz == Long.class) {
            doc.add(new LongPoint(field, (Long) fieldValue));
            doc.add(new StoredField(field, (Long) fieldValue));
            doc.add(new NumericDocValuesField(field, (Long) fieldValue));
        } else if (clazz == Integer.class) {
            doc.add(new IntPoint(field, (Integer) fieldValue));
            doc.add(new StoredField(field, (Integer) fieldValue));
            doc.add(new NumericDocValuesField(field, (Integer) fieldValue));
        } else if (clazz == Float.class) {
            doc.add(new FloatPoint(field, (Float) fieldValue));
            doc.add(new StoredField(field, (Float) fieldValue));
            doc.add(new FloatDocValuesField(field, (Float) fieldValue));
        } else if (clazz == Double.class) {
            doc.add(new DoublePoint(field, (Double) fieldValue));
            doc.add(new StoredField(field, (Double) fieldValue));
            doc.add(new DoubleDocValuesField(field, (Double) fieldValue));
        } else {
            return false;
        }
        return true;
    }



    public static boolean addNumericFieldSort0nly(Document doc, String field, Object fieldValue) {
        Class<?> clazz = fieldValue.getClass();
        if (clazz == Long.class) {

            doc.add(new StoredField(field, (Long) fieldValue));
            doc.add(new NumericDocValuesField(field, (Long) fieldValue));
        } else if (clazz == Integer.class) {

            doc.add(new StoredField(field, (Integer) fieldValue));
            doc.add(new NumericDocValuesField(field, (Integer) fieldValue));
        } else if (clazz == Float.class) {

            doc.add(new StoredField(field, (Float) fieldValue));
            doc.add(new FloatDocValuesField(field, (Float) fieldValue));
        } else if (clazz == Double.class) {

            doc.add(new StoredField(field, (Double) fieldValue));
            doc.add(new DoubleDocValuesField(field, (Double) fieldValue));
        } else {
            return false;
        }
        return true;
    }


    public void setUp() throws Exception {


        Document document = new Document();
        document.add(new NumericDocValuesField("int", 5));
        document.add(new NumericDocValuesField("long", 22L));
        document.add(new FloatDocValuesField("float", (float) 2.30));
        document.add(new DoubleDocValuesField("double",2.2));
    }


}
