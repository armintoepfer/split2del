/**
 * Copyright (c) 2013 Armin Töpfer
 *
 * This file is part of ConsensusFixer.
 *
 * ConsensusFixer is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * ConsensusFixer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ConsensusFixer. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.bsse.cf.utils;

import ch.ethz.bsse.cf.informationholder.Globals;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.samtools.AbstractBAMFileIndex;
import net.sf.samtools.BAMIndexMetaData;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Utils {

    public final static String SAVEPATH = "";

    public static void mkdir(String save) {
        if (!new File(save).exists()) {
            if (!new File(save).mkdirs()) {
                throw new RuntimeException("Cannot create directory: " + save);
            }
        }
    }

    public static void appendFile(String path, String sb) {
        try {
            // Create file 
            FileWriter fstream = new FileWriter(path, true);
            try (BufferedWriter out = new BufferedWriter(fstream)) {
                out.write(sb);
            }
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error append file: ");
            System.err.println(path);
            System.err.println(sb);
        }
    }

    public static void saveFile(String path, String sb) {
        try {
            // Create file 
            FileWriter fstream = new FileWriter(path);
            try (BufferedWriter out = new BufferedWriter(fstream)) {
                out.write(sb);
            }
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error save file: ");
            System.err.println(path);
            System.err.println(sb);
        }
    }

    public static void parseBAM(String location) {
        File bam = new File(location);
        int size = 0;
        try (SAMFileReader sfr = new SAMFileReader(bam)) {
            AbstractBAMFileIndex index = (AbstractBAMFileIndex) sfr.getIndex();
            int nRefs = index.getNumberOfReferences();
            for (int i = 0; i < nRefs; i++) {
                BAMIndexMetaData meta = index.getMetaData(i);
                size += meta.getAlignedRecordCount();
            }
        }
        StatusUpdate.getINSTANCE().printForce("Read count\t\t" + size);
        try (SAMFileReader sfr = new SAMFileReader(bam)) {
            if (Globals.SINGLE_CORE) {
                StatusUpdate.getINSTANCE().println("Computing\t\t");
                int i = 0;
                for (SAMRecord r : sfr) {
                    if (i++ % 1000 == 0) {
                        StatusUpdate.getINSTANCE().print("Computing\t\t" + Math.round(((double) i * 100) / size) + "%");
                    }
                    SFRComputing.single(r);
                }
                StatusUpdate.getINSTANCE().printForce("Computing\t\t100%");
            } else {
                StatusUpdate.getINSTANCE().println("Loading BAM");

                List<List<SAMRecord>> records = new LinkedList();
                List<SAMRecord> tmp = new LinkedList<>();
                SAMRecordIterator it = sfr.iterator();
                for (int x = 0, y = 0; x < size; x += STEP_SIZE) {
                    records.clear();
                    tmp.clear();
                    y = x + STEP_SIZE < size ? x + STEP_SIZE : size - 1;
                    StatusUpdate.getINSTANCE().print("Loading BAM\t\t" + Math.round(((double) y * 100) / size) + "%");
                    final int max = y - x < 100 ? 100 : (int) Math.ceil((y - x) / Runtime.getRuntime().availableProcessors());
                    int c = 0;
                    do {
                        if (++c % max == 0) {
                            records.add(tmp);
                            tmp = new LinkedList<>();
                        }
                        if (it.hasNext()) {
                            tmp.add(it.next());
                        } else {
                            records.add(tmp);
                            break;
                        }
                    } while (c < y - x);
                    records.add(tmp);
                    Parallel.ForEach(records, new LoopBody<List<SAMRecord>>() {
                        @Override
                        public void run(List<SAMRecord> l) {

                            for (SAMRecord r : l) {
                                SFRComputing.single(r);
                            }
                        }
                    });
                }
                StatusUpdate.getINSTANCE().printForce("Loading BAM\t\t100%\n");
            }
        }
    }
    public static final int STEP_SIZE = Runtime.getRuntime().availableProcessors() * 50000;

    public static Map<String, String> parseHaplotypeFile(String location) {
        Map<String, String> hapMap = new ConcurrentHashMap<>();
        try {
            FileInputStream fstream = new FileInputStream(location);
            StringBuilder sb;
            String head = null;
            try (DataInputStream in = new DataInputStream(fstream)) {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                sb = new StringBuilder();
                while ((strLine = br.readLine()) != null) {
                    if (strLine.startsWith(">")) {
                        if (sb.length() > 0) {
                            hapMap.put(sb.toString().toUpperCase(), head);
                            sb.setLength(0);
                        }
                        head = strLine;
                    } else {
                        sb.append(strLine);
                    }
                }
                hapMap.put(sb.toString().toUpperCase(), head);
            }
        } catch (IOException | NumberFormatException e) {
        }
        return hapMap;
    }
   

    public static void error() {
        System.out.println("    .o oOOOOOOOo                                            OOOo");
        System.out.println("    Ob.OOOOOOOo  OOOo.      oOOo.                      .adOOOOOOO");
        System.out.println("    OboO\"\"\"\"\"\"\"\"\"\"\"\".OOo. .oOOOOOo.    OOOo.oOOOOOo..\"\"\"\"\"\"\"\"\"'OO");
        System.out.println("    OOP.oOOOOOOOOOOO \"POOOOOOOOOOOo.   `\"OOOOOOOOOP,OOOOOOOOOOOB'");
        System.out.println("    `O'OOOO'     `OOOOo\"OOOOOOOOOOO` .adOOOOOOOOO\"oOOO'    `OOOOo");
        System.out.println("    .OOOO'            `OOOOOOOOOOOOOOOOOOOOOOOOOO'            `OO");
        System.out.println("    OOOOO                 '\"OOOOOOOOOOOOOOOO\"`                oOO");
        System.out.println("   oOOOOOba.                .adOOOOOOOOOOba               .adOOOOo.");
        System.out.println("  oOOOOOOOOOOOOOba.    .adOOOOOOOOOO@^OOOOOOOba.     .adOOOOOOOOOOOO");
        System.out.println(" OOOOOOOOOOOOOOOOO.OOOOOOOOOOOOOO\"`  '\"OOOOOOOOOOOOO.OOOOOOOOOOOOOO");
        System.out.println(" \"OOOO\"       \"YOoOOOOMOIONODOO\"`  .   '\"OOROAOPOEOOOoOY\"     \"OOO\"");
        System.out.println("    Y           'OOOOOOOOOOOOOO: .oOOo. :OOOOOOOOOOO?'         :`");
        System.out.println("    :            .oO%OOOOOOOOOOo.OOOOOO.oOOOOOOOOOOOO?         .");
        System.out.println("    .            oOOP\"%OOOOOOOOoOOOOOOO?oOOOOO?OOOO\"OOo");
        System.out.println("                 '%o  OOOO\"%OOOO%\"%OOOOO\"OOOOOO\"OOO':");
        System.out.println("                      `$\"  `OOOO' `O\"Y ' `OOOO'  o             .");
        System.out.println("    .                  .     OP\"          : o     .");
        System.out.println("                              :");
        System.out.println("                              .");
    }
}
