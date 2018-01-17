package serv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class authenticationManager {

	public static byte[] addAuthUser() {
		try {
		File inKey = new File("pubkey");
		if(!inKey.exists()) {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			keyGen.initialize(1024, random);
			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey priv = pair.getPrivate();
			PublicKey pub = pair.getPublic();
			System.out.println("Alg of priv :" + priv.getAlgorithm());
			byte[] key = pub.getEncoded();
			byte[] prik = priv.getEncoded();
			System.out.println("Der pubKey ist lang: "+key.length);
			System.out.println("Der privKey ist lang: "+prik.length);
			FileOutputStream keyfos = new FileOutputStream("pubkey");
			keyfos.write(key);
			keyfos.close();
			return prik;
		}
		else {
			return null;
		}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean isAuthenticated(byte[] sign, byte[] data) {
		try {
			FileInputStream keyfis = new FileInputStream("pubkey");
			byte[] encKey = new byte[keyfis.available()];  
			if(encKey.length==0) {
				keyfis.close();
				return false;
			}
			keyfis.read(encKey);

			keyfis.close();
			
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
			PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
			
			Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
			sig.initVerify(pubKey);
			sig.update(data);
			
			return sig.verify(sign);
			
		} catch (NoSuchAlgorithmException | NoSuchProviderException | IOException | 
				InvalidKeySpecException | InvalidKeyException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
