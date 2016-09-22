package org.forwoods.docuwiki.documentationWiki.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class XMLAmpFixerInputStream extends InputStream {
	
	private PushbackInputStream in;

	//whenever this stream encounters an & it reads on to find out if it is valid XML
	//if it is it pushes back the read ahead
	//else it pushes back what it read and appends (&)amp;
	
	public XMLAmpFixerInputStream(InputStream in) {
		this.in = new PushbackInputStream(in, 110);
	}
	

	private void readAhead() throws IOException {
		byte[] buffer = new byte[100];
		int bufferpos=0;
		//read to the next character that isn't valid in an entity name
		boolean certain = false;
		boolean valid=false;
		while (!certain) {
			int i=in.read(); 
			buffer[bufferpos++] = (byte)i;
			if (i==-1) {
				//unexpected end of file - this really isn't valid xml
				certain = true;
				valid = false;
			}
			else if (i==';') {
				//congratulations - this is formatted as a valid entity
				certain = true;
				valid=true;
			}
			else if (Character.isLetter(i)) {
				//this isn't a valid entity
				certain = true;
				valid = false;
			}
			else if (bufferpos >= buffer.length) {
				//we have run out of room in our buffer
				//assume this isn't a valid entity
				certain = true;
				valid = false;
			}
			//else this is a char that could be in an entity name
		}
		
		//now we need to push back all that we read
		in.unread(buffer,0,bufferpos);
		if (!valid) {
			//the naked & in this stream needs to be replaced with &amp;
			byte[] amp = "amp;".getBytes();
			in.unread(amp);
		}
	}
	
	@Override
	public int read() throws IOException {
		int b = in.read();
		if (b=='&') {
			readAhead();
		}
		return b;
	}


	@Override
	public void close() throws IOException {
		in.close();
	}
	
	

}
