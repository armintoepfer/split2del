/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.ethz.bsse.cf.utils;

import ch.ethz.bsse.cf.informationholder.Globals;
import ch.ethz.bsse.cf.informationholder.Read;
import java.util.List;
import java.util.Map;

/**
 *
 * @author toepfera
 */
public class Merge {
    public static void cleanList() {
        double oldSize = Globals.READ_MAP.size();
        for (Map.Entry<String,List<Read>> e : Globals.READ_MAP.entrySet()) {
            boolean hasSA = false;
            for (Read r : e.getValue()) {
                if (!hasSA && r.sa) {
                    hasSA = true;
                    break;
                }
            }
            if (!hasSA) {
                Globals.READ_MAP.remove(e.getKey());
            }
        }
        StatusUpdate.getINSTANCE().println("Splitreads\t\t"+String.valueOf(Math.round(10000*Globals.READ_MAP.size()/oldSize)/10000.0)+"%");
        System.out.println("");
    }
    
    public static void merge() {
        for (Map.Entry<String,List<Read>> e : Globals.READ_MAP.entrySet()) {
            System.out.print(e.getKey());
            for (Read r : e.getValue()) {
                if (!r.sa) {
                    System.out.print(" "+r.refStart+r.strand+r.length);
                }
            }
            System.out.print(" | ");
            for (Read r : e.getValue()) {
                if (r.sa) {
                    System.out.print(" "+r.refStart+r.strand+r.length);
                }
            }
            System.out.println(" ");
        }
    }
}
