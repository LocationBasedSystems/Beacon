package serv;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Client extends Application implements Runnable {
	
	 private Socket socket;
	 private PrintWriter zumServer;
	 private BufferedReader vomServer;
	 
	 TextArea tein;
	 TextArea taus;
	 
	 boolean gettingAdmin = false;
	boolean expectingRawData = false;
	private int rawDataSize = 0;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		BorderPane bp = new BorderPane();
		VBox l = new VBox();
		Scene sc = new Scene(bp);
		primaryStage.setScene(sc);
		primaryStage.setTitle("Test Client");
		
		tein = new TextArea();
		taus = new TextArea();
		
		l.getChildren().addAll(taus,tein);
		
		Button b = new Button("senden");
		
		bp.setBottom(b);
		bp.setCenter(l);
		
		b.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				if(tein.getText().startsWith(PROTOKOLL.cs_dataBegin)) {
					File f = new File("C:/Users/juliu/workspace/PictureToCSV/ultraschall.png");
					DataInputStream in = dataManager.readFile("C:/Users/juliu/workspace/PictureToCSV/ultraschall.png");
					String challengeString = "db:" + f.length() + ":ultraschall.png";
					byte[] chBytes = challengeString.getBytes();
					byte[] signature = null;
					System.out.println("CH:"+challengeString);
					try {
						FileInputStream keyfis = new FileInputStream("privkey");
						byte[] encKey = new byte[keyfis.available()];  
						keyfis.read(encKey);

						keyfis.close();
						
						PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
						Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
						KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
						PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);
						
						dsa.initSign(privKey);
						dsa.update(chBytes);
						signature = dsa.sign();
						
					} catch (NoSuchAlgorithmException | NoSuchProviderException | IOException | InvalidKeySpecException | InvalidKeyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SignatureException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					send(challengeString + ":" + signature.length);
					try {
						socket.getOutputStream().write(signature);
				        socket.getOutputStream().flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					byte[] arr = new byte[64*1024];
					try {
					    int len = 0;
					    while((len = in.read(arr)) != -1)
					    {
					        socket.getOutputStream().write(arr);
					        socket.getOutputStream().flush();
					    } 
					} catch(Exception ex) {
					    ex.printStackTrace();
					}
					
					send(PROTOKOLL.cs_dataEnd);
				}
				else {
					send(tein.getText());
				}
				tein.clear();
			}
		});
		
		primaryStage.show();
		
		Thread t = new Thread(this);
		t.start();
		

	}
	
	public Client() {
        this("localhost", 4444);
    }

    public Client(String serverIP, int serverPort) {
        
        try {
            InetAddress addr = InetAddress.getByName(serverIP);
            System.out.println("(Client Main) HostName: " + addr.getHostName());
            System.out.println("(Client Main) HostAddr: " + addr.getHostAddress());
            socket = new Socket(serverIP, serverPort);
            this.zumServer = new PrintWriter(this.socket.getOutputStream(), true);
            this.vomServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.out.println("(Client Main) unknown Host");
        } catch (IOException io) {
            System.out.println("(Client Main)   Socket-Fehler io");
            System.out.println("(Client Main) " + io.getMessage());
        }

        if (this.socket != null) {
            try {
                String empfang = vomServer.readLine();
                if (empfang != null) {
                    System.out.println("Vom Server sofort erhalten: " + empfang);
                } else {
                    System.out.println(" Nachricht vom Server war leer!");
                }
            } catch (IOException io) {
                System.out.println("(ClientLauscher) " + io.getMessage());
            }
        }
    }

    /**
     * Beendet die Verbindung zum Server.
     */
    public void beendeVerbindung() {
        try {
            socket.close();
        } catch (IOException io) {
            System.out.println("(Client Main)   Socket-Fehler io");
            System.out.println("(Client Main) " + io.getMessage());
        }
        System.out.println("(Client Main) Ich bin am Ende!");
    }
    
    public void send(String s){
        if(socket == null || !this.socket.isConnected()) {
            System.out.println("Keine Verbindung!");
        }
        else{
            zumServer.println(s);
        }
        System.out.println("Gesendet: "+s);
    }
    
    /**
     * Liest eine Nachricht vom Server und gibt sie zurück.
     */
    public String read(){
        try {
        	if(expectingRawData) {
        		if(gettingAdmin) {
        			byte[] tmp = new byte[rawDataSize];
        			socket.getInputStream().read(tmp);
        			System.out.println("Der recKey ist lang: "+tmp.length);
                	FileOutputStream fo = new FileOutputStream("privkey");
                	fo.write(tmp);
                	gettingAdmin = false;
                	expectingRawData = false;
                	return "Raw data";
                }
        	}
        	else {
        		String s = vomServer.readLine();
                if(s.startsWith(PROTOKOLL.sc_sendingKey)) {
                	String [] dates = s.split(":", 3);
                	rawDataSize = Integer.parseInt(dates[1]);
                	gettingAdmin=true;
                	expectingRawData = true;
                }
                
                System.out.println("Empfangen: "+s);
                return s;
        	}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void run() {
		String s;
				do{
					s = read();
					taus.appendText(s+"\n");
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}while(!s.startsWith(PROTOKOLL.sc_stop));
		
	}

}
