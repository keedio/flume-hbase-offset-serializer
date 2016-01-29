package org.keedio.flume.sink.hbase.utils;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

public class OffsetSerializerHelper {
	
	
	// TODO: Check number format exceptions
	// TODO: Check correctly formed property
	// TODO: Throws Exceptions
	public static List<Pair<Integer,Integer>> getOffsets(String line){
		
		List<Pair<Integer,Integer>> outputList = new ArrayList<Pair<Integer,Integer>>();
		Pair<Integer,Integer> pair = new Pair<Integer, Integer>(0, 0);
		
		String[] offsets = line.split(",");
		String[] auxOffset;
		
		for (int i=0; i < offsets.length; i++){
			auxOffset = offsets[i].split(":");
			pair = pair.setAt0(Integer.parseInt(auxOffset[0]));
			pair = pair.setAt1(Integer.parseInt(auxOffset[1]));
			outputList.add(pair);
		}
		
		return outputList;
	}
	
	// TODO: Check correctly formed property
	// TODO: Throws Exceptions
	public static List<String> getColumns(String line){
		
		List<String> outputList = new ArrayList<String>();
				
		String[] columns = line.split(",");
		
		for (int i=0; i < columns.length; i++){
			outputList.add(columns[i]);
		}
		
		return outputList;
	}
	
	public static List<byte[]> getSplittedEvent(byte[] input, List<Pair<Integer,Integer>> offsets){
		
		byte[] auxByteArray = null;
		List<byte[]> outputList = new ArrayList<byte[]>(offsets.size());
		int byteArraySize = 0;
		int offset = 0;
		
		for (int i=0; i<offsets.size(); i++){
			
			offset = offsets.get(i).getValue0();
			byteArraySize = offsets.get(i).getValue1();
			
			auxByteArray = new byte[byteArraySize];
			
			System.arraycopy(input, offset, auxByteArray, 0, byteArraySize);
			outputList.add(auxByteArray);
		}
		
		return outputList;
	}
}
