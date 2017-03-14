package com.mzy.k2data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class MyDataType implements Writable{
	
	/*
	 * ʹ��flag���ֲ�ͬ������
	 */
	private String flag=null;
	private String value=null;
	
	public MyDataType(){}
	
	public MyDataType(String flag,String value){
		this.flag=flag;
		this.value=value;
	}
	
	/*
	 * javaԭʼ���͵��������ֽ�����ת��
	 */
	public void write(DataOutput out) throws IOException {
        out.writeUTF(flag);
        out.writeUTF(value);
      }
      
	/*
	 * �������ֽ�����java���͵�ת��
	 */
      public void readFields(DataInput in) throws IOException {
        flag = in.readUTF();
        value = in.readUTF();
      }
      
      public static MyDataType read(DataInput in) throws IOException {
    	  MyDataType w = new MyDataType();
          w.readFields(in);
          return w;
      }
      
      public String getFlag(){
    	  return this.flag;
      }
      
      public void setFlag(String flag){
    	  this.flag=flag;
      }
      public String getValue(){
    	  return value;
      }
      public String toString(){
    	  return flag+":"+value;
      }
}
