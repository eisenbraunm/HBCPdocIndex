package com.tandf.hbcpdocindex;
import java.io.File;
import java.io.FileNotFoundException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildDocIndex {

    private IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<File>();
    private String indexLocation;
    private String folderToIndex;
    Logger log = LogManager.getLogger(BuildDocIndex.class);
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


    public BuildDocIndex(String indexLocation, String folderToIndex)throws IOException {
        this.indexLocation = indexLocation;
        this.folderToIndex = folderToIndex;
        //clear out the old index
        File index = new File(indexLocation);
        if (index.exists()){

            FileUtils.deleteDirectory(index);
        }

        CharArraySet stopSet = StopFilter.makeStopSet(ENGLISH_STOP_WORDS);
 HBCPdocAnalyzer analyzer = new HBCPdocAnalyzer(stopSet);

        Directory dir = FSDirectory.open( new File( indexLocation).toPath() );
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        writer = new IndexWriter(dir,config );
    }

    public void startBuild() throws IOException {




        //try to add file into the index
        indexFileOrDirectory(folderToIndex);

        //===================================================
        //after adding, we always have to call the
        //closeIndex, otherwise the index is not created
        //===================================================
        writer.commit();
        writer.forceMerge(1);
        closeIndex();

    }

    public void indexFileOrDirectory(String folderName) throws IOException {
        //===================================================
        //gets the list of files in a folder (if user has submitted
        //the name of a folder) or gets a single file name (is user
        //has submitted only the file name)
        //===================================================
        addFiles(new File(folderName));

        int originalNumDocs = writer.getDocStats().numDocs;
        for (File f : queue) {
            // FileReader fr = null;
            try {
                Document doc = new Document();
                String wholePath=f.getAbsolutePath();
                Integer len=wholePath.length()-16;
                String fileName= wholePath.substring(len);
//String subfolder= wholePath.substring(0,len-1);
                String subfolder= wholePath.substring(0,len);

                //===================================================
                // add contents of file
                //===================================================
                //     fr = new FileReader(f);
                Path filepath = Paths.get(subfolder, fileName);
                String fileContents=     new String(Files.readAllBytes( filepath));
                fileContents=removeTagsPunct(fileContents);

         


                //this is a simple text fields without vectors freuqnecies and offsets,

                //  doc.add(new TextField("contents", fr));
                doc.add(new TextField("contents",fileContents, Field.Store.NO));

                //  Field contentField = new Field("contents", getAllText(f), textFieldType);
                //  doc.add(contentField);
                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(), Field.Store.YES));
                doc.add(new SortedDocValuesField("filename", new BytesRef(f.getName())));
                writer.addDocument(doc);


                System.out.println("Added: " + f.getName());
            } catch (Exception e) {
                log.error("Could not add: " + f+ " "+e.getLocalizedMessage());
            } finally {
                //   fr.close();
            }
        }

        int newNumDocs = writer.getDocStats().numDocs;
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        queue.clear();
    }

    private void addFiles(File file) {

        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String filename = file.getName().toLowerCase();
            //===================================================
            // Only index text files
            //===================================================
            if (filename.endsWith(".htm") || filename.endsWith(".html")
                    || filename.endsWith(".xml") || filename.endsWith(".txt")) {
                queue.add(file);
            } else {
                System.out.println("Skipped " + filename);
            }
        }
    }

    /**
     * Close the index.
     *
     * @throws java.io.IOException when exception closing
     */
    public void closeIndex() throws IOException {
        writer.close();

    }


    public String getAllText(File f) throws FileNotFoundException, IOException {
        String textFileContent = "";
        Charset charset = Charset.forName("UTF-8");
        for (String line : Files.readAllLines(Paths.get(f.getAbsolutePath()),charset)) {
            textFileContent += line;
        }
        return textFileContent;
    }


    public static String removeTagsPunct(String string) {

        string= removeHeadTag(string);
        string= removeScriptTag(string);
        string= removeSCRIPTtag(string);

        Pattern REMOVE_TAGS = Pattern.compile("<.+?>");
        if (string == null || string.length() == 0) {
            return "";
        }
        Matcher m = REMOVE_TAGS.matcher(string);

        String value = m.replaceAll("");

        value = value.replace(",", "");
        value = value.replace(";", "");
        value = value.replace(".", "");
        value = value.replace(":", "");
        value = value.replace("&gt;", ">");
        value = value.replace("&lt;", "<");
        value = value.replace("(", "");
        value = value.replace(")", "");
        value = value.replace("}", "");
        value = value.replace("{", "");
        value = value.replace("[", "");
        value = value.replace("]", "");
        value = value.replace("-", "");
        value=value.replace("”","");
        value=value.replace("“","");
        value=value.replace("?","");
        value=value.replace("*","");

        return value;
    }

    public static String removeHeadTag(String data){
        StringBuilder regex = new StringBuilder("<head[^>]*>(.*?)</head>");
        int flags = Pattern.MULTILINE | Pattern.DOTALL| Pattern.CASE_INSENSITIVE;
        Pattern pattern = Pattern.compile(regex.toString(), flags);
        Matcher matcher = pattern.matcher(data);
        return matcher.replaceAll("");
    }


    public static String removeScriptTag(String data){
        StringBuilder regex = new StringBuilder("<script[^>]*>(.*?)</script>");
        int flags = Pattern.MULTILINE | Pattern.DOTALL| Pattern.CASE_INSENSITIVE;
        Pattern pattern = Pattern.compile(regex.toString(), flags);
        Matcher matcher = pattern.matcher(data);
        return matcher.replaceAll("");
    }

    public static String removeSCRIPTtag(String data){
        StringBuilder regex = new StringBuilder("<SCRIPT[^>]*>(.*?)</SCRIPT>");
        int flags = Pattern.MULTILINE | Pattern.DOTALL| Pattern.CASE_INSENSITIVE;
        Pattern pattern = Pattern.compile(regex.toString(), flags);
        Matcher matcher = pattern.matcher(data);
        return matcher.replaceAll("");
    }


}




