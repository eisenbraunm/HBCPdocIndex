package com.tandf.hbcpdocindex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.TextField;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildRowsIndex {
    private String userName;
    private String password;
    private String url;

    private IndexWriter writer;
    private String indexLocation;

    public static final String[] ENGLISH_STOP_WORDS = {
            "about", "above", "adds", "after", "all", "allows", "almost",
            "alone", "along", "also", "alter", "although", "an", "and", "any", "are", "because", "become", "been", "before",
            "began", "begun", "begin", "best", "between", "both", "but", "by", "can", "change", "changed", "changes", "cm",
            "decided", "descr", "did", "does", "either", "end", "every", "except", "for", "form", "full", "further",
            "give", "going", "has", "have", "having", "ie",  "if", "ii", "iii", "into", "is", "it", "iv", "kg", "kj",
            "known", "like", "made", "many", "mg", "more", "most", "need", "now", "news", "nm", "no", "not", "of",
            "off", "on", "once", "one", "opt", "or", "other", "over", "own", "probable", "probably", "props", "reach", "receive",
            "received", "related", "remain", "remained", "remove", "reported", "seek", "seeking", "seemed", "seen",
            "several", "showed", "shown", "taken", "than", "that", "the", "their", "them", "then", "there", "they", "this", "those", "three",
            "two", "under", "up", "very", "was", "were", "when", "whereas", "where", "which", "while", "whilst", "who",
            "whom", "whose", "will", "with", "within", "without", "would", "(1)", "(2)", "(3)", "(4)", "(5)", "(6)", "(7)", "?", "??"
    };


    public BuildRowsIndex(String _indexLocation, String _url, String _userName, String _password) throws IOException {
        this.indexLocation = _indexLocation;
        userName = _userName;
        password = _password;
        url = _url;

        // clear out old directory
        File index = new File(indexLocation);
        if (index.exists()) {

            FileUtils.deleteDirectory(index);
        }


        CharArraySet stopSet = StopFilter.makeStopSet(ENGLISH_STOP_WORDS);
        HBCPdocAnalyzer analyzer = new HBCPdocAnalyzer(stopSet);

        Directory dir = FSDirectory.open( new File( indexLocation).toPath() );
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setRAMBufferSizeMB(48);
        writer = new IndexWriter(dir, config);

    }

    public void startBuild() throws IOException {

        Connection connection = null;
        try {
            connection = getConnection();
            getTablesStart(connection);

        } catch (Exception e) {
            System.out.println("error" + e.getMessage());

        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                //    log.error("Error in closing connection", e);
                System.out.println("error" + e.getMessage());

            }

        }
        writer.commit();
        writer.forceMerge(1);

        closeIndex();
    }

    public Connection getConnection() throws Exception {

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        Connection conn = DriverManager.getConnection(url, userName, password);
        return conn;
    }

    public void getTablesStart(Connection connection) throws SQLException, IOException {

        Long tableId = null;
        String tableRef = null;
        String tableTitle = null;
        String sqlStatement = "SELECT DISTINCT  TBL_INTERACT_FIELDS.DOC_TABLE_ID, TBL_INTERACT_FIELDS.IA_DOC_TABLE_REF, TBL_DOC_TABLE.ID, TBL_DOC_TABLE.DOCUMENT_TABLE_NAME "
                + " FROM            TBL_INTERACT_FIELDS INNER JOIN "
                + "  TBL_DOC_TABLE ON TBL_INTERACT_FIELDS.DOC_TABLE_ID = TBL_DOC_TABLE.ID INNER JOIN "
                + "  TBL_DOC_CONTENTS ON TBL_DOC_TABLE.ID = TBL_DOC_CONTENTS.DOC_TABLE_ID INNER JOIN "
                + "   TBL_DOC_CONTENT ON TBL_DOC_CONTENTS.DOC_CONTENT_ID = TBL_DOC_CONTENT.ID "
                + " WHERE        (TBL_DOC_CONTENT.FOR_WEB = 1) AND (TBL_INTERACT_FIELDS.IA_DOC_TABLE_REF NOT LIKE 'INT_TBL_19%') AND (TBL_INTERACT_FIELDS.IA_DOC_TABLE_REF NOT LIKE 'INT_TBL_20%') AND  "
                + "  (TBL_INTERACT_FIELDS.IA_DOC_TABLE_REF NOT LIKE 'INT_TBL_21%') "
                + " ORDER BY TBL_INTERACT_FIELDS.IA_DOC_TABLE_REF";
        PreparedStatement tStatement = null;
        ResultSet tResultSet = null;

        try {

            tStatement = connection.prepareStatement(sqlStatement, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            tResultSet = tStatement.executeQuery();

            //return all records
            while (tResultSet.next()) {

                tableId = tResultSet.getLong("DOC_TABLE_ID");
                tableRef = tResultSet.getString("IA_DOC_TABLE_REF");
                tableTitle = tResultSet.getString("DOCUMENT_TABLE_NAME");
                System.out.println("about write rows for  " + tableRef);
                compileSQLstringThenWrite(tableId, tableRef, tableTitle, connection);

            }

        } catch (SQLException e) {
            // String err=e.getMessage();
            System.out.println("error" + e.getMessage());
            //  throw new Exception("checkTables error", e);
        } finally {
            try {
                tResultSet.close();
            } catch (Exception e) {
                // log.error("Error closing a ResultSet for " + this.getClass().getName());
            }

            try {
                tStatement.close();
            } catch (Exception e) {
                //  log.error("Error closing a PreparedStatement for " + this.getClass().getName());
            }

        }

    }

    public void compileSQLstringThenWrite(Long tableId, String tableRef, String tableTitle, Connection connection) throws SQLException, IOException {

        String sqlStatement = "SELECT  VIEW_FIELD_NAME,VIEW_DATA_TYPE FROM   TBL_INTERACT_FIELDS WHERE  (DOC_TABLE_ID = " + tableId + ") AND (IS_VISIBLE = 1) ORDER BY ORDERING";
        PreparedStatement tStatement = null;
        String buildString = "";
        ResultSet tResultSet = null;
        int count = 0;
        String dataType = "";

        try {

            tStatement = connection.prepareStatement(sqlStatement, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            tResultSet = tStatement.executeQuery();

            //return all records
            while (tResultSet.next()) {
                if (count > 0) {
                    buildString = buildString + ", ";
                }

                count = count + 1;
                dataType = tResultSet.getString("VIEW_DATA_TYPE");
                if (dataType.equalsIgnoreCase("nvarchar")) {
                    buildString += "dbo.tagstrip(" + tResultSet.getString("VIEW_FIELD_NAME") + ")";
                } else {
                    buildString += tResultSet.getString("VIEW_FIELD_NAME");
                }

            }

        } catch (SQLException e) {
            // String err=e.getMessage();
            System.out.println("error" + e.getMessage());
            //  throw new Exception("checkTables error", e);
        } finally {
            try {
                tResultSet.close();
            } catch (Exception e) {
                // log.error("Error closing a ResultSet for " + this.getClass().getName());
            }

            try {
                tStatement.close();
            } catch (Exception e) {
                //  log.error("Error closing a PreparedStatement for " + this.getClass().getName());
            }

        }

        buildString = "SELECT " + buildString + " FROM " + tableRef + " ORDER BY ORDERING";
        String metadata = buildTableMetadata(connection, tableId);

        writeRowsToIndex(buildString, count, tableId, tableRef, tableTitle, metadata, connection);

    }

    public void writeRowsToIndex(String sqlStatement, int numFields, Long tableId, String tableRef, String tableTitle, String metadata, Connection connection) throws SQLException, IOException {

        String docRef = tableRef.substring(8, 13);
        Integer ordering = null;

        String contents = "";

        String field = "";
        PreparedStatement tStatement = null;

        ResultSet tResultSet = null;

        try {

            tStatement = connection.prepareStatement(sqlStatement, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            tResultSet = tStatement.executeQuery();

            //now index the rows one at a time
            while (tResultSet.next()) {
                Document doc = new Document();
                int count = 2; //don't index the row number
                while (count <= numFields) {
                    field = tResultSet.getString(count);

                    if (field != null && !field.equalsIgnoreCase("null")) {

                        field = tResultSet.getString(count);

                        field = StringEscapeUtils.unescapeHtml4(field);
                        if (field != null) {
                            contents += field.trim() + " ";
                        }
                    }
                    count++;
                }
                if (tableTitle == null) {

                    tableTitle = "Table";
                }

                ordering = tResultSet.getInt("ORDERING");
                doc.add(new StringField("tableRef", tableRef.toLowerCase(), Field.Store.YES));
                doc.add(new TextField("contents", contents.toLowerCase(), Field.Store.NO));

                CNBFieldTypes.addNumericFieldSort0nly(doc, "ordering",ordering);
                writer.addDocument(doc);
                contents = "";

            }

        } catch (SQLException e) {
            // String err=e.getMessage();
            System.out.println("error" + e.getMessage());
            //  throw new Exception("checkTables error", e);
        } finally {
            try {
                tResultSet.close();
            } catch (Exception e) {
                // log.error("Error closing a ResultSet for " + this.getClass().getName());
            }

            try {
                tStatement.close();
            } catch (Exception e) {
                //  log.error("Error closing a PreparedStatement for " + this.getClass().getName());
            }

        }
    }

    public void closeIndex() throws IOException {
        writer.close();
    }

    public static String removeTags(String string) {
        Pattern REMOVE_TAGS = Pattern.compile("<.+?>");
        if (string == null || string.length() == 0) {
            return string;
        }
        Matcher m = REMOVE_TAGS.matcher(string);
        return m.replaceAll("");
    }

    //this adds 0 pad to the front of a number and returns a string
    public static String padNumberString(String Number, int reqLength) {

        while (Number.length() < reqLength) {
            Number = "0" + Number;
        }
        return Number;
    }

    public String buildTableMetadata(Connection connection, Long tableId) {
        String metadata = "";

        String sqlStatement = "   SELECT         TBL_INTERACT_FIELDS.COLUMN_NAME, TBL_FIELD.METADATA FROM            TBL_INTERACT_FIELDS LEFT OUTER JOIN ";
        sqlStatement += " TBL_FIELD ON TBL_INTERACT_FIELDS.FIELD_ID = TBL_FIELD.ID WHERE        (TBL_INTERACT_FIELDS.IS_VISIBLE = 1) and (TBL_INTERACT_FIELDS.DOC_TABLE_ID = " + tableId + ")";

        PreparedStatement tStatement = null;

        ResultSet tResultSet = null;
        int count = 0;
        String columnHeader = "";
        String fMetadata = "";
        try {

            tStatement = connection.prepareStatement(sqlStatement, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            tResultSet = tStatement.executeQuery();

            //return all records
            while (tResultSet.next()) {

                columnHeader = tResultSet.getString("COLUMN_NAME");
                fMetadata = tResultSet.getString("METADATA");
                if (columnHeader != null) {
                    metadata += StringEscapeUtils.unescapeHtml4(columnHeader) + " ";
                }
                if (fMetadata != null) {
                    metadata += " " + StringEscapeUtils.unescapeHtml4(fMetadata) + " ";
                }
            }

        } catch (SQLException e) {
            // String err=e.getMessage();
            System.out.println("error metadata table Id= " + tableId + "; " + e.getMessage());

        } finally {
            try {
                tResultSet.close();
            } catch (Exception e) {
                // log.error("Error closing a ResultSet for " + this.getClass().getName());
            }

            try {
                tStatement.close();
            } catch (Exception e) {
                //  log.error("Error closing a PreparedStatement for " + this.getClass().getName());
            }

        }

        return metadata;

    }
}
