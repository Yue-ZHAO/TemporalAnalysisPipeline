# Pipeline for Timestamping Sub-documents of the Web

## How to use
1. Compile
    
    mvn compile

2. Package
    
    mvn clean package -Dmaven.test.skip=true

3. Run the Jar

##Example
     Java -jar pipeline-0.0.1-SNAPSHOT.jar /root folder of Clueweb 12/ /root folder of output/ 50 0.7 1996 2012

The threshold of the length is 50, which means that only the paragraphs longer than 50 are timestamped.  
The threshold of the similarity is 0.7, which means that if the value of distance JaroWinklerTFIDF between 2 paragraphs is larger than 0.7, we treat them as the same.  
The begin year is 1996 and the end year is 2012, which means that we only extract the historical pages between 1996 and 2012.

##Result organization
    /root folder of output
    /root folder of output/historicalPages
    /root folder of output/taggedPages
    /root folder of output/historicalPagesDownload.log
    /root folder of output/paragraphFeatures.arff

historicalPages is the root folder of all the historical pages downloaded from Internet Archive. For each clueweb file, the folder name is the MD5 code of its url.  
taggedPages is the rootfolder of all the tagged clueweb files. The file name is trecID_MD5.

    
   
        