package memdb;

import memdb.autogen.IndexedDatabase;
import memdb.autogen.InvalidIndex;
import memdb.autogen.QueryIndexResult;
import memdb.autogen.Record;
import memdb.autogen.RecordNotFound;
import memdb.autogen.ScanContinuation;
import memdb.autogen.ServiceException;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/** Implementation of an IndexedDatabase Client. */
public class IndexedDatabaseClient {
	private IndexedDatabase.Client client;
	private TTransport transport;

	/** Creates a new IndexedDatabase client.
	 * @param server Hostname of the IndexedDatabase server.
	 * @param port Port number of the IndexedDatabase server. */
	public IndexedDatabaseClient(String server, short port) {
		client = null;
		transport = null;
		try {
			transport = new TSocket(server, port);
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
			client = new IndexedDatabase.Client(protocol);	
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}

	/** Closes the connection to the server. */
	public void closeConnection() {
		transport.close();
	}

	/** Updates the record. 
	 * @param key The key of the record. 
	 * @param record The actual record. */
	public void setRecord(String key, Record record)
	throws ServiceException, TException, RecordNotFound {
		client.setRecord(key, record);
	}
	
	/** Gets the record with a given key. 
	 * @param key The key of the record.
	 * @return The record. */
	public Record getRecord(String key)
	throws RecordNotFound, ServiceException, TException {
		return client.getRecord(key);
	}

	/** Returns the index records.
	 * @param indexId Identifier of the Index.
	 * @param pageLimit Maximum number of records in the result.
	 * @param scanContinuation Specifies a previous continuation, so that the next
	 * records can queried.
	 * @return QueryIndexResult containing at most pageLimit number of records
	 * starting from the previous scanContinuation satisfying the conditions
	 * specified by the index. 
	 * */
	public QueryIndexResult queryIndex(short indexId, int pageLimit,
			ScanContinuation scanContinuation) throws InvalidIndex, ServiceException,
			TException {
		return client.queryIndex(indexId, pageLimit, scanContinuation);
	}
}