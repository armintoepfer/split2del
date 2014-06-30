/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.ethz.bsse.cf.informationholder;

/**
 *
 * @author toepfera
 */
public class Deletion {
    public String suffix;
    public String prefix;
    public int del_length;

    public Deletion(String suffix, String prefix, int del_length) {
        this.suffix = suffix;
        this.prefix = prefix;
        this.del_length = del_length;
    }
    
}
