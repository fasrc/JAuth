package JAuthCLI;
import JAuthCLI.Base32String;
import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;


// read ~/.google-authenticator
// display counter with dots (30 second refresh)
// jcuff@srv:~/auth/javaauth/jc$ java -classpath ./ JAuthCLI.Main /home/jcuff/.google_authenticator 


public class Main {
 Timer timer;
 public static void main(String[] args) {
   Runtime.getRuntime().addShutdownHook(new Thread() {
     public void run() {
       System.out.println("");
     }
   });
   Main main = new Main();
   
   String inFile=args[0];

   //undocumented cli option for adding timestamp to output lines
   boolean addtimestamp = false;
   if(args.length>1){
      addtimestamp = true;
   }
   
   try {
   RandomAccessFile raf= new RandomAccessFile(inFile,"r");
   FileReader fileReader = new FileReader(raf.getFD());
   BufferedReader bufReader  = new LineNumberReader(fileReader,65536);
   main.reminder(bufReader.readLine(),addtimestamp); 
   }
   catch (IOException e){
    e.printStackTrace();
   }
   
   if(addtimestamp){
      //                                                          2012-11-24 11:26:36 EST
      System.out.println(":----------------------------:--------:-------------------------:");
      System.out.println(":       Code Wait Time       :  Code  :          Time           :");
      System.out.println(":----------------------------:--------:-------------------------:");
   }else{
      System.out.println(":----------------------------:--------:");
      System.out.println(":       Code Wait Time       :  Code  :");
      System.out.println(":----------------------------:--------:");
   }
 }
 public void reminder(String secret, boolean addtimestamp) {
   timer = new Timer();
   timer.scheduleAtFixedRate(new TimedPin(secret,addtimestamp), 0, 1 * 1000);
 }
 class TimedPin extends TimerTask {
    private String secret;
	private boolean addtimestamp = false;
    public TimedPin (String secret, boolean addtimestamp){
      this.secret=secret;
	  this.addtimestamp=addtimestamp;
    }
    String previouscode="";
	int numdots=0;  //there are timing issues on slower computers, still need to count dots rather than deduce from the system clock
    public void run() {
        String newout = Main.computePin(secret,null);
        if(previouscode.equals(newout)){
           if(numdots<29){
		      System.out.print("."); 
			  numdots++;
		   }
        } 
        else {
          if(previouscode.equals("")){
            //print "."s for initial code that's ready upon start up
            for (int i=0; i<29;i++){
              System.out.print(".");
            }
          }
          System.out.print(": "+ newout + " :");
		  if(this.addtimestamp){
		     Date d = new Date();
			 if(previouscode.equals("")){
			    System.out.print("           n/a           :");
			 }else{
			    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z ':'");
			    System.out.print(" " + f.format(d));
			 }
		  }
		  System.out.print("\n");
		  numdots = 0;
          if(previouscode.equals("")){
            //print "."s for progress already made towards the second code (new codes happen at :00 and :30 seconds on the system clock)
            for (int i=0; i<Calendar.getInstance().get(Calendar.SECOND)%30; i++){
              System.out.print(".");
			  numdots++;
            }
          }
        }
        previouscode = newout; 
       }
   } 
 public static String computePin(String secret, Long counter) {
    if (secret == null || secret.length() == 0) {
      return "Null or empty secret";
    }
    try {
      final byte[] keyBytes = Base32String.decode(secret);
      Mac mac = Mac.getInstance("HMACSHA1");
      mac.init(new SecretKeySpec(keyBytes, ""));
      PasscodeGenerator pcg = new PasscodeGenerator(mac);
      return pcg.generateTimeoutCode();
    } catch (GeneralSecurityException e) {
      return "General security exception";
    } catch (Base32String.DecodingException e) {
      return "Decoding exception";
    }
  }
}
