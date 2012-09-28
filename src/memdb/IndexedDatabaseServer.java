package memdb;

import java.util.ArrayList;

import memdb.autogen.Index;
import memdb.autogen.IndexedDatabase;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

/** A multi-threaded thrift server for the IndexedDatabase. **/
public class IndexedDatabaseServer implements Runnable {	
  private TServer server;

  /** Constructs a multi-threaded IndexedDatabase server.
   * @param port The port at which the server is to be started.
   * @param index A list of {@code Index}es that the server maintains.
   **/
  public IndexedDatabaseServer(short port, ArrayList<Index> index) {
    IndexedDatabase.Processor<IndexedDatabase.Iface> processor = 
      new IndexedDatabase.Processor<IndexedDatabase.Iface>(
          new IndexedDatabaseImpl(index));
    TServerTransport serverTransport = null;
    try {
      serverTransport = new TServerSocket(port);
    } catch (TTransportException e) {
      e.printStackTrace();
    }
    // Use a multi-threaded server.
    server = new TThreadPoolServer(
        new TThreadPoolServer.Args(serverTransport).processor(processor));
  }

  /** Starts a multi-threaded IndexedDatabase server which maintains no indexes
   * @param port The port at which the server is to be started. */
  public IndexedDatabaseServer(short port) {
    this(port, new ArrayList<Index>());
  }

  /** Starts the server. */
  @Override
  public void run() {
    server.serve();
  }

  /** Stops the server */
  public void stop() {
    server.stop();
  }
}
