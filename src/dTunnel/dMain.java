/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dTunnel;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kulz0
 */
public class dMain {
    public static void main(String[] args) {
        File dFolder = new File("Download");
        if(!(dFolder.exists()&&dFolder.isDirectory()))
            dFolder.mkdir();
        try {
            ServerSocket listener = new ServerSocket(12345);
            System.out.println("Listening at port 12345 ...");
            ArrayList<dTunnel> conns = new ArrayList<>();
            while(true){
                conns.add(new dTunnel(listener.accept(),conns));
                conns.get(conns.size()-1).start();
            }

        } catch (IOException ex) {
            Logger.getLogger(dMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
