package smallcampus.QuickPointer;

import java.awt.EventQueue;
import java.io.IOException;

import smallcampus.QuickPointer.net.BaseServer;
import smallcampus.QuickPointer.net.QPBluetoothServer;
import smallcampus.QuickPointer.net.TCP.QPTcpUdpServer;
import smallcampus.QuickPointer.ui.MainSetupFrame;
import smallcampus.QuickPointer.ui.MainSetupFrame.OnServerSelectListener;
import smallcampus.QuickPointer.ui.PointerPanel;
import smallcampus.QuickPointer.ui.QuickPointerMainFrame;
import smallcampus.QuickPointer.util.EventListener;

public class QuickPointerApp {
        public static void main(String[] args) throws IOException{
        	
        	//Initialize...
        	//final InitializationFrame frame = new InitializationFrame();
    		EventQueue.invokeLater(new Runnable() {
    			public void run() {
    				try {
    					final MainSetupFrame window = new MainSetupFrame();
    					
    		        	//Choose a server type        	
    		        	window.setOnSelectListener(new OnServerSelectListener(){
    						@Override
    						public void serverSelect(int type) {
    							//Setup the server
    							BaseServer server = null;
    							
    				        	switch(type){
    				        	case TYPE_TCP:
    				        		try {
    									server = new QPTcpUdpServer(Config.DEFAULT_TCP_SERVER_PORT,Config.DEFAULT_UDP_SERVER_PORT);
    								} catch (IOException e1) {
    									e1.printStackTrace();
    								}
    				        		break;
    				        	case TYPE_BLUETOOTH:
    				        		server = new QPBluetoothServer();
    				        		break;
    				        	}
    				        	
    				        	if(server==null){
    				        		System.err.println("Cannot initialize the server. Program terminated.");
    				        		System.exit(-1);
    				        	}
    				        	
    				        	//create connection information
    				        	window.showConnectionInfoPanel(server.getHostname());
    				        	
    				        	
    				        	//Create main UI
    				            QuickPointerMainFrame qp = new QuickPointerMainFrame();
    				            final PointerPanel pointer = qp.getPointer();    
    				            //disable the pointer before connection
    				            pointer.setVisible(false);
    				            
    				            server.setOnCoordinateReceiveListener(new EventListener<float[]>(){
    								@Override
    								public void perform(float[] args) {
    									pointer.setPositionR(args[0], args[1]);
    								}
    				            });
    				            
    				        	//Accept connection and show main UI
    				        	server.setOnConnectionReceiveListener(new EventListener(){
    								@Override
    								public void perform(Object args) {
    									window.showSucessfulCountDown();
    									pointer.setVisible(true);
    								}
    				        	});

    				            
    				            System.out.println("Starting Server...");
    				            server.start();
    				            
    						}
    		        	});
    					
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			}
    		});

        }
}