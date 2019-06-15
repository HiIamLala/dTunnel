/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dTunnel;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kulz0
 */
public class dTask extends Thread{
    String content = "";
    BufferedWriter bw;
    boolean done = false;
    int count = 0;
    int spd = 0;
    long downloaded = 0;
    long fSize;
    public dTask(String content, BufferedWriter bw) {
        this.content = content;
        this.bw = bw;
    }

    @Override
    public void run() {
        FileOutputStream fos = null;
        try {
            String fname = "untitled";
            URL url = new URL(content);
            System.out.println("Link: " + content);
            if(!url.getFile().isEmpty()){
                fname = url.getFile().split("/")[url.getFile().split("/").length-1];
                System.out.println("File name: " + fname);
                fSize = getFileSize(url);
                fos = new FileOutputStream("Download/"+fname);
                System.out.println("File size: " + fSize + " Bytes");
                bw.write("@200:OK|"+fname+"|"+fSize+'\0');
                bw.flush();
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                downloaded = 0;
                syncTask synC = new syncTask(this);
                synC.start();
                BufferedInputStream in = new BufferedInputStream(url.openStream());
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fos.write(dataBuffer, 0, bytesRead);
                    downloaded += bytesRead;
                    count += bytesRead;
                    System.out.print("\rDownloaded " + (float)downloaded*100/fSize + " % | " + spd/1024 + " KB/s");
                }
                fos.close();
                done = true;
                bw.write("@:READY"+'\0');
                bw.flush();
            }
            else{
                System.out.println("Bad URL.");
                bw.write("@404:BADURL"+'\0');
                bw.flush();
            }
        } catch (IOException e) {
            // handle exception
        } finally{
            try {
                if(fos!=null)
                    fos.close();
            } catch (IOException ex) {
                Logger.getLogger(dTask.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    public static int getFileSize(URL url) {
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            if(conn instanceof HttpURLConnection) {
                ((HttpURLConnection)conn).setRequestMethod("HEAD");
            }
            conn.getInputStream();
            return conn.getContentLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(conn instanceof HttpURLConnection) {
                ((HttpURLConnection)conn).disconnect();
            }
        }
    }
}
