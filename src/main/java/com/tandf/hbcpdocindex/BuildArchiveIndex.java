package com.tandf.hbcpdocindex;

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
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class BuildArchiveIndex {
    private Logger log = LogManager.getLogger(BuildArchiveIndex.class);
    private IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<File>();
    private String indexLocation;
    private String folderToIndex;

    public static final String[] ENGLISH_STOP_WORDS = {
         "about", "above", "adds", "after", "all", "allows", "almost",
            "alone", "along", "also", "alter", "although", "an", "and", "any", "are", "because", "become", "been", "before",
            "began", "begun", "begin", "best", "between", "both", "but", "by", "can", "change", "changed", "changes", "cm",
            "decided", "descr", "did", "does", "either", "end", "every", "except", "for", "form", "full", "further",
            "give", "going", "has", "have", "having", "ie",  "if", "ii", "iii", "into", "is", "it", "iv", "kg", "kj",
            "known", "like", "made", "many", "mg", "more", "most",  "need", "now", "news", "nm", "no", "not", "of",
            "off", "on", "once", "one", "opt", "or", "other", "over", "own", "probable", "probably", "props", "reach", "receive",
            "received", "related", "remain", "remained", "remove", "reported", "seek", "seeking", "seemed", "seen",
            "several", "showed", "shown", "taken", "than", "that", "the", "their", "them", "then", "there", "they", "this", "those", "three",
            "two", "under", "up", "very", "was", "were", "when", "whereas", "where", "which", "while", "whilst", "who",
            "whom", "whose", "will", "with", "within", "without", "would", "(1)", "(2)", "(3)", "(4)", "(5)", "(6)", "(7)", "?", "??"
    };


    public BuildArchiveIndex(String indexLocation, String folderToIndex) throws IOException {
        this.indexLocation = indexLocation;
        this.folderToIndex = folderToIndex;
        //clear out the old index
        File index = new File(indexLocation);
        if (index.exists()) {

            FileUtils.deleteDirectory(index);
        }
        CharArraySet stopSet = StopFilter.makeStopSet(ENGLISH_STOP_WORDS);
        HBCPdocAnalyzer analyzer = new HBCPdocAnalyzer(stopSet);


        Directory dir = FSDirectory.open( new File( indexLocation).toPath() );
        IndexWriterConfig config = new IndexWriterConfig(analyzer);


        writer = new IndexWriter(dir, config);
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

    public void indexFileOrDirectory(String fileName) throws IOException {
        //===================================================
        //gets the list of files in a folder (if user has submitted
        //the name of a folder) or gets a single file name (is user
        //has submitted only the file name)
        //===================================================
        addFiles(new File(fileName));

        int originalNumDocs =writer.getDocStats().numDocs;
        for (File f : queue) {
            FileReader fr = null;
            try {
                Document doc = new Document();

                //===================================================
                // add contents of file
                //===================================================
                fr = new FileReader(f);


                PDFTextStripper pdfStripper = null;
                PDDocument pdDoc = null;
                COSDocument cosDoc = null;
                RandomAccessBufferedFileInputStream raFile = new RandomAccessBufferedFileInputStream(f);
                PDFParser parser = new PDFParser(raFile);
                parser.parse();
                cosDoc = parser.getDocument();
                pdfStripper = new PDFTextStripper();
                pdDoc = new PDDocument(cosDoc);
                pdfStripper.setStartPage(1);
                // pdfStripper.setEndPage(5);

                String parsedText = pdfStripper.getText(pdDoc);
                System.out.println(parsedText);

                String path = f.getPath().toString();
                System.out.println(path);


                String[] temp = path.split("\\\\");
                Integer length = temp.length;
                Integer edition = Integer.valueOf(temp[length - 2]);

                if (edition != 0) {
                    System.out.println("Edition:   " + edition);

                    String tblSectionsId = null;
                    String docRef;
                    if (edition < 97) { //file name convention changes from 97th. this builds the section id from the doc ref part of the filename
                        tblSectionsId = "10" + f.getName().substring(0, 2);
                        docRef = f.getName().substring(0, 5);
                    } else if (edition < 100){
                        tblSectionsId = "10" + f.getName().substring(7, 9);
                        docRef = f.getName().substring(7, 9) + "_" + f.getName().substring(10, 12);
                        System.out.println("fmane "+ f.getName());
                        System.out.println("docref "+  docRef);
                        System.out.println("tblSectionsId "+  tblSectionsId);
                    }else{
                        tblSectionsId = "10" + f.getName().substring(8, 10);
                        docRef = f.getName().substring(8, 10) + "_" + f.getName().substring(11, 13);
                    }

                    Integer.parseInt(tblSectionsId);
                    System.out.println("sectionId: " + tblSectionsId);
                    System.out.println("docRef:    " + docRef);

                    //  Field contentField = new Field("contents", getAllText(f), textFieldType);
                    //  doc.add(contentField);
                    doc.add(new TextField("contents", parsedText, Field.Store.NO));
                    doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                    doc.add(new StringField("edition", edition.toString(), Field.Store.YES));
                    doc.add(new StringField("filename", f.getName(), Field.Store.YES));
                    doc.add(new SortedDocValuesField("filename", new BytesRef(f.getName())));
                    doc.add(new StringField("sectionsid", tblSectionsId, Field.Store.YES));
                    doc.add(new StringField("docref", docRef, Field.Store.YES));
                    doc.add(new StringField("all_entries", "y", Field.Store.YES));


                    writer.addDocument(doc);
                    cosDoc.close();
                    pdDoc.close();

                    System.out.println("Added: " + f.getName());
                } else {
                    System.out.println("Skipped docs removed before 94th Edn.");
                }

            } catch (Exception e) {
              log.error("Could not add: " + f.getName() + " " + e.getLocalizedMessage() + " " +e.getMessage());
                System.out.println("---Could not add: " + e.getStackTrace());
            } finally {
                fr.close();
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
            if (filename.endsWith(".pdf")) {
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
        for (String line : Files.readAllLines(Paths.get(f.getAbsolutePath()), charset)) {
            textFileContent += line;
        }
        return textFileContent;
    }
}
