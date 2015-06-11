# Pipeline for Timestamping Sub-documents of the Web

## How to use
###Jar package
You can download the Jar from [here](https://www.dropbox.com/s/rn0ghvi5ufv3mbb/pipeline-0.0.1-SNAPSHOT.jar?dl=0).

     Java -jar pipeline.jar /root_folder_of_Clueweb_12/ /root_folder_of_output/ 50 0.7 1996 2012

The threshold of the length is 50, which means that only the paragraphs longer than 50 are timestamped.  
The threshold of the similarity is 0.7, which means that if the value of distance JaroWinklerTFIDF between 2 paragraphs is larger than 0.7, we treat them as the same.  
The begin year is 1996 and the end year is 2012, which means that we only extract the historical pages between 1996 and 2012.

###Source code
1. Compile    
```
mvn compile
```

2. Package
```
mvn clean package -Dmaven.test.skip=true
```
3. Run the Jar

##Result organization
    /root_folder_of_output
    /root_folder_of_output/historicalPages
    /root_folder_of_output/taggedPages
    /root_folder_of_output/historicalPagesDownload.log
    /root_folder_of_output/paragraphFeatures.arff

historicalPages is the root folder of all the historical pages downloaded from Internet Archive. For each clueweb file, the folder name is the MD5 code of its url.  
taggedPages is the rootfolder of all the tagged clueweb files. The file name is trecID_MD5.

    
   
        