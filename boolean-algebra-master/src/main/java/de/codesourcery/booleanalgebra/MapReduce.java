package de.codesourcery.booleanalgebra;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class MapReduce {

	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
		private Text testName = new Text();
		private Text lineNoAndClass = new Text();

		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) 
		throws IOException {
			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line);
			if (tokenizer.hasMoreTokens()) {
				lineNoAndClass.set(tokenizer.nextToken() + " : " + tokenizer.nextToken());
			}
			if (tokenizer.hasMoreTokens()) {
				testName.set(tokenizer.nextToken());
			}
			output.collect(lineNoAndClass, testName);
		}
	}

	public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) 
		throws IOException {
			String tests = "";
			while (values.hasNext()) {
				tests += values.next().get() + " , ";
			}
			output.collect(key, new Text(tests));
		}
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(MapReduce.class);
		conf.setJobName("MapReduce");
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		conf.setMapperClass(Map.class);
		conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
		JobClient.runJob(conf);
	}
}
