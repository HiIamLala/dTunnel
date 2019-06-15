/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dTunnel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kulz0
 */
class spdC extends Thread{
    public boolean trigger;
    public int count=0,speed=0,interval=1;
    public dTask down;
    public spdC(boolean trigger,int count,int speed){
        this.trigger = trigger;
        this.count = count;
        this.speed = speed;
    }
    public spdC(boolean trigger,int count,int speed, int interval){
        this.trigger = trigger;
        this.count = count;
        this.speed = speed;
        this.interval = interval;
    }
    
    public spdC(dTask down,int interval){
        this.down = down;
        this.interval = interval;
    }

    @Override
    public void run() {
        while(!down.done){
            down.count = 0;
            try {
                    sleep(interval*1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(spdC.class.getName()).log(Level.SEVERE, null, ex);
            }
            down.spd = down.count/interval;
        }
    }
}
