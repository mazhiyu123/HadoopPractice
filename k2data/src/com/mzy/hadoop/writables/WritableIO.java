package com.mzy.hadoop.writables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Partitioner;

public class WritableIO {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		IntWritable writable = new IntWritable();
		writable.set(1331321);// ��ֵ
		//��writable���л�Ϊbyte[]����
		byte[] bytes = serialize(writable);
		
		for (int i =0 ; i < bytes.length ; i++){
			System.out.println(bytes[i]);
		}
		
		IntWritable writable2 = new IntWritable();
		// ��bytes[] �����л�ΪIntWritable����
		deserialize(writable2,bytes);
		System.out.println(writable.get());
	}
	
	// ���л�����
	public static byte[] serialize(IntWritable writable)throws IOException	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(out);
		writable.write(dataOut);
		dataOut.close();
		return out.toByteArray();
	}
	
	//�����л�
	public static byte[] deserialize(IntWritable writable,byte[] bytes)throws IOException{
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		DataInputStream dataInput = new DataInputStream(in);
		writable.readFields(dataInput);
		dataInput.close();
		return bytes;
	}
	
	//�Լ�ʵ�ֵķ�������
	public class MyPationer extends Partitioner<Text,IntWritable>{
		@Override
		public int getPartition(Text key,IntWritable value,int numReducerTasks){
			return (new Boolean(value.get() > 10000).hashCode() & Integer.MAX_VALUE) % numReducerTasks;
		}
	}
	
	// �Զ���ıȽϹ���
	public class MyWritableComparator extends WritableComparator{
		protected MyWritableComparator(){
			super(IntWritable.class,true);
		}
		
		@Override
		public int compare(WritableComparable a,WritableComparable b){
			IntWritable x = (IntWritable )a;
			IntWritable y = (IntWritable)b;
			return (x.get()%5 - y.get()%5) > 0  ? 1 : -1; 
		}
	}

}
