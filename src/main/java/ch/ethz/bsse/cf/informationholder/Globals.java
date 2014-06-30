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
package ch.ethz.bsse.cf.informationholder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Information holder for all necessary given and inferred parameters.
 *
 * @author Armin Töpfer (armin.toepfer [at] gmail.com)
 */
public class Globals {

    public static String SAVEPATH;
    public static boolean SINGLE_CORE;
    public static final Map<String, List<Read>> READ_MAP = new ConcurrentHashMap<>();
    public static int MIN_LENGTH = 100;
    public static final List<Read> FINAL_READS = new ArrayList<>();
    public static final StringBuilder HEADER = new StringBuilder();
    public static final Map<Integer, List<Deletion>> DEL_MAP = new HashMap<>();
    public static int PRE_SUF_LENGTH = 5;
    public static boolean FIX;
}
