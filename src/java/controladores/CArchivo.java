
package controladores;

// @author Daniel.Meza

import java.io.Serializable; 
import java.sql.Connection; 
import java.sql.ResultSet; 
import java.sql.ResultSetMetaData; 
import java.sql.Statement; 
import java.util.ArrayList; 
import java.util.List; 
import java.util.Properties; 
import javax.sql.DataSource; 
import javax.annotation.Resource; 
import javax.faces.application.FacesMessage; 
import javax.faces.component.UIComponent; 
import javax.faces.context.FacesContext; 
import modelos.ModeloPrueba; 
import org.apache.commons.io.FilenameUtils; 
import org.apache.myfaces.custom.fileupload.UploadedFile; 

public class CArchivo implements Serializable {         
           
       @Resource(name="jdbc/siipo")
       private DataSource ds;
       private UploadedFile archivo;
       private FacesContext context = FacesContext.getCurrentInstance();
       private Connection conexion;
       private Statement select,insert;
               
       public void subir(){
                                                                            
              String nombreArchivo = FilenameUtils.getName(archivo.getName());
              List<String> datos = obtenerLineasDatos(archivo);
              int reg_resp = obtenerTipoarchivo(nombreArchivo);
              
              try{
                  
                  int l = 1;
                  
                  for( String linea: datos ){ 
                      
                       //System.out.println(l + "Linea : " + linea);
                                                              
                       Properties ptec = new Properties(); 
                       Properties ppd  = new Properties(); 
                           
                       ptec.load( Thread.currentThread().getContextClassLoader().getResourceAsStream("tiposexamenclaves.properties") ); 
                       ppd.load ( Thread.currentThread().getContextClassLoader().getResourceAsStream("posicionesdats.properties")    ); 
                                                                                
                       String claveExamen = linea.substring(0,3);                                                                                                    
                           
                       if( ptec.containsKey(claveExamen) ){                               
                               
                           String ceo = ((String)ptec.get(claveExamen)).trim();                            
                           int posIniApli = Integer.valueOf(((String)ppd.get(ceo)).trim());                                                                                                                     
                           
                           char[] caracteresLinea = linea.toCharArray();   
                           
                           for( int k = posIniApli; k <= (caracteresLinea.length - 1); k++ ){ 
                                   
                                if( caracteresLinea[k] == ' '){ continue; }                                     
                                else{ 
                                        
                                     conexion = ds.getConnection(); 
                                     select = conexion.createStatement(); 
                                         
                                     //System.out.println( " claveExamen " + claveExamen + " reg_resp " + reg_resp );
                                     
                                     String selectCadena = "select * from longitud_campos where clave_instrumento = " + claveExamen  
                                                           + " and reg_resp = " + reg_resp;                                         
                                         
                                     ResultSet rs = select.executeQuery(selectCadena); 
                                     ResultSetMetaData rsmd = rs.getMetaData(); 
                                                                                                                                                                                  
                                     while( rs.next() ){ 
                                             
                                            for( int j = 2; j <= rsmd.getColumnCount() ; j++ ){ 
                                                 //context.addMessage(null,new FacesMessage( l + " " + linea.substring(rs.getInt(j)) )); 
                                                 System.out.println( "En el while " + l + " " + linea.substring(rs.getInt(j)) );
                                            }                                                                                                 
                                                                                                                                                                                                                       
                                     }
                                                                                                                     
                                     select.close(); 
                                     conexion.close(); 
                                                                                 
                                } 
                                    
                           }                               
                               
                           System.out.println(); 
                               
                       }else{ 
                             context.addMessage(null,new FacesMessage( "El archivo proporcionado no es valido. Verifica " )); 
                             return; 
                       }
                                                                                                                 
                       l++;
                           
                  }                                                                                                             
                                         
              }catch(Exception e){ e.printStackTrace(); } 
                                                     
       }  
       
       public List<String> obtenerLineasDatos(UploadedFile archivo){
           
              List<String> datos = new ArrayList<String>();
              
              String nombreArchivo = FilenameUtils.getName(archivo.getName());
              System.out.println(nombreArchivo);                                                                                    
                             
              String linea = "";                                                 
              int temp;
              
              int h = 1; 
              
              try{
                  
                  byte[] bytes = archivo.getBytes();                                
                  
                  for( int i = 0; i <= (bytes.length - 1); i++){  
                                          
                       temp = (char)bytes[i];                       
                       
                       if( temp == -1 ){ break; } 
                    
                       linea += (char)temp;                                               
                       
                       if( temp == '\n' ){ 
                           datos.add(linea);
                           linea = "";
                       } 
                       
                  } 
                  
              }catch(Exception e){ e.printStackTrace(); }
              
              return datos;
                                     
       }
       
       public int obtenerTipoarchivo(String nombreArchivo){
           
              int reg_resp = -1;
              
              if( nombreArchivo.startsWith("R") || nombreArchivo.startsWith("r") ){ reg_resp = 0; }
              if( nombreArchivo.startsWith("S") || nombreArchivo.startsWith("s") ){ reg_resp = 1; }
                            
              return reg_resp;
             
       }
       
       public ArrayList<ModeloPrueba> getDatosDats(){ 
            
              ArrayList<ModeloPrueba> almp = new ArrayList<ModeloPrueba>(); 
              
              try{ 
                  
                  conexion = ds.getConnection(); 
                  Statement s = conexion.createStatement(); 
                  ResultSet rs = s.executeQuery("select desc_ident from lectura_originales"); 
                  
                  while(rs.next()){                       
                        ModeloPrueba mp = new ModeloPrueba(); 
                        mp.setDesc_ident(rs.getString(1)); 
                        almp.add(mp); 
                  } 
                  
                  s.close(); 
                  conexion.close(); 
                  
              }catch(Exception e){ e.printStackTrace(); } 
               
              return almp; 
            
       } 
       
       public void validarArchivo(FacesContext fc,UIComponent uic,Object o){ 
           
              System.out.println(o.getClass()); 
              /*if( !((File)o).getName().endsWith(".dat") ){ 
                  fc.addMessage("",new FacesMessage("Archivo invalido")); 
              }*/ 
           
       } 
   
       public UploadedFile getArchivo() { return archivo; } 

       public void setArchivo(UploadedFile archivo) { this.archivo = archivo; } 
       
} 
