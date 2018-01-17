package serv;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *Server uebernimmt annahme von neuen Clients und Kommuniziert mit WPA_Supplicant
 */
public class Server
{
    private static ServerSocket server;
    private static Socket client;
    private static ClientHandler connected;
    private static String currentInterface;
    /**
     * Constructor for objects of class Server
     */
    public Server()
    {
    }
    
    public static void main(String[] args) throws IOException {
        while(true){
            System.out.println("Server started");
            
            Process p = Runtime.getRuntime().exec("sudo wpa_cli");
            PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(p.getOutputStream())));
            
            
       
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null; 
    
            try {
                while (!input.readLine().contains("Interactive mode")){
                }
            } catch (IOException e) {
                    e.printStackTrace();
            }
            
            try{
                Thread.sleep(1000);
            } catch(Exception e){
                e.printStackTrace();
            }
            
            output.println("p2p_find");
            output.flush();
            
            try {
                while (!(line = input.readLine()).contains("P2P-GO-NEG-REQUEST")){
                    System.out.println(line);
                }
                
            } catch (IOException e) {
                    e.printStackTrace();
            }
            System.out.println("starting ..." + line);
            String[] parts = new String[3];
            parts = line.split(" ");
            System.out.println(parts[1]);
            
            try{
                Thread.sleep(1000);
            } catch(Exception e){
                e.printStackTrace();
            }
            
            output.println("p2p_connect " + parts[1] + " pbc go_intent=0");
            output.flush();
            
            try {
                while (!(line = input.readLine()).contains("P2P-GROUP-STARTED")){
                    System.out.println(line);
                }
            } catch (IOException e) {
                    e.printStackTrace();
            }
            String [] dates = line.split(" ", 3);
            currentInterface = dates[1];
            System.out.println(currentInterface);
            
            System.out.println("real group started");
            Server s = new Server();
            
            try {
                server = new ServerSocket(2901);
                client = server.accept();
                connected = new ClientHandler(client);
                Thread t = new Thread(connected);
                t.start();
                t.join();
                System.out.println("Dies sollte erst nach einem Disconnect erscheinen");
                output.println("p2p_group_remove " + currentInterface);
                System.out.println("p2p_group_remove " + currentInterface);
                output.flush();
                output.println("quit");
                output.flush();
                output.close();
                input.close();
                server.close();
                client.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
