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
package ch.ethz.bsse.cf.utils;

import ch.ethz.bsse.cf.informationholder.Globals;
import ch.ethz.bsse.cf.informationholder.Read;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Merge {

    public static void cleanList() {
        double oldSize = Globals.READ_MAP.size();
        for (Map.Entry<String, List<Read>> e : Globals.READ_MAP.entrySet()) {
            if (e.getValue().size() < 2) {
                Globals.READ_MAP.remove(e.getKey());
                for (Read r : e.getValue()) {
                    Globals.FINAL_READS.add(r);
                }
                continue;
            }
            boolean hasSA = false;
            for (Read r : e.getValue()) {
                if (!hasSA && r.sa) {
                    hasSA = true;
                    break;
                }
            }
            if (!hasSA) {
                Globals.READ_MAP.remove(e.getKey());
                for (Read r : e.getValue()) {
                    Globals.FINAL_READS.add(r);
                }
            }
        }
        StatusUpdate.getINSTANCE().println("Splitreads\t\t" + String.valueOf(Math.round(10000 * Globals.READ_MAP.size() / oldSize) / 10000.0) + "%");
    }

    public static void merge() {
        for (Map.Entry<String, List<Read>> e : Globals.READ_MAP.entrySet()) {
            List<Read> forward = new ArrayList<>();
            List<Read> reverse = new ArrayList<>();
            for (Read r : e.getValue()) {
                if (r.forward_strand) {
                    forward.add(r);
                } else {
                    reverse.add(r);
                }
            }
            if (forward.size() >= 2 || reverse.size() >= 2) {
                Globals.READ_MAP.remove(e.getKey());
            }
            loopSingleDirection(forward);
            loopSingleDirection(reverse);
        }
    }

    private static void loopSingleDirection(List<Read> l) {
        if (l == null || l.size() < 2) {
            return;
        } else {
            for (Read r : l) {
                for (Read r2 : l) {
                    if (r != r2) {
                        mergeReads(r, r2);
                    }
                }
            }
        }
    }

    private static void mergeReads(Read r, Read r2) {
        Read nr = new Read();
        Read fwd;
        Read rev;
        if (r.refStart < r2.refStart && r.getEnd() < r2.refStart) {
            fwd = r;
            rev = r2;
        } else if (r.refStart > r2.refStart && r.refStart > r2.getEnd()) {
            fwd = r2;
            rev = r;
        } else {
            System.out.println(r.refStart + (r.forward_strand ? "+" : "-") + r.length + "\t" + r2.refStart + (r2.forward_strand ? "+" : "-") + r2.length);
            return;
        }

        int gap = rev.refStart - (fwd.refStart + fwd.length);
        nr.forward_strand = fwd.forward_strand;
        nr.sequence = new StringBuilder().append(fwd.sequence).append(rev.sequence);
        nr.quality = new StringBuilder().append(fwd.quality).append(rev.quality);
        nr.cigar = new StringBuilder().append(fwd.cigar).append(gap).append("D").append(rev.cigar);
        nr.read_name = fwd.read_name;
        nr.ref_name = fwd.ref_name;
        nr.refStart = fwd.refStart;
        nr.as = fwd.as + rev.as;
        Globals.FINAL_READS.add(nr);
    }
}
