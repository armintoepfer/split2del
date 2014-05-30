<h1 align="center"><Split2Del</h1>

### CONTENT:
This java command line application merges split read alignments to reconstruct large deletions.

### CITATION:
If you use Split2Del, please cite <i>Töpfer A.</i> https://github.com/armintoepfer/split2del

### DOWNLOAD:
Please get the latest binary at [releases](https://github.com/armintoepfer/split2del/releases/latest).

### FEATURES:
 - Currently only finds split reads, not yet merged.

### ISSUES:
Please open an issue on github or write me a [mail](https://github.com/armintoepfer/split2del/blob/master/README.md#contact)

- - -

#### PREREQUISITES TO RUN:
 - JDK 7 (http://jdk7.java.net/)

## RUN:  
 `java -jar split2del.jar -i alignment.bam -r reference.fasta`  
 
```
  -i          INPUT  : Alignment file in BAM format (required).
  -o          PATH   : Path to the output directory (default: current directory).
```

## Technical details
#####To minimize the memory consumption and the number of full garbage collector executions, use:
`java -XX:NewRatio=9 -jar split2del.jar`

#####If your dataset is very large and you run out of memory, increase the heapspace with:
`java -XX:NewRatio=9 -Xms2G -Xmx10G -jar split2del.jar`

### Help:
 Further help can be showed by running without additional parameters:
  `java -jar split2del.jar`

### PREREQUISITES COMPILE (only for dev):
 - Maven 3 (http://maven.apache.org/)

### INSTALL (only for dev):
    cd Split2del
    mvn -DartifactId=samtools -DgroupId=net.sf -Dversion=1.9.6 -Dpackaging=jar -Dfile=src/main/resources/jars/sam-1.96.jar -DgeneratePom=false install:install-file
    mvn clean package
    java -jar Split2del/target/split2del.jar

## CONTACT:
    Armin Töpfer
    armin.toepfer (at) gmail.com
    http://www.bsse.ethz.ch/cbg/people/toepfera

## LICENSE:
 GNU GPLv3 http://www.gnu.org/licenses/gpl-3.0
