import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedList;
import java.util.List;

public class HeartBeat implements Runnable {

	private ConcurrentHashMap<ServerInfo, Date> serverStatus;
	private int sequence;
	private int port;

	public HeartBeat(ConcurrentHashMap<ServerInfo, Date> serverStatus, int port) {
		this.serverStatus = serverStatus;
		this.port = port;
		sequence = 0;
	}

	@Override
	public void run() {
		while(true) {
			try {
				LinkedList<Thread> threads = new LinkedList<>();
				HashMap<ServerInfo, Date> servers = new HashMap<>(serverStatus);
					for(ServerInfo si : servers.keySet()) {
						threads.add(new Thread(new Runnable() {
							@Override
							public void run() {
								try {
						            // create socket with a timeout of 2 seconds
						            Socket toServer = new Socket();
						            toServer.connect(new InetSocketAddress(si.getHost(), si.getPort()), 2000);
						            PrintWriter printWriter = new PrintWriter(toServer.getOutputStream(), true);

						            // send the message forward
						            printWriter.println(String.format("hb|%d|%d", port, sequence));
						            printWriter.flush();
						            // close printWriter and socket
						            printWriter.close();
						            toServer.close();
						        } catch (IOException e) {

						        }
							}
						}));
						threads.getLast().start();
					}
					for(Thread t : threads)
						try {
							t.join();
						} catch(InterruptedException e) {
						}

				Thread.sleep(2000);
				++sequence;
			} catch (InterruptedException e) {

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
