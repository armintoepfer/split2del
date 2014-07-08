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
package ch.ethz.bsse.cf.utils;

import ch.ethz.bsse.cf.informationholder.Globals;
import ch.ethz.bsse.cf.informationholder.Read;
import java.util.ArrayList;
import net.sf.samtools.CigarElement;
import static net.sf.samtools.CigarOperator.D;
import static net.sf.samtools.CigarOperator.EQ;
import static net.sf.samtools.CigarOperator.H;
import static net.sf.samtools.CigarOperator.I;
import static net.sf.samtools.CigarOperator.M;
import static net.sf.samtools.CigarOperator.N;
import static net.sf.samtools.CigarOperator.P;
import static net.sf.samtools.CigarOperator.S;
import static net.sf.samtools.CigarOperator.X;
import net.sf.samtools.SAMRecord;

/**
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class SFRComputing {

    public static void single(SAMRecord samRecord) {
        try {
            if (samRecord.getAlignmentBlocks().isEmpty()) {
                return;
            }
            if (!Globals.READ_MAP.containsKey(samRecord.getReadName())) {
                Globals.READ_MAP.put(samRecord.getReadName(), new ArrayList<Read>());
            }

            int readStart = 0;
            int insertion_offset = 0;
            int deletion_offset = 0;
            int matches = 0;
            Read r = new Read();
            r.refStart = samRecord.getUnclippedStart();
            r.sa = samRecord.getSupplementaryAlignmentFlag();
            r.read_name = samRecord.getReadName();
            r.ref_name = samRecord.getReferenceName();
            for (SAMRecord.SAMTagAndValue tav : samRecord.getAttributes()) {
                switch (tav.tag) {
                    case "SA":
                        r.forward_strand = String.valueOf(tav.value).split(",")[2].equals("+");
                        break;
                    case "AS":
                        r.as = (int)tav.value;
                        break;
                    default:
                        break;
                }
            }
            boolean start = true;
            for (CigarElement c : samRecord.getCigar().getCigarElements()) {
                switch (c.getOperator()) {
                    case X:
                    case EQ:
                    case M:
                        start = false;
                        if ((readStart + c.getLength()) > samRecord.getReadBases().length) {
                            System.out.println("\nInput alignment is corrupt.\nCIGAR is longer than actual read-length.");
                            System.exit(9);
                        }
                        r.cigar.append(c.getLength()).append("M");
                        matches += c.getLength();
                        for (int i = 0; i < c.getLength(); i++) {
                            r.sequence.append((char) samRecord.getReadBases()[readStart + insertion_offset]);
                            r.quality.append(samRecord.getBaseQualityString().charAt(readStart + insertion_offset));
                            readStart++;
                        }
                        break;
                    case I:
                        start = false;
                        if ((readStart + c.getLength()) > samRecord.getReadBases().length) {
                            System.out.println("\nInput alignment is corrupt.\nCIGAR is longer than actual read-length.");
                            System.exit(9);
                        }
                        r.cigar.append(c.getLength()).append("I");
                        for (int i = 0; i < c.getLength(); i++) {
                            r.sequence.append((char) samRecord.getReadBases()[readStart + i + insertion_offset]);
                            r.quality.append(samRecord.getBaseQualityString().charAt(readStart + i + insertion_offset));
                        }
                        insertion_offset += c.getLength();
                        break;
                    case D:
                        start = false;
                        r.cigar.append(c.getLength()).append("D");
                        deletion_offset += c.getLength();
                        break;
                    case S:
                        if (start) {
                            r.internal_offset = c.getLength();
                            r.refStart += c.getLength();
                            start = false;
                        }
                        readStart += c.getLength();
                        break;
                    case H:
                        if (start) {
                            r.internal_offset = c.getLength();
                            r.refStart += c.getLength();
                            start = false;
                        }
                        break;
                    case P:
                        System.out.println("P");
                        System.exit(9);
                        break;
                    case N:
                        System.out.println("N");
                        System.exit(9);
                        break;
                    default:
                        break;
                }
            }
            r.matches = matches+insertion_offset;
            r.length = matches + deletion_offset;
            if (r.length >= Globals.MIN_LENGTH) {
                Globals.READ_MAP.get(samRecord.getReadName()).add(r);
            }
//                System.out.println(samRecord.getReadName() + "\t" + 0 + "\t" + samRecord.getReferenceName() + "\t" + r.refStart + "\t" + 60 + "\t" + r.cigar.toString() + "\t*\t0\t0" + "\t" + r.sequence.toString() + "\t*" + "\n");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println();
            System.err.println(e);
            System.err.println();
        } catch (Exception e) {
            System.err.println("WOOT:" + e);
            // Sometimes CIGAR is not correct. In that case we simply ignore it/
        }
    }
}
