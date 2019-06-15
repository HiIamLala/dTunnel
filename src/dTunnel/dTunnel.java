/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dTunnel;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kulz0
 */
class dTunnel extends Thread{
    ArrayList<dTunnel> clients;
    Socket conn;
    Integer speed;

    public dTunnel(Socket conn, ArrayList<dTunnel> clients) {
        this.conn = conn;
        this.clients = clients;
        System.out.println("Connected (" + conn.getInetAddress() + ":" + conn.getPort() + ")");
    }

    @Override
    public void start() {
        try {
            boolean end = false;
            boolean su = false;
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            bw.write("Connected to dTunnel server. What can I help you?"+'\0');
            bw.flush();
            while(!end){
                char buff[] = new char[4096];
                br.read(buff, 0, 4096);
                Message mess = new Message(buff);
                switch(mess.key){
                    case "su":
                        if(mess.content.isEmpty()){
                            bw.write("Password?"+'\0');
                            bw.flush();
                            buff = new char[4096];
                            br.read(buff, 0, 4096);
                            if(Message.cleanBuff(buff).equalsIgnoreCase("12101998")){
                                su = true;
                                bw.write("You're loged in as root."+'\0');
                                bw.flush();
                            }
                            else{
                                bw.write("Wrong password."+'\0');
                                bw.flush();
                            }
                        }
                        else if(su){
                            bw.write("Again? Really..???"+'\0');
                            bw.flush();
                        }
                        else {
                            if(mess.content.equalsIgnoreCase("12101998")){
                                su = true;
                                bw.write("You're loged in as root."+'\0');
                                bw.flush();
                            }
                            else{
                                bw.write("Wrong password."+'\0');
                                bw.flush();
                            }
                        }
                        break;
                    case "iperf":
                        System.out.println("Start test bandwidth");
                        char temp[] = getAlphaNumericString(4096).toCharArray();
                        for(int i=0;i<256;i++){
                            bw.write(temp, 0, 4096);
                            bw.flush();
                        }
                        buff = new char[4096];
                        br.read(buff, 0, 4096);
                        this.speed = Integer.parseInt(Message.cleanBuff(buff));
                        if(this.speed > 1048576)
                            System.out.println("Speed: " + (float) this.speed/1048576 + " MByte/s");
                        else if(this.speed > 1024)
                            System.out.println("Speed: " + (float) this.speed/1024 + " KByte/s");
                        else
                             System.out.println("Speed: " + this.speed + " Byte/s");
                        break;
                    case "get":
                        //Download File Request
                        dTask down = new dTask(mess.content,bw);
                        down.start();
                        spdC dSpd = new spdC(down, 2);
                        dSpd.start();
                        break;
                    case "down":
                        String fName = mess.content;
                        System.out.println("Prepairing file " + fName);
                        File fts = new File("Download/"+fName);
                        if(fts.exists()&&fts.canRead()){
                            bw.write("@200:OK|"+fts.length()+'\0');
                            bw.flush();
                            buff = new char[4096];
                            br.read(buff, 0, 4096);
                            if(new String(buff,0,12).equalsIgnoreCase("@201:CONFIRM") && (Long.parseLong(Message.cleanBuff(buff).split("\\|")[1]))==fts.length()){
                                sendFile(fts,conn);
                            }
                            else{
                                System.out.println("Authen Error");
                            }
                        }
                        else{
                            bw.write("@404:NOTFOUND|"+fName+'\0');
                            bw.flush();
                        }
                        break;
                    case "ls":
                            File dFolder = new File("Download");
                            File list[] = dFolder.listFiles();
                            String out = "@202:REPLS";
                            for(File item : list){
                                if(item.isDirectory()){
                                    out += "|" + item.getName() + "#" + "-D";
                                }
                                else{
                                    out += "|" + item.getName() + "#" + item.length();
                                }
                            }
                            bw.write(out+'\0');
                    default:
                        bw.write("Unknow command!"+'\0');
                        bw.flush();
                        break;
                }
            }
            
        } catch (IOException ex) {
            Logger.getLogger(dTunnel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static String getAlphaNumericString(int n) 
    { 
  
        // chose a Character random from this String 
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                    + "0123456789"
                                    + "abcdefghijklmnopqrstuvxyz"; 
  
        // create StringBuffer size of AlphaNumericString 
        StringBuilder sb = new StringBuilder(n); 
  
        for (int i = 0; i < n; i++) { 
  
            // generate a random number between 
            // 0 to AlphaNumericString variable length 
            int index 
                = (int)(AlphaNumericString.length() 
                        * Math.random()); 
  
            // add Character one by one in end of sb 
            sb.append(AlphaNumericString 
                          .charAt(index)); 
        } 
  
        return sb.toString(); 
    } 

    private void sendFile(File fts, Socket conn) {
        try {
            System.out.println(fts.getName());
            FileInputStream fis = new FileInputStream(fts);
            try {
                DataOutputStream cos = new DataOutputStream(conn.getOutputStream());
                long sent = 0;
                long fSize = fts.length();
                byte buff[] = new byte[16*1024];
                while(sent<fSize){
                    if((fSize-sent)<16*1024){
                        buff = new byte[16*1024];
                        fis.read(buff, 0, (int)(fSize-sent));
                        cos.write(buff, 0, (int)(fSize-sent));
                        cos.flush();
                        sent+=(int)(fSize-sent);
                    }
                    else {
                        buff = new byte[16*1024];
                        fis.read(buff, 0, 16*1024);
                        cos.write(buff, 0, 16*1024);
                        cos.flush();
                        sent+=16*1024;
                    }
                }
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(dTunnel.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(dTunnel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
