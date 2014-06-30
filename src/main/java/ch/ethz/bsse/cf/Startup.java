/**
 * Copyright (c) 2014 Armin Töpfer
 *
 * This file is part of Split2Del.
 *
 * Split2Del is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 *
 * ConsensusFixer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Split2Del. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.ethz.bsse.cf;

import ch.ethz.bsse.cf.informationholder.Deletion;
import ch.ethz.bsse.cf.informationholder.Globals;
import ch.ethz.bsse.cf.informationholder.Read;
import ch.ethz.bsse.cf.utils.Merge;
import ch.ethz.bsse.cf.utils.Utils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.samtools.SAMFormatException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Startup {

    public static void main(String[] args) throws IOException {
        new Startup().doMain(args);
        System.exit(0);
    }
    //GENERAL
    @Option(name = "-i")
    private String input;
    @Option(name = "-fix")
    private boolean fix;
    @Option(name = "-consensus")
    private boolean consensus;
    @Option(name = "-splength")
    private int fl = 5;

    private void setMainParameters() {
        Globals.SINGLE_CORE = true;
        Globals.PRE_SUF_LENGTH = fl;
        Globals.FIX = fix;
    }

    private void parse() throws CmdLineException {
        if (this.input == null) {
            throw new CmdLineException("No input given");
        }
        Utils.parseBAM(input);

    }

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        parser.setUsageWidth(80);
        try {
            parser.parseArgument(args);
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(" ");
            }
            setMainParameters();
            parse();
            Merge.cleanList();
            Merge.merge();
            if (fix) {
                System.out.println("Position\tDeletion_length\tCount\tPrefix\tSuffix");
                for (Map.Entry<Integer, List<Deletion>> e : Globals.DEL_MAP.entrySet()) {
                    if (consensus) {
                        
                        Map<Integer, char[][]> consensus_map_prefix = new HashMap<>();
                        Map<Integer, char[][]> consensus_map_suffix = new HashMap<>();
                        
                        for (Deletion d : e.getValue()) {
                            {
                                if (!consensus_map_prefix.containsKey(d.del_length)) {
                                    consensus_map_prefix.put(d.del_length, new char[fl + 1][4]);
                                }
                                int i = 0;
                                for (char c : d.prefix.toCharArray()) {
                                    int j = 0;
                                    switch (c) {
                                        case 'A':
                                            j = 0;
                                            break;
                                        case 'C':
                                            j = 1;
                                            break;
                                        case 'G':
                                            j = 2;
                                            break;
                                        case 'T':
                                            j = 3;
                                            break;
                                        default:
                                            break;
                                    }
                                    System.out.print("");
                                    if (i >= d.prefix.toCharArray().length) {
                                        System.err.println("ERROR");
                                        System.exit(0);
                                    }
                                    if (j >= fl) {
                                        System.err.println("ERROR 2");
                                        System.exit(0);
                                    }
                                    consensus_map_prefix.get(d.del_length)[i++][j]++;
                                }
                            }
                            {
                                if (!consensus_map_suffix.containsKey(d.del_length)) {
                                    consensus_map_suffix.put(d.del_length, new char[fl + 1][4]);
                                }
                                int i = 0;
                                for (char c : d.suffix.toCharArray()) {
                                    int j = 0;
                                    switch (c) {
                                        case 'A':
                                            j = 0;
                                            break;
                                        case 'C':
                                            j = 1;
                                            break;
                                        case 'G':
                                            j = 2;
                                            break;
                                        case 'T':
                                            j = 3;
                                            break;
                                        default:
                                            break;
                                    }
                                    System.out.print("");
                                    if (i >= d.suffix.toCharArray().length) {
                                        System.err.println("ERROR");
                                        System.exit(0);
                                    }
                                    if (j >= fl) {
                                        System.err.println("ERROR 2");
                                        System.exit(0);
                                    }
                                    consensus_map_suffix.get(d.del_length)[i++][j]++;
                                }
                            }
                        }
                        for (Map.Entry<Integer, char[][]> e2 : consensus_map_prefix.entrySet()) {
                            System.out.print(e.getKey() + "\t" + e2.getKey() + "\t");
                            int sum = e2.getValue()[0][0]+e2.getValue()[0][1]+e2.getValue()[0][2]+e2.getValue()[0][3];
                            System.out.print(sum + "\t");
                            for (int i = 0; i < fl + 1; i++) {
                                int max = 0;
                                char base = ' ';
                                for (int j = 0; j < 4; j++) {
                                    if (e2.getValue()[i][j] >= max) {
                                        max = e2.getValue()[i][j];
                                        switch (j) {
                                            case 0:
                                                base = 'A';
                                                break;
                                            case 1:
                                                base = 'C';
                                                break;
                                            case 2:
                                                base = 'G';
                                                break;
                                            case 3:
                                                base = 'T';
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                }
                                System.out.print(base);
                            }
                            System.out.print("\t");
                            for (int i = 0; i < fl+1; i++) {
                                int max = 0;
                                char base = ' ';
                                for (int j = 0; j < 4; j++) {
                                    if (consensus_map_suffix.get(e2.getKey())[i][j] >= max) {
                                        max = consensus_map_suffix.get(e2.getKey())[i][j];
                                        switch (j) {
                                            case 0:
                                                base = 'A';
                                                break;
                                            case 1:
                                                base = 'C';
                                                break;
                                            case 2:
                                                base = 'G';
                                                break;
                                            case 3:
                                                base = 'T';
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                }
                                System.out.print(base);
                            }
                            System.out.println("");
                        }
                    } else {
                        for (Deletion d : e.getValue()) {
                            System.out.println(e.getKey() + "\t" + d.del_length + "\t" + d.prefix + "\t" + d.suffix);
                        }
                    }
                }
            } else {
                System.out.print(Globals.HEADER.toString());
                for (Read r : Globals.FINAL_READS) {
                    System.out.println(r.read_name + "\t" + 0 + "\t" + r.ref_name + "\t" + r.refStart + "\t" + 60 + "\t" + r.cigar.toString() + "\t*\t0\t0" + "\t" + r.sequence.toString() + "\t*\tAS:i:" + r.as);
                }
            }
        } catch (SAMFormatException e) {
            System.err.println("");
            System.err.println("Input file is not in BAM format.");
            System.err.println(e);
        } catch (CmdLineException cmderror) {
            System.err.println(cmderror.getMessage());
            System.err.println("");
            System.err.println("Split2Del version: " + Startup.class.getPackage().getImplementationVersion());
            System.err.println("");
            System.err.println("USAGE: java -jar Split2Del.jar options...\n");
            System.err.println(" -------------------------");
            System.err.println(" === GENERAL options ===");
            System.err.println("  -i INPUT\t\t: Alignment file in BAM format (required).");
            System.err.println("");
            System.err.println(" -------------------------");
            System.err.println(" === Technical options ===");
            System.err.println("  -XX:NewRatio=9\t: Reduces the memory consumption (RECOMMENDED to use).");
            System.err.println("  -Xms2G -Xmx10G\t: Increase heap space.");
            System.err.println("  -XX:+UseParallelGC\t: Enhances performance on multicore systems.");
            System.err.println("  -XX:+UseNUMA\t\t: Enhances performance on multi-CPU systems.");
            System.err.println(" -------------------------");
            System.err.println(" === EXAMPLES ===");
            System.err.println("   java -XX:+UseParallelGC -Xms2g -Xmx10g -XX:+UseNUMA -XX:NewRatio=9 -jar Split2Del.jar -i alignment.bam -r reference.fasta");
            System.err.println(" -------------------------");
        }
    }
}
