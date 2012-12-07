/*
  DBFReader
  Class for reading the records assuming that the given
	InputStream comtains DBF data.

  This file is part of JavaDBF packege.

  Author: anil@linuxense.com
  License: LGPL (http://www.gnu.org/copyleft/lesser.html)

  $Id: DBFReader.java,v 1.8 2004/03/31 10:54:03 anil Exp $
*/

package javadbf;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import org.joda.time.DateTime;

/**
	DBFReader class can creates objects to represent DBF data.

	This Class is used to read data from a DBF file. Meta data and
	records can be queried against this document.

	<p>
	DBFReader cannot write anythng to a DBF file. For creating DBF files 
	use DBFWriter.

	<p>
	Fetching rocord is possible only in the forward direction and 
	cannot re-wound. In such situation, a suggested approach is to reconstruct the object.

	<p>
	The nextRecord() method returns an array of Objects and the types of these
	Object are as follows:

	<table>
	<tr>
		<th>xBase Type</th><th>Java Type</th>
	</tr>

	<tr>
		<td>C</td><td>String</td>
	</tr>
	<tr>
		<td>N</td><td>Integer</td>
	</tr>
	<tr>
		<td>F</td><td>Double</td>
	</tr>
	<tr>
		<td>L</td><td>Boolean</td>
	</tr>
	<tr>
		<td>D</td><td>java.util.Date</td>
	</tr>
	</table>
	
*/
public class DBFReader extends DBFBase {

	DataInputStream dataInputStream;
	DBFHeader header;

	/* Class specific variables */
	boolean isClosed = true;

	/**
		Initializes a DBFReader object.

		When this constructor returns the object 
		will have completed reading the hader (meta date) and 
		header information can be quried there on. And it will 
		be ready to return the first row.

		@param InputStream where the data is read from.	
	*/
	public DBFReader( InputStream in) throws DBFException {

		try {

			this.dataInputStream = new DataInputStream( in);
			this.isClosed = false;
			this.header = new DBFHeader();
			this.header.read( this.dataInputStream);

			/* it might be required to leap to the start of records at times */
			int t_dataStartIndex = this.header.headerLength - ( 32 + (32*this.header.fieldArray.length)) - 1;
			if( t_dataStartIndex > 0) {

				dataInputStream.skip( t_dataStartIndex);
			}
		}
		catch( IOException e) {

			throw new DBFException( e.getMessage());	
		}
	}


	public String toString() {

		StringBuffer sb = new StringBuffer(  this.header.year + "/" + this.header.month + "/" + this.header.day + "\n"
		+ "Total records: " + this.header.numberOfRecords + 
		"\nHEader length: " + this.header.headerLength +
		"");

		for( int i=0; i<this.header.fieldArray.length; i++) {

			sb.append( this.header.fieldArray[i].getName());
			sb.append( "\n");
		}

		return sb.toString();
	}

	/**
		Returns the number of records in the DBF.
	*/
	public int getRecordCount() {

		return this.header.numberOfRecords;
	}

	/**
		Returns the asked Field. In case of an invalid index,
		it returns a ArrayIndexOutofboundsException.

		@param index. Index of the field. Index of the first field is zero.
	*/
	public DBFField getField( int index) 
	throws DBFException {

		if( isClosed) {

			throw new DBFException( "Source is not open");
		}

		return this.header.fieldArray[ index];
	}

	/**
		Returns the number of field in the DBF.
	*/
	public int getFieldCount() 
	throws DBFException {

		if( isClosed) {

			throw new DBFException( "Source is not open");
		}

		if( this.header.fieldArray != null) {

			return this.header.fieldArray.length;
		}

		return -1;
	}

