package com.mzy.hadoop.totalsort;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler.RandomSampler;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;

public class TotalSort {

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		// TODO Auto-generated method stub
		
		Path inputPath = new Path(args[0]);
		Path outputPath = new Path(args[1]);
		// �����ļ���·��
		Path partitionPath = new Path(args[2]);
		
		int numReduceTask = Integer.parseInt(args[3]);
		
		// ��һ������ �ᱻѡ�еĸ��� �ڶ�������ѡȡ��������  �����������Ƕ�ȡ�����InputSplit�ĸ���
		RandomSampler<Text,Text> sampler = new InputSampler.RandomSampler<Text,Text>(0.1, 1000,10);
		
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf,"totalSort");
		
		job.setJarByClass(TotalSort.class);
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setNumReduceTasks(numReduceTask);
		
		// ����Partition��
		job.setPartitionerClass(TotalOrderPartitioner.class);
		
		FileInputFormat.setInputPaths(job, inputPath);
		FileOutputFormat.setOutputPath(job, outputPath);
		
		// д������ļ�
		InputSampler.writePartitionFile(job, sampler);
		
		System.exit(job.waitForCompletion(true) ? 1 :0);
	}

}
