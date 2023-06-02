package com.tandf.hbcpdocindex;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HBCPdocIndex {

    /**
     * @param args the command line arguments
     */
    // private static String userName = "chcd";
   // private  String userName = "hbcp_user";
    //private  String userName=null;

   // private  String url ="jdbc:sqlserver://localhost:1433;databaseName=HBCPwebsite_104test";
  // private  String url =null;
   // private  String password = "overlord";

    public static void main(String[] args) throws IOException {
        Logger log = LogManager.getLogger(HBCPdocIndex.class);
/*
        String indexLocation = "C:\\ChemNetBase_data\\HBCP104\\doc_lucene\\HBCPdocsIndex_104";
        Boolean buildDocIndex=false;
        String indexLocationTbl = "C:\\ChemNetBase_data\\HBCP104\\doc_lucene\\HBCPdocsIndex_104_tbl";
        String indexLocationRows = "C:\\ChemNetBase_data\\HBCP104\\doc_lucene\\HBCPdocsIndex_104_rows";
        Boolean  buildTableIndexes=false;
        String folderToIndex = "C:\\ChemNetBaseProject\\ChemNBwebsite5_2\\web\\documents";
        String archiveIndexLocation = "C:\\ChemNetBase_data\\HBCP104\\doc_lucene\\HBCPdocsIndex_104_archive";
        String archiveFolderToIndex = "C:\\ChemNetBase_data\\HBCP_PDFarchive";
        Boolean buildArchiveIndex =false;
*/
  String url =null;
        String userName=null;
        String password=null;
        String indexLocation=null;
        String folderToIndex=null;
        Boolean buildDocIndex=false;
        String indexLocationTbl=null;
        Boolean  buildTableIndexes=false;
        String indexLocationRows=null;

        Boolean   buildTableRowIndexes=false;

        String archiveIndexLocation=null;
        String archiveFolderToIndex=null;
        Boolean buildArchiveIndex =false;

   // BuildDocIndex docBuilder = new BuildDocIndex(indexLocation, folderToIndex);
     //   docBuilder.startBuild();


    //    System.out.println("**** now Table index*****");
  //  BuildTablesIndex tableBuilder = new BuildTablesIndex(indexLocationTbl, url, userName, password);

     //   tableBuilder.startBuild();
/*
        System.out.println("****Table index completed- now Row index*****");
       BuildRowsIndex rowBuilder = new BuildRowsIndex(indexLocationRows, url, userName, password);
        rowBuilder.startBuild();
        System.out.println("****Main index completed- now Archive index*****");






         BuildArchiveIndex archiveBuilder = new BuildArchiveIndex(archiveIndexLocation, archiveFolderToIndex);
        archiveBuilder.startBuild();
        System.out.println("****Archive index completed*****");
*/
       //-------- FIRST COLLECT THE CONFIGURATION FOR ALL THE BUILDS


        System.out.println("****COMPLETED****");
        org.w3c.dom.Document confDocument;


        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           DocumentBuilder builder = factory.newDocumentBuilder();
            //Get xml file path
            String path = System.getProperty("user.dir") + "/HBCPdocIndexConf.xml";
            log.warn("XML file: " + path);
            confDocument = builder.parse(new File(path));

            //get root node
            Node indexes = confDocument.getDocumentElement();
            //get list of indexes
            NodeList indexList = indexes.getChildNodes();

            //get length of list of indexes
            int indexLength = indexList.getLength();

            for (int i = 0; i < indexLength; ++i) {
                Node index = indexList.item(i);

//           	Print node name.
                String indexNode = index.getNodeName();
                if (indexNode.startsWith("#")) {
                    continue;
                }


                //Check to see if it is the indexCreateDirectory node
                if (indexNode.equals("folderToIndex")) {
                    folderToIndex=index.getFirstChild().getTextContent();
                    continue;
                }

                if (indexNode.equals("indexLocation")) {
                    indexLocation=index.getFirstChild().getTextContent();
                    continue;
                }

                if (indexNode.equals("buildDocIndex")) {
                   String temp=index.getFirstChild().getTextContent();
                   if(temp.equalsIgnoreCase("true")){
                       buildDocIndex=true;
                   }
                    continue;
                }
                if (indexNode.equals("indexLocationTbl")) {
                    indexLocationTbl=index.getFirstChild().getTextContent();
                    continue;
                }
                if (indexNode.equals("buildTableIndexes")) {
                    String temp=index.getFirstChild().getTextContent();
                    if(temp.equalsIgnoreCase("true")){
                        buildTableIndexes=true;
                    }
                    continue;
                }
                if (indexNode.equals("indexLocationRows")) {
                    indexLocationRows=index.getFirstChild().getTextContent();
                    continue;
                }
                if (indexNode.equals("buildTableRowIndexes")) {
                    String temp=index.getFirstChild().getTextContent();
                    if(temp.equalsIgnoreCase("true")){
                        buildTableRowIndexes=true;
                    }
                    continue;
                }

                if (indexNode.equals("archiveIndexLocation")) {
                    archiveIndexLocation=index.getFirstChild().getTextContent();
                    continue;
                }
                if (indexNode.equals("archiveFolderToIndex")) {
                    archiveFolderToIndex=index.getFirstChild().getTextContent();
                    continue;
                }
                if (indexNode.equals("buildArchiveIndex")) {
                    String temp=index.getFirstChild().getTextContent();
                    if(temp.equalsIgnoreCase("true")){
                        buildArchiveIndex=true;
                    }
                    continue;
                }

                if (index.hasAttributes()) {
                    NamedNodeMap attributes = index.getAttributes();

                    // loop through attributes setting the indexName and schema
                    for (int numAttr = 0; numAttr < attributes.getLength(); numAttr++) {
                        Node attribute = attributes.item(numAttr);
                        log.warn(attribute.getNodeName() + "=" + attribute.getNodeValue());

                        if (attribute.getNodeName().equalsIgnoreCase("url")) {
                            // url = attribute.getNodeValue();
                            url=attribute.getNodeValue();
                        } else if (attribute.getNodeName().equalsIgnoreCase("pw")) {
                            // pw = attribute.getNodeValue();
                            password= attribute.getNodeValue();

                        } else if (attribute.getNodeName().equalsIgnoreCase("uid")) {
                            //  uid = attribute.getNodeValue();
                            userName=attribute.getNodeValue();
                        }
                    }
                }
            }


        } catch (ParserConfigurationException pce) {
            log.error("There was parser an exception",pce);
            pce.printStackTrace();
            System.exit(1);
        } catch (IOException ioe) {
            log.error("There was an IO exception",ioe);
            ioe.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            // general error
            log.error("There was an exception",e);
            System.exit(1);

        }

//-- now check that the input directories are all present

        if(buildDocIndex){
            File loc = new File(folderToIndex);
            if (!loc.exists()){
                log.error("folderToIndex does not exist, please check the HBCPdocIndexConf");
                System.exit(1);
            }
        }

        if(buildTableIndexes || buildTableRowIndexes){

            if (userName==null || password== null || url==null){
                log.error("database has not been set in HBCPdocIndexConf");
                System.exit(1);
            }
        }

        if(buildArchiveIndex){
            File loc = new File(archiveFolderToIndex);
            if (!loc.exists()){
                log.error("folderToIndex does not exist, please check the HBCPdocIndexConf");
                System.exit(1);
            }
        }

//-----------now start building-------------------------------------
        if(buildDocIndex){
             BuildDocIndex docBuilder = new BuildDocIndex(indexLocation, folderToIndex);
            docBuilder.startBuild();
            log.info("**** Document Index Completed*****");
        }
        if(buildTableIndexes) {
            BuildTablesIndex tableBuilder = new BuildTablesIndex(indexLocationTbl, url, userName, password);
            tableBuilder.startBuild();
            log.info("**** Table Index Completed*****");
        }
        if(buildTableRowIndexes) {
            BuildRowsIndex rowBuilder = new BuildRowsIndex(indexLocationRows, url, userName, password);
            rowBuilder.startBuild();
            log.info("**** Table Row Index Completed*****");
        }

        if(buildArchiveIndex) {
            BuildArchiveIndex archiveBuilder = new BuildArchiveIndex(archiveIndexLocation, archiveFolderToIndex);
            archiveBuilder.startBuild();
            log.info("**** Archive Index Completed*****");
        }

        log.info("****** Build Completed***************");

        //     SearchIndex searcher = new SearchIndex(indexLocation);
        //    try {
        //  searcher.luceneDocSearch("*omposi* AND of AND the AND  Earth","AND" , "nu*i");
        // searcher.luceneDocSearch("*omposi* AND of AND the AND  Earth","AND" , "");
        /*    String query1 = "*Calciumsdsd*";
         String query2 = "3-(Ethenyloxy)-1-propene";

         String andOr = "OR";*/
        //  SearchIndex searcherT = new SearchIndex(indexLocationTbl);
        //   List<TableResultsEnt> tableResults = searcherT.luceneTableSearchAll(query1, query2, andOr);

        /*   System.out.println("***********table search completed***************");
         SearchIndex searcherR = new SearchIndex(indexLocationRows);

         String tableRef = "int_tbl_03_01_01";
         List<Integer> rowNums = searcherR.luceneRowSearch(query1, query2, andOr, tableRef);
         System.out.println("****COMPLETED****");*/




        // searcher.ListTerms();
        // } catch (ParseException ex) {
        //   System.out.println(ex.getLocalizedMessage());
        // Logger.getLogger(HBCPdocIndex.class.getName()).log(Level.SEVERE, null, ex);
        //  }
    }
}
