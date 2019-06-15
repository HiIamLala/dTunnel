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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kulz0
 */
public class dClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File rFolder = new File("Received");
        if(!(rFolder.exists()&&rFolder.isDirectory()))
            rFolder.mkdir();
        Scanner sc = new Scanner(System.in);
        System.out.print("Welcome. Enter your server address: ");
        String temp = sc.nextLine();
        String address = temp.split(":")[0];
        int port = 12345;
        if(temp.split(":").length > 1){
            port = Integer.parseInt(temp.split(":")[1]);
        }
        try {
            boolean end = false;
            Socket conn = new Socket(address,port);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            char buff[] = new char[4096];
            buff = new char[4096];
            br.read(buff, 0, 4096);
            System.out.println(new String(buff));
            while(!end){
                System.out.print("$ ");
                String cmd = sc.nextLine();
                Message mess = new Message(cmd);
                switch(mess.key){
                    case "su":
                        bw.write(cmd+'\0');
                        bw.flush();
                        if(mess.content.isEmpty()){
                            buff = new char[4096];
                            br.read(buff, 0, 4096);
                            System.out.println(new String(buff));
                            bw.write(sc.nextLine());
                            bw.flush();
                        }
                        buff = new char[4096];
                        br.read(buff, 0, 4096);
                        System.out.println(new String(buff));
                        break;
                    case "iperf":
                        bw.write(cmd+'\0');
                        bw.flush();
                        long start = System.currentTimeMillis();
                        for(int i=0;i<256;i++){
                            br.read(buff, 0, 4096);
                        }
                        long time = System.currentTimeMillis() - start;
                        long speed = 1048576000/time;
                        bw.write(Long.toString(speed)+'\0');
                        bw.flush();
                        if(speed > 1048576)
                            System.out.println("Speed: " + (float) speed/1048576 + " MByte/s");
                        else if(speed > 1024)
                            System.out.println("Speed: " + (float) speed/1024 + " KByte/s");
                        else
                             System.out.println("Speed: " + speed + " Byte/s");
                        break;
                    case "get":
                        bw.write(cmd+'\0');
                        bw.flush();
                        String fName = "";
                        long fSize=0;
                        if(!(fName = checkURL(br).split("\\|")[0]).isEmpty()){
                            System.out.println("File name: " + fName);
                            if(getServerProgress(br,fName,fSize)==100){
                                if((fSize = authFile(fName, bw, br))!=-1){
                                    System.out.println("File " + fName + " auhorized.");
                                    File ftr = new File("Received/"+fName);
                                    if(ftr.exists()){
                                        ftr = new File("Received/"+ftr.getName().split("\\.(?=[^\\.]+$)")[0]+"_new"+"."+ftr.getName().split("\\.(?=[^\\.]+$)")[1]);
                                    }
                                    receiveFile(ftr,conn,fSize);
                                }
                                else{
                                    System.out.println("File " + fName + " auhorized fail.");
                                }
                            }
                        }
                        break;
                    case "down":
                        System.out.println("Requesting file " + mess.content);
                        if((fSize = authFile(mess.content, bw, br))!=-1){
                            System.out.println("File " + mess.content + " auhorized.");
                            File ftr = new File("Received/"+mess.content);
                            if(ftr.exists()){
                                ftr = new File("Received/"+ftr.getName().split("\\.(?=[^\\.]+$)")[0]+"_new"+"."+ftr.getName().split("\\.(?=[^\\.]+$)")[1]);
                            }
                            receiveFile(ftr,conn,fSize);
                        }
                        else{
                                System.out.println("File " + mess.content + " auhorized fail.");
                        }
                        break;
                    case "ls":
                        bw.write(cmd+'\0');
                        bw.flush();
                        buff = new char[4096];
                        br.read(buff, 0, 4096);
                        if(new String(buff,0,10).equalsIgnoreCase("@202:REPLS")){
                            String list[] = Message.cleanBuff(buff).split("\\|");
                            for(int i = 1; i<list.length ; i++){
                                System.out.printf("%-50s\t|\t%-15s Bytes\n",list[i].split("#")[0], list[i].split("#")[1]);
                            }
                        }
                        break;
                    case "getperf":
                        bw.write(cmd+'\0');
                        bw.flush();
                        buff = new char[4096];
                        br.read(buff, 0, 4096);
                        int spd = Integer.parseInt(Message.cleanBuff(buff));
                        if(spd == -1){
                            System.out.println("Bad URL");
                        }
                        else{
                            if(spd > 1048576)
                                System.out.println("Speed: " + (float) spd/1048576 + " MByte/s");
                            else if(spd > 1024)
                                System.out.println("Speed: " + (float) spd/1024 + " KByte/s");
                            else
                                 System.out.println("Speed: " + spd + " Byte/s");
                        }
                        break;
                    default:
                        bw.write(cmd+'\0');
                        bw.flush();
                        buff = new char[4096];
                        br.read(buff, 0, 4096);
                        System.out.println(Message.cleanBuff(buff));
                        break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(dClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static long authFile(String fName, BufferedWriter bw, BufferedReader br) {
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

    private static void receiveFile(File ftr, Socket conn, long fSize) {
        try {
            FileOutputStream fos = new FileOutputStream(ftr);
            try {
                long received = 0;
                DataInputStream cis = new DataInputStream(conn.getInputStream());
                byte buff[] = new byte[16*1024];
                while(received<fSize){
                    if((fSize-received)<16*1024){
                        buff = new byte[16*1024];
                        cis.readFully(buff, 0, (int) (fSize-received));
                        fos.write(buff, 0, (int)(fSize-received));
                        received+=(int)(fSize-received);
                    }
                    else{
                        buff = new byte[16*1024];
                        cis.readFully(buff, 0, 16*1024);
                        fos.write(buff, 0, 16*1024);
                        received+=16*1024;
                    }
                    System.out.print("\rReceived " + (received/fSize)*100 + "%");
                }
                System.out.println();
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(dClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(dClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String checkURL(BufferedReader br) {
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
    
    Message decodeCmd(String cmd){
        String temp[] = cmd.split(" ");
        if(temp.length == 1){
            return new Message(temp[0],"");
        }
        else {
            String content = "";
            for(int i=1;i<temp.length;i++){
                content+=temp[i];
            }
            return new Message(temp[0],content);
        }
    }
    
    static float getServerProgress(BufferedReader br, String fName, long fSize){
        float progress = 0;
        try {
            boolean ready = false;
            char buff[] = new char[4096];
            while(!ready){
                buff = new char[4096];
                br.read(buff, 0, 4096);
                if(Message.cleanBuff(buff).equalsIgnoreCase("@:READY")){
                    System.out.println("Ready");
                    return 100;
                }
                progress = Float.parseFloat(Message.cleanBuff(buff).split("\\|")[0]);
                float sSpeed = Float.parseFloat(Message.cleanBuff(buff).split("\\|")[1]);
                if(sSpeed > 1048576)
                    System.out.printf("\rDownloaded: %.2f %% (%.2f MB/s)",progress, sSpeed/1048576);
                else if(sSpeed > 1024)
                    System.out.printf("\rDownloaded: %.2f %% (%.2f KB/s)",progress, sSpeed/1024);
                else
                    System.out.printf("\rDownloaded: %.2f %% (%.2f B/s)",progress, sSpeed);
            }
            return progress;
        } catch (IOException ex) {
            Logger.getLogger(dClient.class.getName()).log(Level.SEVERE, null, ex);
            return progress;
        }
    }
    
}
