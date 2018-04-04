i have created public ami by steps shown in ami.txt
AMI : 	ami-64201372
region : us-east-1
name : HADOOPAMI_GAURANG_MODY

1) now launch 4 machine from above mentioned 

2*) upload pem key to /root/.ssh folder
	change permisson using
	chmod 600 cloudclass.pem	

3)update ip hostname pairs in following file. all machines.
	nano /etc/hosts
	ec2-34-229-16-65.compute-1.amazonaws.com HadoopMaster
	ec2-54-146-158-39.compute-1.amazonaws.com Slave1
	ec2-54-152-165-179.compute-1.amazonaws.com Slave2
	ec2-54-172-52-210.compute-1.amazonaws.com Slave3


	
*4) update ip address and hostname in this file
    nano ~/.ssh/config
	
	
*5) generate ssh key for password less ssh 
    cd /
    cd root/.ssh/
	ssh-keygen -f ~/.ssh/id_rsa -t rsa -P "" 
	cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
	cat ~/.ssh/id_rsa.pub | ssh HadoopMaster 'cat >> ~/.ssh/authorized_keys' 
	cat ~/.ssh/id_rsa.pub | ssh Slave1 'cat >> ~/.ssh/authorized_keys' 
	cat ~/.ssh/id_rsa.pub | ssh Slave2 'cat >> ~/.ssh/authorized_keys' 
	cat ~/.ssh/id_rsa.pub | ssh Slave3 'cat >> ~/.ssh/authorized_keys' 
	
*6)
all steps from now will run on /usr/local/hadoop/etc/hadoop directory

Edit public dns in following files.
1) masters
2)core-site.xml
3)yarn-site.xml
4)mapred-site.xml
5)slaves


7)run this command on all slave nodes
	rm -R /usr/local/hadoop  &&  chmod -R 777 /usr/local/
	

	
	
8)run following command it will configure hadoop in all nodes. 


scp -r /usr/local/hadoop ubuntu@Slave1:/usr/local && scp -r /usr/local/hadoop ubuntu@Slave2:/usr/local && scp -r /usr/local/hadoop ubuntu@Slave3:/usr/local	

9)

format and start hadoop

hadoop namenode -format
bash /usr/local/hadoop/sbin/start-dfs.sh
bash /usr/local/hadoop/sbin/start-yarn.sh

verify hadoop running all name node & data node.
jps

run follwing command on each node if you dont see data node in jps output

bash /usr/local/hadoop/sbin/hadoop-daemon.sh start datanode


10) To run program
	create a new directory 
	mkdir project
	cd project
	mkdir classes
	
	Upload main.sh , run.sh & WordCount.java to the project folder.
	upload states.tar.gz to master node
	
	tar -xzvf  states.tar.gz
	1)
		hdfs dfs -mkdir -p states
		hdfs dfs -put states/* states/
		hdfs dfs -ls  states/
	
	2) either follow step 1 or 2. 
	bash main.sh
	
	3) run run.sh
	
	After successful execution you will see 3 directories in project folder.
	1)countrywise : shows total count of keywords
	2)statewise : show total count of keywords state wise
	3)sorted :  shows top3 files containt keywods
	
	Alternatively you can see output.txt file in project directory which combines all outputs

11)	My program is having 2 mode.
   1) exact word mode. (check : output1.txt)
		this mode will exactly match the keyword.
   2) all words containing the keywords. (check : output2.txt)
		this mode will show all words containg the keywords.
		this mode will also use url decode.
		e.g. Education : education,educational,miseducational.
	To change word mode 
	 private final static int exact_mode = 1; //change to 0 if you want word containing keyword too.

12) following guides,examples i referred while doing this project.
    1)http://pingax.com/install-apache-hadoop-ubuntu-cluster-setup/
	2)http://www.javamakeuse.com/2016/04/secondary-sort-example-in-hadoop-mapreduce.html
    3)https://github.com/hadoopessence/MapReduce/blob/master/java/src/org/apache/hadoopessence/composite/CompositeKeyMR.java
    4)https://vangjee.wordpress.com/2012/03/20/secondary-sorting-aka-sorting-values-in-hadoops-mapreduce-programming-paradigm/
    5)http://blog.cloudera.com/blog/2011/04/simple-moving-average-secondary-sort-and-mapreduce-part-3/

		
	