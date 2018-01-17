package serv;

/**	
* Dieses Protokoll dient als Definition aller Validen Nachrichten zwischen Server und Client		
*/
public class PROTOKOLL {

	//Server -> Client
		public static final String sc_welcome = "w:";
		public static final String sc_ok = "ok:";
		
		public static final String sc_warte = "warte:";			//
		public static final String sc_error = "e:";		
		public static final String sc_stop = "stop:";
		public static final String sc_dataBegin = "db:";			//length, key, signLength
		public static final String sc_sendingKey = "sk:";			//length
		public static final String sc_dataEnd = "de:";
		public static final String sc_sendFileList = "fl:";
		
		//Client -> Server
		
		public static final String cs_closeConnection = "cc:";			//
		
		public static final String cs_addAdmin = "aa:";
		public static final String cs_dataBegin = "db:";			//length, key, signedHash
		public static final String cs_dataRequest = "dr:";			//key
		public static final String cs_configuration = "c:";			//
		public static final String cs_dataEnd = "de:";
		public static final String cs_listFiles = "ls:";
		
}
