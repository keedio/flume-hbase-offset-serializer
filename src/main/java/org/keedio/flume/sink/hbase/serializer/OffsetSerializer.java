/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.keedio.flume.sink.hbase.serializer;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.conf.ComponentConfiguration;
import org.apache.flume.sink.hbase.HbaseEventSerializer;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.javatuples.Pair;
import org.keedio.flume.sink.hbase.utils.OffsetSerializerHelper;
import org.keedio.flume.sink.hbase.utils.KeyType;
import org.keedio.flume.sink.hbase.utils.RowKeyGenerator;

import com.google.common.base.Charsets;

/**
 * A simple serializer that returns puts from an event, by writing the event
 * body into it. The headers are discarded. It also updates a row in hbase
 * which acts as an event counter.
 *
 * Takes optional parameters:<p>
 * <tt>rowPrefix:</tt> The prefix to be used. Default: <i>default</i><p>
 * <tt>incrementRow</tt> The row to increment. Default: <i>incRow</i><p>
 * <tt>suffix:</tt> <i>uuid/random/timestamp.</i>Default: <i>uuid</i><p>
 *
 * Mandatory parameters: <p>
 * <tt>cf:</tt>Column family.<p>
 * Components that have no defaults and will not be used if null:
 * <tt>payloadColumn:</tt> Which column to put payload in. If it is null,
 * event data will not be written.<p>
 * <tt>incColumn:</tt> Which column to increment. Null means no column is
 * incremented.
 */
public class OffsetSerializer implements HbaseEventSerializer {
	
  private byte[] incrementRowKey;
  private byte[] cf;
 
  /*
  private byte[] timestampEntrada, timestampSalida, idPeticion, huboError,
  idServidor, idThread, codCanal, escenario, idSesion, idUsuario, ipCliente;
  
  
  private static FixedFormatManager manager = new FixedFormatManagerImpl();
  private EntryLogRecord logEntradaRecord;
  */
  
  private byte[] incCol;
  private KeyType keyType;
  
  private List<byte[]> eventSplitted;
  private List<String> columnNames;
  private List<Pair<Integer,Integer>> offsets;

  public OffsetSerializer(){

  }

  @Override
  public void configure(Context context) {
	  
    incrementRowKey = context.getString("incrementRow", "incRow").getBytes(Charsets.UTF_8);
    offsets = OffsetSerializerHelper.getOffsets(context.getString("offsets"));
    columnNames = OffsetSerializerHelper.getColumns(context.getString("columns"));
    String incrementColumnName = context.getString("incrementColumn","iCol");
    
    String rowKeyType = context.getString("rowKeyType", "uuid");
        
    if(rowKeyType.equals("timestamp")){
      keyType = KeyType.TS;
    } else if (rowKeyType.equals("random")) {
      keyType = KeyType.RANDOM;
    } else if(rowKeyType.equals("nano")){
      keyType = KeyType.TSNANO;
    } else {
      keyType = KeyType.UUID;
    }

    if(incrementColumnName != null && !incrementColumnName.isEmpty()) {
      incCol = incrementColumnName.getBytes(Charsets.UTF_8);
    }
  }

  @Override
  public void configure(ComponentConfiguration conf) {
  }

  @Override
  public void initialize(Event event, byte[] cf) {
	
	this.eventSplitted = OffsetSerializerHelper.getSplittedEvent(event.getBody(), offsets);
	this.cf = cf;
  }

  @Override
  public List<Row> getActions() throws FlumeException {
    List<Row> actions = new LinkedList<Row>();
    byte[] rowKey;
    try {
      rowKey = RowKeyGenerator.generateRowKey(keyType);
      
	  Put put = new Put(rowKey);
		
	  for (int i=0; i<columnNames.size();i++){
	    put.addColumn(cf, columnNames.get(i).getBytes(Charsets.UTF_8), eventSplitted.get(i));
	  }
	  actions.add(put);
	} catch (UnsupportedEncodingException e) {
	  throw new FlumeException("Could not get row key!", e);
	}
	return actions;
  }
  
  @Override
  public List<Increment> getIncrements(){
    List<Increment> increments = new LinkedList<Increment>();
    if(incCol != null) {
      Increment inc = new Increment(incrementRowKey);
      inc.addColumn(cf, incCol, 1);
      increments.add(inc);
    }
    return increments;
  }

  @Override
  public void close() {
  }

}