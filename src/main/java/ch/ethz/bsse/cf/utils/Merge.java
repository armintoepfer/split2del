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

import ch.ethz.bsse.cf.informationholder.Deletion;
import ch.ethz.bsse.cf.informationholder.Globals;
import ch.ethz.bsse.cf.informationholder.Read;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sf.samtools.BAMRecord;
import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMRecord;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Merge {

    static int counts = 0;

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
//        System.out.println("\n"+counts+"\n");
    }

    private static void loopSingleDirection(List<Read> l) {
        if (l == null || l.size() < 2) {
            return;
        } else {
            Collections.sort(l, new Comparator<Read>() {
                @Override
                public int compare(Read o1, Read o2) {
                    return new Integer(o1.internal_offset).compareTo(o2.internal_offset);
                }
            });
            for (int i = 1; i < l.size(); ++i) {
                if (l.get(i - 1).internal_offset + l.get(i - 1).matches > l.get(i).internal_offset) {
                    int overlap = (l.get(i - 1).internal_offset + l.get(i - 1).matches) - l.get(i).internal_offset;
                    if (overlap > 0 && overlap <= 10) {
                        Read r = l.get(i);
                        SAMRecord s = new SAMRecord(null);
                        s.setCigarString(r.cigar.toString());
                        r.cigar.setLength(0);
                        Cigar cigar = s.getCigar();
                        boolean firstMatch = true;
                        for (CigarElement ce : cigar.getCigarElements()) {
                            switch (ce.getOperator()) {
                                case X:
                                case EQ:
                                case M:
                                    if (firstMatch) {
                                        if (ce.getLength() - overlap < 0) {
                                            return;
                                        }
                                        if (ce.getLength() - overlap > 0) {
                                            r.cigar.append(ce.getLength() - overlap).append("M");
                                        }
                                        firstMatch = false;
                                        break;
                                    }
                                case I:
                                case D:
                                case S:
                                case H:
                                case P:
                                case N:
                                default:
                                    r.cigar.append(ce.getLength()).append(ce.getOperator());
                                    break;
                            }
                        }
                        r.internal_offset += overlap;
                        r.refStart += overlap;
                        String tmpS = r.sequence.substring(overlap);
                        r.sequence.setLength(0);
                        r.sequence.append(tmpS);
                        r.length -= overlap;
                        r.matches -= overlap;
//                        String tmpQ = r.quality.substring(1);
//                        r.quality.setLength(0);
//                        r.quality.append(tmpQ);
                    } else {
                        for (Read rs : l) {
                            Globals.FINAL_READS.add(rs);
                        }
                        counts++;
                        return;
                    }
                }
            }
            if (!Globals.FIX) {
                Read tmp = l.get(0);
                for (int i = 1; i < l.size(); ++i) {
//                    if (tmp == null || l.get(i) == null) {
//                        System.err.println("");
//                    }
                    tmp = mergeReadsPairwiseTmp(tmp, l.get(i));
                    if (tmp == null) {
                        break;
                    }
                }
                if (tmp != null) {
                    Globals.FINAL_READS.add(tmp);
                }
            } else {
                for (int i = 1; i < l.size(); ++i) {
                    Globals.FINAL_READS.add(mergeReadsPairwiseTmp(l.get(i - 1), l.get(i)));
                }
            }

            //Merge pairwise oldsql
//            for (Read r : l) {
//                for (Read r2 : l) {
//                    if (r != r2) {
//                        mergeReadsPairwise(r, r2);
//                    }
//                }
//            }
        }
    }

    private static Read mergeReadsPairwiseTmp(Read r, Read r2) {
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
//            System.out.println(r.refStart + (r.forward_strand ? "+" : "-") + r.length + "\t" + r2.refStart + (r2.forward_strand ? "+" : "-") + r2.length);
            return null;
        }

        int gap = rev.refStart - (fwd.refStart + fwd.length);
        nr.length = (rev.refStart + rev.length) - fwd.refStart;
        nr.forward_strand = fwd.forward_strand;
        nr.sequence = new StringBuilder().append(fwd.sequence).append(rev.sequence);
        nr.quality = new StringBuilder().append(fwd.quality).append(rev.quality);
        nr.cigar = new StringBuilder().append(fwd.cigar).append(gap).append("D").append(rev.cigar);
        nr.read_name = fwd.read_name;
        nr.ref_name = fwd.ref_name;
        nr.refStart = fwd.refStart;
        nr.as = fwd.as + rev.as;

        int global_position = fwd.refStart + fwd.length;
        if (Globals.FIX) {
            if (!Globals.DEL_MAP.containsKey(global_position)) {
                Globals.DEL_MAP.put(global_position, new LinkedList<Deletion>());
            }
            Globals.DEL_MAP.get(global_position).add(new Deletion(rev.sequence.substring(0, Globals.PRE_SUF_LENGTH + 1), fwd.sequence.substring(fwd.matches - Globals.PRE_SUF_LENGTH - 1, fwd.matches), gap));
        }
        return nr;
    }

    private static void mergeReadsPairwise(Read r, Read r2) {
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
//            System.out.println(r.refStart + (r.forward_strand ? "+" : "-") + r.length + "\t" + r2.refStart + (r2.forward_strand ? "+" : "-") + r2.length);
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
