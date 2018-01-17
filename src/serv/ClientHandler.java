package serv;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;


/**
 * Hauptklasse zur Verarbeitung von Ein und Ausgaben einzelner Clients
 */
public class ClientHandler implements Runnable{
	
	private Socket client;
	private PrintWriter zumClient;
	private BufferedReader vomClient;
	
	private byte[] adminKey;
	/** Erzeugt ein neues Objekt der Klasse ClientHandler mit den uebergebenen Parametern
	 * @param p
	 * @param client
	 */
	public ClientHandler(Socket client) {
		super();
		this.client = client;		
	}

	public void run() {
			
		try {
			zumClient = new PrintWriter(client.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			vomClient = new BufferedReader(new InputStreamReader(client
					.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		kommunication();
	
	}
	
	
	private void kommunication() {

		String ein;
		send("Connection to Beacon established");
		try {
			do {
				ein = vomClient.readLine();
				if (ein != null) {
					System.out.println("Vom Client empfangen: " + ein);
				}
				if (ein == null) {
					System.out.println(" Vom Client empfangen: NULL!");
					send(PROTOKOLL.sc_error + "NULL gelesen");
				}
				else if(ein.startsWith(PROTOKOLL.cs_addAdmin)) {
					byte[] result = authenticationManager.addAuthUser();
					if(result != null) {
						send(PROTOKOLL.sc_sendingKey + result.length);
						client.getOutputStream().write(result);
						client.getOutputStream().flush();
						
					}
					else {
						send(PROTOKOLL.sc_error + "could not add Admin");
					}
				}
				else if(ein.startsWith(PROTOKOLL.cs_dataBegin)) {
					System.out.println("Entered data begin block");
					String [] dates = ein.split(":", 4);
					int dataLength = Integer.parseInt(dates[1]);
					int signatureLength = Integer.parseInt(dates[3]);
					int readBytes = 0;
					int remainingBytes = dataLength - readBytes;
					FileOutputStream fo = dataManager.writeFile("beacondata/" + dates[2]);
					
					String challString = dates[0] + ":" + dates[1] + ":" + dates[2];
					byte[] challStringAsByte = challString.getBytes("UTF8");
					byte[] receivedSignature = new byte[signatureLength];
					System.out.println("CH:"+challString);
					client.getInputStream().read(receivedSignature, 0, signatureLength);
					
					if(authenticationManager.isAuthenticated(receivedSignature, challStringAsByte)) {
					byte[] arr = new byte[1024];
					int datgroe = 0;
					try {
					    while(remainingBytes > 0)
					    {
					    	if(remainingBytes < arr.length) {
					    		arr=new byte[remainingBytes];
					    	}
					    	int soViel = client.getInputStream().read(arr);
					    	byte[] outArr = new byte[soViel];
					    	for (int i = 0; i<outArr.length;i++) {
								outArr[i] = arr[i];
							}
					    	
					    	System.out.println("SoViel: " + soViel);
					        readBytes += soViel;
					        
					        datgroe += outArr.length;
						    fo.write(outArr);
					        
					        remainingBytes = dataLength - readBytes;
					        System.out.println("Remaining Bytes: " + remainingBytes);
					        System.out.println("DataLength: " + dataLength);
					        System.out.println("ReadBytes: " + readBytes);
					    } 
					    System.out.println("datgroe: " + datgroe);
					    fo.close();
					    
					    //clearing overlying stream
					    String tmpTrsh = vomClient.readLine();
					    while(!tmpTrsh.startsWith(PROTOKOLL.cs_dataEnd)) {
					    	tmpTrsh = vomClient.readLine();
					    }
					    
					    send(PROTOKOLL.sc_ok);
					} catch(Exception ex) {
					    ex.printStackTrace();
					}
					}
					else {
						send(PROTOKOLL.sc_error + "Signatur nicht akzeptiert send de: to continue");
						//clearing overlying stream
					    String tmpTrsh = vomClient.readLine();
					    while(!tmpTrsh.startsWith(PROTOKOLL.cs_dataEnd)) {
					    	tmpTrsh = vomClient.readLine();
					    }
					}
				}
				else if(ein.startsWith(PROTOKOLL.cs_dataEnd)) {
					send(PROTOKOLL.sc_ok);
				}
				else if(ein.startsWith(PROTOKOLL.cs_dataRequest)) {
					String [] dates = ein.split(":", 2);
					DataInputStream in = dataManager.readFile("beacondata/" + dates[1]);
					File f = new File("beacondata/" + dates[1]);
					send(PROTOKOLL.sc_dataBegin + f.length() +":" + dates[1]);
					byte[] arr = new byte[1024];
					try {
					    int len = 0;
					    while((len = in.read(arr)) != -1)
					    {
					        client.getOutputStream().write(arr);
					        client.getOutputStream().flush();
					    } 
					} catch(Exception ex) {
					    ex.printStackTrace();
					}
					in.close();
					send("n");
					send(PROTOKOLL.sc_dataEnd);
				}
				else if (ein.startsWith(PROTOKOLL.cs_listFiles)) {
					File dir = new File("beaconData");
					dir.mkdir();
					String[] out = dir.list();
					String resp = "[";
					for(String string : out) {
						resp = resp + string + ";";
					}
					resp = PROTOKOLL.sc_sendFileList + resp + "]";
					System.out.println(resp);
					send(resp);
				}
				else if (ein.startsWith(PROTOKOLL.cs_closeConnection)) {
					send(PROTOKOLL.sc_stop);
				}
				 else {
					send(PROTOKOLL.sc_error + ein);
				}
			} while (ein != null && !ein.startsWith(PROTOKOLL.cs_closeConnection));
			
			System.out.println("Connection closed");
			client.close();
		} catch (IOException io) {
			System.out.println("IO-Fehler");
			System.out.println(io.getMessage());
		}
	}

	public void send(String s){
        if(client == null || !this.client.isConnected()) {
            System.out.println("Keine Verbindung!");
        }
        else{
            zumClient.println(s);
        }
        System.out.println("Gesendet: "+s);
    }

	/** Gibt den Wert von vomClient zurueck
	 * @return Der Wert von vomClient
	 */
	public BufferedReader getVomClient() {
		return vomClient;
	}
	
	
	
}
