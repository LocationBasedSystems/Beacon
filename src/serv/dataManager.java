package serv;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class dataManager {
	
	public static FileOutputStream writeFile(String filePath) {
		FileOutputStream fo = null;
		try {
			 fo = new FileOutputStream(filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fo;
	}
	
	public static DataInputStream readFile(String filePath) {
		DataInputStream in = null;
		try {
			in = new DataInputStream(new FileInputStream(filePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return in;
	}
}
