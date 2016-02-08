# flume-hbase-offset-serializer
Flume Hbase Serializer to split event body into chunks to send to HBase columns

Compilation and packaging
----------
```
  $ mvn package
```

Deployment
----------

Copy csvSerializer-<version>.jar in target folder into flume plugins dir folder
```
  $ mkdir -p $FLUME_HOME/plugins.d/hbase-csv-serializer/lib
  $ cp csvSerializer-<version>.jar $FLUME_HOME/plugins.d/hbase-csv-serializer/lib
```

Configuration of Hbase CSV Serializer:
----------
Mandatory properties in <b>bold</b>

| Property Name | Default | Description |
| ----------------------- | :-----: | :---------- |
| <b>offsets</b> | - | BeginningOffset:ChunkSize coma separadet |
| <b>columns</b> | - | List of HBase Table-ColumnFamily to spread the chunks |


Configuration example
--------------------

```properties
agent.sinks.hbase.serializer.offsets=0:26,27:26,54:24,79:1,81:15,97:20,118:8,127:51,188:32,212:57,270:16
agent.sinks.hbase.serializer.columns=TIMESTAMPIN,TIMESTAMPOUT,IDPETITION,ERROR,IDSERVER,IDTHREAD,CANCODE,ESCENARIO,IDSESION,IDUSUARIO,IPCLIENTE

```
