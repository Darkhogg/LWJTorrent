package es.darkhogg.bencode;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

public final class BencodeOutputStream implements Closeable {
	
	private final OutputStream stream;
	private final PrintWriter printer;
	
	public BencodeOutputStream ( OutputStream in ) {
		stream = in;
		printer = new PrintWriter( new OutputStreamWriter( in ) );
	}
	
	public BencodeOutputStream ( File file )
	throws FileNotFoundException {
		this( new FileOutputStream( file ) );
	}
	
	public void writeValue ( Value<?> value )
	throws IOException {
		if ( value instanceof StringValue ) {
			StringValue sv = (StringValue) value;
			printer.print( sv.getValue().length );
			printer.print( ':' );
			printer.flush();
			stream.write( sv.getValue() );
			
		} else if ( value instanceof IntegerValue ) {
			IntegerValue iv = (IntegerValue) value;
			printer.print( 'i' );
			printer.print( iv.getValue() );
			printer.print( 'e' );
			printer.flush();
			
		} else if ( value instanceof ListValue ) {
			ListValue lv = (ListValue) value;
			printer.print( 'l' );
			for ( Value<?> val : lv.getValue() ) {
				writeValue( val );
			}
			printer.print( 'e' );
			printer.flush();
			
		} else if ( value instanceof DictionaryValue ) {
			DictionaryValue dv = (DictionaryValue) value;
			printer.print( 'd' );
			for ( Map.Entry<String,Value<?>> me : dv.getValue().entrySet() ) {
				writeValue( new StringValue( me.getKey() ) );
				writeValue( me.getValue() );
			}
			printer.print( 'e' );
			printer.flush();
			
		} else {
			throw new IllegalArgumentException( "Invalid value type" );
		}
	}
	
	@Override 
	public void close ()
	throws IOException {
		stream.close();
		printer.close();
	}
}
