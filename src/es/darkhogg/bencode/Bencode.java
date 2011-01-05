package es.darkhogg.bencode;

import java.io.File;
import java.nio.charset.Charset;

public class Bencode {
	
	public static final Charset UTF8 = Charset.forName( "UTF-8" );
	
	public static void main ( String[] args )
	throws Exception {
		BencodeInputStream bis = new BencodeInputStream(
			new File( "D:\\bencodeTest.txt" ) );
		
		Value<?> val = bis.readValue();
		System.out.println( val );
		
		BencodeOutputStream bos = new BencodeOutputStream(
			new File( "D:\\bencodeTest2.txt" ) );
		
		bos.writeValue( val );
		
		bis.close();
		bos.close();
	}
	
}
