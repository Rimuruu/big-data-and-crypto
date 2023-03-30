package com.mypackage;

import java.io.IOException;
import java.util.StringTokenizer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;





public class Decrypt{


  public static class TestEncrypt
   {

    static byte[] secretKey = "9mng65v8jf4lxn93nabf981m".getBytes();
    static SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "TripleDES");
    static byte[] iv = "a76nb5h9".getBytes();
    static IvParameterSpec ivSpec = new IvParameterSpec(iv);
    static Cipher encryptCipher;


    public String decrypt(String t) {
        try{
         byte[] secretMessagesBytes = Base64.getDecoder().decode(t);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessagesBytes);
         // String encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
          String s = new String(encryptedMessageBytes, StandardCharsets.UTF_8);
          return s;
        }
         catch(Exception e) {
            System.out.println("issue with "+t);
            e.printStackTrace();
            
         }
         return "";
      }

    
    public String decryptLine(String line) {
        StringTokenizer itr = new StringTokenizer(line);
        if(itr.hasMoreTokens()){
       String result = decrypt(itr.nextToken())+ "       "; 
         while (itr.hasMoreTokens()) {
            String token = itr.nextToken();
         //result = result + decrypt(itr.nextToken())+" "; 
            if(!token.isEmpty()) result = result +decrypt(token)+" ";

         }
        
         return result;  
        }
        return "";
      }

    public String decryptParagraph(String paragraph) {
        StringTokenizer itr = new StringTokenizer(paragraph,"\n");
        String result = "";
       // String result = decrypt(itr.nextToken())+ "       "; 
         while (itr.hasMoreTokens()) {
         //result = result + decrypt(itr.nextToken())+" "; 
            result = result + decryptLine(itr.nextToken())+"\n";

         }
         return result;  
      }
        
 
  }



  public static void main(String[] args) throws Exception {
        TestEncrypt.encryptCipher = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");
        TestEncrypt.encryptCipher.init(Cipher.DECRYPT_MODE, TestEncrypt.secretKeySpec, TestEncrypt.ivSpec);
        TestEncrypt test = new TestEncrypt();

  
        /*StringTokenizer itr = new StringTokenizer(args[0],"\n\\s*\n");
         while (itr.hasMoreTokens()) {
         String t = itr.nextToken(); 
         System.out.println(t + "paragrah");
         
         }*/

         String[] paragraphs = args[0].split("\n\\s*\n");

        for (String paragraph : paragraphs) {
            System.out.println(test.decryptParagraph(paragraph)+"\n");
        }
         
  }
}