	/**
		Reads the returns the next row in the DBF stream.
		@returns The next row as an Object array. Types of the elements 
		these arrays follow the convention mentioned in the class description.
	*/
	public Object[] nextRecord() 
	throws DBFException {

		if( isClosed) {

			throw new DBFException( "Source is not open");
		}

		Object recordObjects[] = new Object[ this.header.fieldArray.length];

		try {

			boolean isDeleted = false;
			do {
				
				if( isDeleted) {
	
					dataInputStream.skip( this.header.recordLength-1);
				}
				int t_byte;
				try{
					t_byte = dataInputStream.readByte();
				}catch(Exception e){
					t_byte = END_OF_DATA;
				}
				if( t_byte == END_OF_DATA) {

					return null;
				}

				isDeleted = (  t_byte == '*');
			} while( isDeleted);
	
			for( int i=0; i<this.header.fieldArray.length; i++) {
				
				switch( this.header.fieldArray[i].getDataType()) {
	
					case 'C':
	
						byte b_array[] = new byte[ this.header.fieldArray[i].getFieldLength()];
						dataInputStream.read( b_array);
						recordObjects[i] = new String( b_array, characterSetName).trim();
						break;
	
					case 'D':
	
						byte t_byte_year[] = new byte[ 4];
						dataInputStream.read( t_byte_year);
	
						byte t_byte_month[] = new byte[ 2];
						dataInputStream.read( t_byte_month);
	
						byte t_byte_day[] = new byte[ 2];
						dataInputStream.read( t_byte_day);
	
						try {                                                                                                           
                                                        
                                                     DateTime dt = new DateTime(Integer.parseInt(new String( t_byte_year)),
                                                                                Integer.parseInt(new String( t_byte_month)),
                                                                                Integer.parseInt(new String( t_byte_day)),0,0); 
                                                        
    						     /*
                                                       GregorianCalendar calendar = new GregorianCalendar( 
					                     Integer.parseInt( new String( t_byte_year)),
						             Integer.parseInt( new String( t_byte_day)), 
						             Integer.parseInt( new String( t_byte_month)) 
						       );
	
						       recordObjects[i] = calendar.getTime();
                                                     */
                                                     
                                                        recordObjects[i] = dt.toDate();
						}
						catch ( NumberFormatException e) {
							/* this field may be empty or may have improper value set */
							recordObjects[i] = null;
						}

						break;
	
						
					case 'T':
						try {
						/*
						 * 8 bytes - two ints, first for date, second for time.  
						 * The date is the number of days since  01/01/4713 BC. 
						 * Time is hours * 3600000L + minutes * 60000L + Seconds * 1000L
						 */
						byte t_byte_date[] = new byte[ 4];
						byte t_byte_date_shift[] = new byte[ 4];
						dataInputStream.read( t_byte_date);
						byte t_byte_time[] = new byte[ 4];
						byte t_byte_time_shift[] = new byte[ 4];
						dataInputStream.read( t_byte_time);
						
//						byte t_byte_year1[] = new byte[ 4];
//						dataInputStream.read( t_byte_year1);
	
//						byte t_byte_month1[] = new byte[ 2];
//						dataInputStream.read( t_byte_month1);
	
//						byte t_byte_day1[] = new byte[ 2];
//						dataInputStream.read( t_byte_day1);
//						System.out.println(new String( t_byte_date) + " " +new String( t_byte_time));
						
						for (int j=0 ; j<t_byte_date.length; j++){
							t_byte_date_shift[(t_byte_date.length-1)-j]=t_byte_date[j];
						}
						for (int j=0 ; j<t_byte_time.length; j++){
							t_byte_time_shift[(t_byte_time.length-1)-j]=t_byte_time[j];
						}
						
//						ByteArrayInputStream bis = new ByteArrayInputStream(t_byte_date_shift); 
//						DataInputStream dis = new DataInputStream(bis); 
//						double fechaInt = dis.readDouble();
//						System.out.println("double "+ fechaInt );
////						Date fecha = new Date(fechaInt);
//						GregorianCalendar calendario = new GregorianCalendar(-4713, 0, 1);
//						System.out.println(calendario.getTime());
////						calendario.add(GregorianCalendar.DAY_OF_MONTH, fechaInt/24);
//						System.out.println("sumado "+calendario.getTime());
						
						
						t_byte_date = Utils.trimLeftSpaces( t_byte_date);
//						System.out.println("new String( t_byte_date) "+new String( t_byte_date));
//						System.out.println("fecha "+  ByteBuffer.wrap(t_byte_date_shift).getInt());
//						System.out.println("hora "+  ByteBuffer.wrap(t_byte_time_shift).getInt());
						
						int fechaInt = ByteBuffer.wrap(t_byte_date_shift).getInt();
						int horaInt = ByteBuffer.wrap(t_byte_time_shift).getInt();
						
						GregorianCalendar calendario = new GregorianCalendar(-4712, 0, 1,0, 0, 0);
//						System.out.println(calendario.getTime());
						calendario.add(GregorianCalendar.DAY_OF_MONTH, fechaInt);
						if (horaInt > 0){
							calendario.add(GregorianCalendar.HOUR_OF_DAY, horaInt/1000/60/60);
							calendario.add(GregorianCalendar.MINUTE, (horaInt/1000/60)%60);
							calendario.add(GregorianCalendar.SECOND, ((horaInt/1000)%60)%60);
						}
//						System.out.println(this.header.fieldArray[i].getName()+ " "+calendario.getTime());
						
						//System.out.println(this.header.fieldArray[i].getName()+" '"+new String( t_numeric)+"'");
//													if( t_byte_date.length > 0 && !Utils.contains( t_byte_date, (byte)'?')) {
//
//														recordObjects[i] = new Double( new String( t_byte_date));
//													}
//													else {
//
//														recordObjects[i] = null;
//													}
//						System.out.println("intento con double "+recordObjects[i]);
						

//							t_byte_date=Utils.trimLeftSpaces( t_byte_date);
//							if( t_byte_date.length > 0 && !Utils.contains( t_byte_date, (byte)'?')) {
//
//								System.out.println( new Double( new String( t_byte_date)));
//							}
//							t_byte_time=Utils.trimLeftSpaces( t_byte_time);
//							if( t_byte_time.length > 0 && !Utils.contains( t_byte_time, (byte)'?')) {
//
//								System.out.println( new Double( new String( t_byte_time)));
//							}
//	
							if (fechaInt != 0)
								recordObjects[i] = calendario.getTime();
							else
								recordObjects[i] = null;
						}
						catch (Exception e) {
							/* this field may be empty or may have improper value set */
							e.printStackTrace();
							recordObjects[i] = null;
						}

						break;	
						
					case 'F':
	
						try {

							byte t_float[] = new byte[ this.header.fieldArray[i].getFieldLength()];
							dataInputStream.read( t_float);
							t_float = Utils.trimLeftSpaces( t_float);
							if( t_float.length > 0 && !Utils.contains( t_float, (byte)'?')) {

								recordObjects[i] = new Float( new String( t_float));
							}
							else {

								recordObjects[i] = null;
							}
						}
						catch( NumberFormatException e) {

							throw new DBFException( "Failed to parse Float: " + e.getMessage());
						}

						break;
	
					case 'N':
	
						try {

							byte t_numeric[] = new byte[ this.header.fieldArray[i].getFieldLength()];
							dataInputStream.read( t_numeric);
							t_numeric = Utils.trimLeftSpaces( t_numeric);
//System.out.println(this.header.fieldArray[i].getName()+" '"+new String( t_numeric)+"'");
							if( t_numeric.length > 0 && !Utils.contains( t_numeric, (byte)'?')) {

								recordObjects[i] = new Double( new String( t_numeric));
							}
							else {

								recordObjects[i] = null;
							}
						}
						catch( NumberFormatException e) {
//							e.printStackTrace();
							//throw new DBFException( "Failed to parse Number: " + e.getMessage());
							recordObjects[i] = null;
						}

						break;
	
					case 'L':
	
						byte t_logical = dataInputStream.readByte();
//						System.out.println(t_logical);
						if( t_logical == 'Y' || t_logical == 'y' || t_logical == 'T' || t_logical == 't' || t_logical == '1') {
	
							recordObjects[i] = Boolean.TRUE;
						}
						else {
	
							recordObjects[i] = Boolean.FALSE;
						}
						break;
	
					case 'M':
						// TODO Later
						recordObjects[i] = new String( "null");
						break;

					default:
						
						
						recordObjects[i] = new String( "no tiene tipo "+this.header.fieldArray[i].getDataType());
//						para tratar de leer los que 
						
				}
//				System.out.println(this.header.fieldArray[i].getName() +"(" + this.header.fieldArray[i].getFieldLength() + ") : "+recordObjects[i]);
			}
		}
		catch( EOFException e) {
			e.printStackTrace();
			return null;
		}
		catch( IOException e) {
			e.printStackTrace();
			throw new DBFException( e.getMessage());
		}

		return recordObjects;
	}
}
