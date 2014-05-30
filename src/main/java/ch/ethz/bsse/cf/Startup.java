/**
 * Copyright (c) 2014 Armin Töpfer
 *
 * This file is part of Split2Del.
 *
 * Split2Del is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
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

import ch.ethz.bsse.cf.informationholder.Globals;
import ch.ethz.bsse.cf.informationholder.Read;
import ch.ethz.bsse.cf.utils.Merge;
import ch.ethz.bsse.cf.utils.Utils;
import java.io.File;
import java.io.IOException;
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

    private void setMainParameters() {
        Globals.SINGLE_CORE = true;
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
            System.out.print(Globals.HEADER.toString());
            for (Read r : Globals.FINAL_READS) {
                System.out.println(r.read_name + "\t" + 0 + "\t" + r.ref_name + "\t" + r.refStart + "\t" + 60 + "\t" + r.cigar.toString() + "\t*\t0\t0" + "\t" + r.sequence.toString() + "\t*\tAS:i:" + r.as);
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
