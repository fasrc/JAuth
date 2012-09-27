package JAuthCLI;
import JAuthCLI.Base32String;
import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;
import java.util.Calendar;


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
   System.out.println(":----------------------------:--------:");
   System.out.println(":       Code Wait Time       :  Code  :");
   System.out.println(":----------------------------:--------:");
   Main main = new Main();
   String inFile=args[0];
   try {
   RandomAccessFile raf= new RandomAccessFile(inFile,"r");
   FileReader fileReader = new FileReader(raf.getFD());
   BufferedReader bufReader  = new LineNumberReader(fileReader,65536);
   main.reminder(bufReader.readLine()); 
   }
   catch (IOException e){
    e.printStackTrace();
   }
 }
 public void reminder(String secret) {
   timer = new Timer();
   timer.scheduleAtFixedRate(new TimedPin(secret), 0, 1 * 1000);
 }
 class TimedPin extends TimerTask {
    private String secret; 
    public TimedPin (String secret){
      this.secret=secret;
    }
    String previouscode="";
    public void run() {
        String newout = Main.computePin(secret,null);
        if(previouscode.equals(newout)){
           System.out.print("."); 
        } 
        else {
          if(previouscode.equals("")){
            //print "."s for initial code that's ready upon start up
            for (int i=0; i<29;i++){
              System.out.print(".");
            }
          }
          System.out.println(": "+ newout + " :");
          if(previouscode.equals("")){
            //print "."s for progress already made towards the second code (new codes happen at :00 and :30 seconds on the system clock)
            for (int i=0; i<Calendar.getInstance().get(Calendar.SECOND)%30; i++){
              System.out.print(".");
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
