package smallcampus.QuickPointer.net.TCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import smallcampus.QuickPointer.net.BaseClient;
import smallcampus.QuickPointer.net.Protocol;
import smallcampus.QuickPointer.net.Protocol.Status;

public final class QPTcpUdpClient extends BaseClient {
    private static QPTcpUdpClient instance; 
    
    private Socket tcpSocket;
    private PrintWriter out;
    private BufferedReader in;
    
	private DatagramSocket udpSocket;

	private String hostname;
	private InetAddress host;
    private int tcpPort, udpPort;

    private Thread tcpReceiveThread;
    
	public QPTcpUdpClient(String hostname, int tcpPort, int udpPort) throws SocketException{
		instance = this;
		
		this.hostname = hostname;
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		
    	tcpSocket = new Socket();
    	udpSocket = new DatagramSocket();
    	
	}
	
	
	@Override
	public synchronized void connect(){
		new Thread(clientConnect).start();
	}

	@Override
	public void disconnect() {
		isConnected = false;
		new Thread(clientStop).start();
	}

	@Override
	public synchronized void sendCoordinateData(float x, float y) {
		if(isConnected()){
			//TODO data lost
			new Thread(new UdpSend(Protocol.compileCoordinateMsg(x, y))).start();
		}
	}

	@Override
	public synchronized void sendStartControl() {
		if(isConnected()){
			new Thread(new TcpSend(Protocol.getStatusString(Status.START))).start();
		}
	}

	@Override
	public synchronized void sendStopControl() {
		if(isConnected()){
			new Thread(new TcpSend(Protocol.getStatusString(Status.STOP))).start();
		}
	}
	
	private final class UdpSend implements Runnable{
		DatagramPacket p;
		UdpSend(String msg){
			System.out.println("UDPSend preparing msg:"+msg);
			p = UDPProtocol.compilePacket(msg);
		}
		@Override
		public void run() {
			try {
				udpSocket.send(p);
				System.out.println("UDP send END:" + System.currentTimeMillis());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	private final class TcpSend implements Runnable{
		String msg;
		TcpSend(String msg){
			this.msg = msg;
		}
		@Override
		public void run() {
			out.println(msg);
		}	
	}
	
	private final Runnable tcpReceive = new Runnable(){
		@Override
		public void run() {
			String fromServer;
			while(isConnected()){
				try {
					fromServer = in.readLine();
					
					//TODO protocol implementation should be here
					
					if(onControlReceive!=null){
						onControlReceive.perform(fromServer);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	};

	private final Runnable clientConnect = new Runnable(){
		@Override
		public void run() {

			try {
				host = InetAddress.getByName(hostname);
				
				tcpSocket.connect(new InetSocketAddress(host,tcpPort), 5000);
				
				
				isConnected = true;
				
				out = new PrintWriter(tcpSocket.getOutputStream(), true);
				in = new BufferedReader(
		            new InputStreamReader(tcpSocket.getInputStream()));
		        
				tcpReceiveThread = new Thread(tcpReceive);
				tcpReceiveThread.start();
				
			} catch (Exception e) {
				isConnected=false;
				if(onServerConnectFailure!=null){
					onServerConnectFailure.perform(null);
				}
				return;
			}
			udpSocket.connect(host, udpPort);
			
			
			if(onServerConnected!=null){
				onServerConnected.perform(null);
			}
		}
	};
	
	private final Runnable clientStop = new Runnable(){
		@Override
		public void run() {
			try {
				instance.tcpSocket.shutdownOutput();
				instance.tcpSocket.shutdownInput();
				instance.tcpSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			instance.udpSocket.close();
		}	
	};

	@Override
	public void sendPageUpControl() {
		if(isConnected()){
			new Thread(new TcpSend(Protocol.getStatusString(Status.PAGEUP))).start();
		}
	}


	@Override
	public void sendPageDownControl() {
		if(isConnected()){
			new Thread(new TcpSend(Protocol.getStatusString(Status.PAGEDOWN))).start();
		}
	}
}
