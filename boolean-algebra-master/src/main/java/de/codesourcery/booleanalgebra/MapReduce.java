package de.codesourcery.booleanalgebra;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.ArrayList;

public class MapReduce {
	public static HashMap<String, Integer> testLineCount = new HashMap<String, Integer>();

	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
		private Text testName = new Text();
		private Text lineNoAndClass = new Text();

		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line);
			if (tokenizer.hasMoreTokens()) {
				lineNoAndClass.set("<" + tokenizer.nextToken() + ":" + tokenizer.nextToken() + ", ");
			}
			if (tokenizer.hasMoreTokens()) {
				String test = tokenizer.nextToken();
				testLineCount.put(test,
						testLineCount.getOrDefault(test, 0) + 1);
				testName.set(test);
			}
			output.collect(lineNoAndClass, testName);
		}
	}

	public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			StringBuilder tests = new StringBuilder("(");
			ArrayList<String> testNameList = new ArrayList<String>();
			while (values.hasNext()) {
				testNameList.add(values.next().toString());
			}
			testNameList = sortTestNameList(testNameList);
			for (String testName: testNameList) {
				tests.append(testName + ", ");
			}
			tests.delete(tests.length()-2, tests.length());
			tests.append(")>");
			output.collect(key, new Text(tests.toString()));
		}
	}

	public static ArrayList<String> sortTestNameList(ArrayList<String> testNameList) {
		if (testNameList.size() < 2) return testNameList;
		boolean sorted = true;
		while (sorted) {
			sorted = false;
			for (int i = 1; i < testNameList.size(); i++) {
				if (((testLineCount.get(testNameList.get(i)) ==
						testLineCount.get(testNameList.get(i-1))) &&
						(testNameList.get(i).compareToIgnoreCase(testNameList.get(i-1)) > 0))
						|| (testLineCount.get(testNameList.get(i)) >
						testLineCount.get(testNameList.get(i-1)))) {
					sorted = true;
					String temp = testNameList.get(i);
					testNameList.set(i, testNameList.get(i-1));
					testNameList.set(i-1, temp);
				}
			}
		}
		return testNameList;
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(MapReduce.class);
		conf.setJobName("MapReduce");
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
		JobClient.runJob(conf);
	}
}