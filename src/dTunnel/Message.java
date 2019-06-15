/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dTunnel;

/**
 *
 * @author kulz0
 */
public class Message {
    public String key,content = "";
    public Message(String key,String content) {
        this.key = key;
        this.content = content;
    }
    
    public Message(char[] buff){
        String cmd = cleanBuff(buff);
        if(cmd.length()<=1){
            this.key = "NULL";
            this.content = "NULL";
        }
        else{
            String temp[] = cmd.substring(1, cmd.length()).split(" ");
            if(temp.length == 1){
                this.key = temp[0];
            }
            else {
                String content = "";
                for(int i=1;i<temp.length;i++){
                    content+=temp[i];
                }
                this.key = temp[0];
                this.content = content;
            }
        }
    }
    public Message(String cmd){
        if(cmd.length()<=1){
            this.key = "NULL";
            this.content = "NULL";
        }
        else{
            String temp[] = cmd.substring(1, cmd.length()).split(" ");
            if(temp.length == 1){
                this.key = temp[0];
            }
            else {
                String content = "";
                for(int i=1;i<temp.length;i++){
                    content+=temp[i];
                }
                this.key = temp[0];
                this.content = content;
            }
        }
    }
    
    static public String cleanBuff(char[] buff){
        String out = "";
        for(int i=0;i<buff.length;i++){
            if(buff[i] == '\0') break;
            out += buff[i];
        }
        return out;
    }
    
    public void decodeCmd(String cmd){
        String temp[] = cmd.split(" ");
        if(temp.length == 1){
            this.key = temp[0];
        }
        else {
            String content = "";
            for(int i=1;i<temp.length;i++){
                content+=temp[i];
            }
            this.key = temp[0];
            this.content =content;
        }
    }
}