package serv;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;



public class TestServer {
	private ServerSocket server;
	private Socket client;
	private ClientHandler[] spieler = new ClientHandler[100];
	private int akt = 0;
	
	public static void main (String [] args){
		 TestServer s = new TestServer();
	}
	public TestServer() {
		
		try {
			server = new ServerSocket(2901);
			do{
			client = server.accept();
			spieler[akt] = new ClientHandler(client);
			Thread t = new Thread(spieler[akt]);
			t.start();
			akt++;
			}while(true);
		}

		catch (IOException io) {
			
		}
	}
}
