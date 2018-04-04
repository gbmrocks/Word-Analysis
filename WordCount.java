
import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class WordCount {

    private final static IntWritable one = new IntWritable(1);
    private final static String delims = "<\'>[]()%,+#& .:;-=_/\\\"";
    private final static int exact_mode = 1; //change to 0 if you want word containing keyword too.
    //i.e. education educational miseducational

    public static class Map extends
            Mapper<LongWritable, Text, Text, IntWritable> {

        private Text word = new Text();

        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString();
            line=stringurldecode(line);
            line = line.toLowerCase().trim();
            String str = "";
            StringTokenizer tokenizer = new StringTokenizer(line, delims);
            while (tokenizer.hasMoreTokens()) {
                str = tokenizer.nextToken();
                if (keyword_matcher(str)) {
                    word.set(str);
                    context.write(word, one);
                }
            }
        }
    }

    public static class Reduce extends
            Reducer<Text, IntWritable, Text, IntWritable> {

        public void reduce(Text key, Iterable<IntWritable> values,
                Context context) throws IOException, InterruptedException {
            int sum = 0;
            Iterator<IntWritable> it = values.iterator();
            while (it.hasNext()) {
                IntWritable v = new IntWritable(Integer.valueOf(it.next().toString()));
                sum += v.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }

    public static String stringurldecode(String line) {
        if (exact_mode != 1) {
            try {
                line = URLDecoder.decode(line, "UTF-8");
            } catch (Exception ex) {
            }
            return line;
        }
        return line;
    }

    public static boolean keyword_matcher(String key) {
        if (exact_mode == 1) {
            if (key.toLowerCase().equals("education")
                    || key.toLowerCase().equals("politics")
                    || key.toLowerCase().equals("sports")
                    || key.toLowerCase().equals("agriculture")) {
                return true;
            }
        } else {
            if (key.toLowerCase().contains("education")
                    || key.toLowerCase().contains("politics")
                    || key.toLowerCase().contains("sports")
                    || key.toLowerCase().contains("agriculture")) {
                return true;
            }
        }
        return false;
    }

    public static class Map_file extends
            Mapper<LongWritable, Text, Text, IntWritable> {

        private Text word = new Text();
        private Text filename = new Text();

        public void map(LongWritable key, Text value, Mapper.Context context)
                throws IOException, InterruptedException {
            String name = ((FileSplit) context.getInputSplit()).getPath().getName();
            String line = value.toString();
            line=stringurldecode(line);
            line = line.toLowerCase().trim();
            String str = "";
            StringTokenizer tokenizer = new StringTokenizer(line, delims);
            while (tokenizer.hasMoreTokens()) {
                str = tokenizer.nextToken();
                if (keyword_matcher(str)) {
                    word.set(name + ":" + str);
                    context.write(word, one);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        Job job = new Job(conf, "word count");
        job.setJarByClass(WordCount.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path("countrywise"));
        job.waitForCompletion(true);

        Configuration conf1 = new Configuration();
        Job job1 = new Job(conf1, "word count");
        job1.setJarByClass(WordCount.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(IntWritable.class);
        job1.setMapperClass(Map_file.class);
        job1.setReducerClass(Reduce.class);
        job1.setInputFormatClass(TextInputFormat.class);
        job1.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job1, new Path(args[0]));
        FileOutputFormat.setOutputPath(job1, new Path("statewise"));
        job1.waitForCompletion(true);

        //will give output in sorted pair 
        Configuration conf2 = new Configuration();
        Job job2 = Job.getInstance(conf2, "word count");
        job2.setJarByClass(WordCount.class);
        job2.setMapperClass(Map2.class);
        job2.setCombinerClass(Reduce2.class);
        job2.setReducerClass(Reduce2.class);
        job2.setOutputKeyClass(CustomKey.class);
        job2.setOutputValueClass(Text.class);
        job2.setPartitionerClass(NaturalKeyPartitioner.class);
        job2.setGroupingComparatorClass(NaturalKeyGroupingComparator.class);
        job2.setSortComparatorClass(CustomKeyComparator.class);
        FileInputFormat.addInputPath(job2, new Path("statewise"));
        FileOutputFormat.setOutputPath(job2, new Path("sorted"));
        System.exit(job2.waitForCompletion(true) ? 0 : 1);
    }

    public static class CustomKey implements WritableComparable<CustomKey> {

        private String keyword;
        private Long count;

        public CustomKey() {
        }

        public CustomKey(String keyword, Long count) {
            this.keyword = keyword;
            this.count = count;
        }

        public void SetCustomKey(String keyword, Long count) {
            this.keyword = keyword;
            this.count = count;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        @Override
        public void write(DataOutput d) throws IOException {
            WritableUtils.writeString(d, keyword);
            d.writeLong(count);
            // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void readFields(DataInput di) throws IOException {
            keyword = WritableUtils.readString(di);
            count = di.readLong();
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int compareTo(CustomKey o) {
            int result = keyword.compareTo(o.keyword);
            if (result != 0) {
                return result;
            }
            return count.compareTo(o.getCount());
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String toString() {
            return keyword + ":" + count;
        }

    }

    public static class CustomKeyComparator extends WritableComparator {

        protected CustomKeyComparator() {
            super(CustomKey.class, true);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public int compare(WritableComparable w1, WritableComparable w2) {
            CustomKey k1 = (CustomKey) w1;
            CustomKey k2 = (CustomKey) w2;
            int result = k1.getKeyword().compareTo(k2.getKeyword());
            if (0 == result) {
                result = -1 * k1.getCount().compareTo(k2.getCount());
            }
            return result;
        }
    }

    public static class NaturalKeyGroupingComparator extends WritableComparator {

        protected NaturalKeyGroupingComparator() {
            super(CustomKey.class, true);
        }

        @Override
        public int compare(WritableComparable w1, WritableComparable w2) {
            CustomKey k1 = (CustomKey) w1;
            CustomKey k2 = (CustomKey) w2;
            return k1.getKeyword().compareTo(k2.getKeyword());
        }
    }

    public static class NaturalKeyPartitioner extends Partitioner<CustomKey, DoubleWritable> {

        @Override
        public int getPartition(CustomKey key, DoubleWritable val, int numPartitions) {
            int hash = key.getKeyword().hashCode();
            int partition = hash % numPartitions;
            return partition;
        }
    }

    public static class Map2
            extends Mapper<LongWritable, Text, CustomKey, Text> {
        private Text keyword = new Text(), state = new Text();
        private String[] string, tokens;
        private Long count;
        private CustomKey customKey = new CustomKey();
        public void map(LongWritable key, Text value, Context context
        ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString(), "\n");
            while (itr.hasMoreTokens()) {
                tokens = itr.nextToken().split("\t");
                string = tokens[0].split(":");
                state.set(string[0]);
                keyword.set(string[1]);
                count = Long.parseLong(tokens[1].trim());
                customKey.SetCustomKey(keyword.toString(), count);
                context.write(customKey, state);
            }
        }
    }

    public static class Reduce2
            extends Reducer<CustomKey, Text, CustomKey, Text> {
        @Override
        public void reduce(CustomKey key, Iterable<Text> values,
                Context context
        ) throws IOException, InterruptedException {
            int count = 0;
            Iterator<Text> it = values.iterator();
            while (it.hasNext() && count < 3) {
                Text v = new Text(it.next().toString());
                context.write(key, v);
                count++;
            }
        }
    }
}

