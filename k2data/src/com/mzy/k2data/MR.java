package com.mzy.k2data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Counters.Counter;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import java.lang.reflect.Field;  

public class MR {
	/*
	 * ��������data-input.csv������
	 * ���ӣ�
	 * Map�������ݣ�did1,1.1,1.2,1.3
	 * Map������ݣ�key:1  value:datafile:1,1.1,1.2,1.3
	 */
	public static class DataFileMap extends  Mapper<LongWritable, Text, Text, MyDataType>
	{
		//Ϊÿһ��������ӵ��б�ʶ��
		int row=0;
		public void map(LongWritable line, Text value,Context context)throws IOException, InterruptedException
		{
			//ȥ��ÿһ�п�ͷ����ĸ
			String newvalue=value.toString().substring(3);
			//�����value
			MyDataType data_v=new MyDataType("datafile",newvalue);
			//�����key
			Text data_k=new Text((String.valueOf(++row)));
			context.write(data_k, data_v);
		}

	}

	/*
	 * ��������range-input.csv������
	 * ���ӣ�
	 * Map�������ݣ�2��2��4
	 * Map������ݣ�key:2  value:2,4
	 */
	public static class RangeFileMap extends  Mapper<LongWritable, Text, Text, MyDataType>
	{
		public   String  range_k=null;
        public   String  newvalue=null;
        
        /*
         * ������ȡ�ֲ�ʽ����
         */
        @Override
        protected void setup(Mapper<LongWritable, Text, Text, MyDataType>.Context context)
                throws IOException, InterruptedException {
        	super.setup(context);
            if (context.getCacheFiles() != null&& context.getCacheFiles().length > 0) {
            	//ȡ�û�����ļ�
                File cacheFile = new File("./cachePath");
                InputStreamReader read = new InputStreamReader(new FileInputStream(cacheFile));
                BufferedReader bufferedReader = new BufferedReader(read);
                
                //��һ�����ݣ��з�
                String line;
				while ((line = bufferedReader.readLine()) != null) {
					 range_k=line.substring(0, 1);
	                 newvalue=line.substring(2);
				}
				bufferedReader.close();
            }else {
            	System.out.println("��ȡ�ֲ�ʽ����ʧ��");
            }
        }
        

		public void map(LongWritable line, Text value,Context context)throws IOException, InterruptedException
		{	 
			 //�����key
			 range_k=value.toString().substring(0, 1);
			 
             newvalue=value.toString().substring(2);
             //�����value
             MyDataType range_v=new MyDataType("rangefile",newvalue);
			
			 context.write(new Text(range_k), range_v);
		}

	}
	

	/*
	 * �����������ݺ��쳣���ݵ�����
	 */
	public static class ResReduce extends  Reducer<Text, MyDataType, Text, NullWritable>
	{	
		//���ö�·���
		 private MultipleOutputs<Text, NullWritable> outPath;
		 public void setup(Context context) {
		   outPath = new MultipleOutputs<Text, NullWritable>(context);
		 }
		
		
		public void reduce(Text fileName, Iterable<MyDataType> values, Context context) throws IOException, InterruptedException
		{
			
			// �ֱ��¼���������ļ�������
			ArrayList<Float> fromDataFile=new ArrayList<Float>();
			ArrayList<Float> fromRangeFile=new ArrayList<Float>();
			//��¼�������ݵĳ���
			int dataLength=-1;
			int rangeLength=-1;
		
			for(MyDataType data:values){
				if ("datafile".equals(data.getFlag()))
				{//��������DataFileMap������
					String[] datatokens=SeparateDataMain.DELIMITER.split(data.getValue());
					dataLength=datatokens.length;
					for(int i=0;i<dataLength;i++){
						fromDataFile.add(Float.parseFloat(datatokens[i]));
					}
				} else if ("rangefile".equals(data.getFlag()))
				{//��������RangeFileMap������
					String[] rangetokens=SeparateDataMain.DELIMITER.split(data.getValue());
					rangeLength=rangetokens.length;
					for(int i=0;i<rangeLength;i++){
						fromRangeFile.add(Float.parseFloat(rangetokens[i]));
					}
				}
			}
			
			
			//�Զ�������� �������ݼ���
			Counter normalCounter=(Counter) context.getCounter(ResultEnum.NORMALCOUNTER);
			//�쳣���ݼ���
			Counter abnormalCounter=(Counter) context.getCounter(ResultEnum.ABNORMALCOUNTER);
			
			if(rangeLength != -1){
				 //����range-input.csv�ļ����е���Ҫɸѡ��һ������
				 
				
				//ȷ���������ݵķ�Χ
				float min=fromRangeFile.get(0);
				float max=fromRangeFile.get(1);
				
				//��¼��������
				StringBuilder res1=new StringBuilder("deviceId");
				//��¼�쳣����
				StringBuilder res2=new StringBuilder("deviceId");
				
				
				//����ĳһ���Ƿ���һ�������������ݣ���һ�������쳣���ݵ����
				boolean tag=false;
				
				for(int i=0;i<dataLength;i++){
					float tmpdata=fromDataFile.get(i);
					if(tmpdata>=min&&tmpdata<=max){
						//������������
						res1.append(String.valueOf(tmpdata)+",");
					}else{
						//�����쳣����
						tag=true;
						res2.append(String.valueOf(tmpdata)+",");
					}
				}
				
				//���������
				outPath.write("normal", new Text(res1.toString().substring(0,res1.length()-1)), null,"/k2data/res-output/normal/");
				normalCounter.increment(1);
				if(tag){
					//����쳣��
					outPath.write("abnormal", new Text(res2.toString().substring(0,res2.length()-1)), null,"/k2data/res-output/abnormal/");
					abnormalCounter.increment(1);
				}
			}else{
                //����range-input.csvû��ָ���е�һ�����ݣ����������ݶ����쳣����
				StringBuilder res3=new StringBuilder("deviceId");
				for(int i=0;i<dataLength;i++){
					float tmpdata=fromDataFile.get(i);
						res3.append(String.valueOf(tmpdata)+",");
					}	
				//�쳣����·��
				outPath.write("abnormal", new Text(res3.toString().substring(0,res3.length()-1)), null,"/k2data/res-output/abnormal/");
				abnormalCounter.increment(1);
				
			}
			
		}
		protected void cleanup(Context context) throws IOException, InterruptedException {
			   outPath.close();
			 }
		
	}
	
		
	
}
