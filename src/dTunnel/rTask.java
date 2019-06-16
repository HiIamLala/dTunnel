/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dTunnel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kulz0
 */
class rTask extends Thread{
    File ftr;
    String fName;
    Socket conn;
    long fSize = 0;
    int count;
    int speed;
    long received = 0;
    boolean done = false;

    public rTask(File ftr, Socket conn, long fSize) {
        this.ftr= ftr;
        this.conn = conn;
        this.fSize = fSize;
    }

    @Override
    public void run() {
        try {
            FileOutputStream fos = new FileOutputStream(ftr);
            try {
                DataInputStream cis = new DataInputStream(conn.getInputStream());
                byte buff[] = new byte[16*1024];
                while(received<fSize){
                    if((fSize-received)<16*1024){
                        buff = new byte[16*1024];
                        cis.readFully(buff, 0, (int) (fSize-received));
                        fos.write(buff, 0, (int)(fSize-received));
                        received+=(int)(fSize-received);
                        count+=(int)(fSize-received);
                    }
                    else{
                        buff = new byte[16*1024];
                        cis.readFully(buff, 0, 16*1024);
                        fos.write(buff, 0, 16*1024);
                        received+=16*1024;
                        count+=16*1024;
                    }
                }
                done = true;
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(dClient.class.getName()).log(Level.SEVERE, null, ex);
                done = true;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(dClient.class.getName()).log(Level.SEVERE, null, ex);
            done = true;
        }
        done = true;
    }
    
    public static long authFile(String fName, BufferedWriter bw, BufferedReader br) {
        float progress = 0;
        File ftr = new File("Received/"+fName);
        try {
            bw.write("/down "+fName+'\0');
            bw.flush();
            char buff[] = new char[4096];
            br.read(buff, 0, 4096);
            long received = 0;
            if(new String(buff,0,7).equalsIgnoreCase("@200:OK")){
                long fSize = Long.parseLong(Message.cleanBuff(buff).split("\\|")[1]);
                System.out.println("File size: " + fSize);
                bw.write("@201:CONFIRM|"+fSize+'\0');
                bw.flush();
                return fSize;
            }
            else{
                System.out.println(Message.cleanBuff(buff));
                return -1;
            }
        } catch (IOException ex) {
            Logger.getLogger(dClient.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public static String checkURL(BufferedReader br) {
        String fName = "";
        String fSize = "";
        try {
            char buff[] = new char[4096];
            br.read(buff, 0, 4096);
            if(Message.cleanBuff(buff).equalsIgnoreCase("@404:BADURL")){
                System.out.println("Bad URL.");
            }
            else if(new String(buff,0,7).equalsIgnoreCase("@200:OK")) {
                fName += Message.cleanBuff(buff).split("\\|")[1];
                System.out.println("File name: "+ fName);
                fSize += Message.cleanBuff(buff).split("\\|")[2];
                System.out.println("File size: "+ fSize);
            }
            else {
                System.out.println(Message.cleanBuff(buff));
            }
        } catch (IOException ex) {
            Logger.getLogger(dClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fName+"|"+fSize;
    }
}
