export CLASSPATH=$HADOOP_HOME/share/hadoop/common/hadoop-common-2.8.0.jar:$HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-client-core-2.8.0.jar:$HADOOP_HOME/share/hadoop/common/lib/commons-cli-1.2.jar
 rm -r classes/* countrywise statewise sorted WordCount.jar output.txt
 hdfs dfs -rm -R /countrywise countrywise /statewise statewise /sorted sorted
 javac -d classes WordCount.java
 jar -cvf WordCount.jar classes/*.class
 hadoop jar WordCount.jar WordCount states/ /out
 hdfs dfs -get countrywise countrywise
 hdfs dfs -get statewise statewise
 hdfs dfs -get sorted sorted
 printf "======================================Overall======================================\n" >> output.txt
 cat countrywise/part-r-00000 >> output.txt
 printf "\n======================================Statewise======================================\n" >> output.txt
 cat statewise/part-r-00000 >> output.txt
 printf "\n======================================Top 3======================================\n" >>  output.txt
 cat sorted/part-r-00000 >> output.txt
