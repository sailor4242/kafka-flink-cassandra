CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE TABLE IF NOT EXISTS test_keyspace.table_ticker
(id uuid, ticker text, time timestamp, price text, volume int, currency text, PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS test_keyspace.table_ohlc1m
(id uuid, ticker text, start_time timestamp, open text, high text, low text, close_time timestamp, volume int, currency text, PRIMARY KEY(id));

DROP TABLE test_keyspace.table_ticker;

DROP TABLE test_keyspace.table_ohlc1m;