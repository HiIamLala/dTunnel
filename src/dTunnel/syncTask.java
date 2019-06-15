/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dTunnel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kulz0
 */
class syncTask extends Thread{
    dTask down;

    public syncTask(dTask down) {
        this.down = down;
    }

    @Override
    public void run() {
        while(!down.done){
            try {
                down.bw.write(String.valueOf((float)down.downloaded*100/down.fSize)+"|"+down.spd+'\0');
                down.bw.flush();
                try {
                    sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(syncTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(syncTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    
    
    
}
