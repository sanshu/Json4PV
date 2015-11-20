package org.godziklab.json4pv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DSSPExtractor {

    /* Called from main JSON4PV servlet */
    public static String getDSSPFromZip(String pdbId, String zipLocation) {
        ZipFile zip;
        try {
            zip = new ZipFile(zipLocation);

            ZipEntry entry = zip.getEntry("merged" + File.separator + pdbId
                    + ".txt");

            if (entry == null) {
                zip.close();
                return "";
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(
                    zip.getInputStream(entry)));
            String line;
            boolean found = false;
            while ((line = input.readLine()) != null) {
                if (line.startsWith(">" + pdbId)) {
                    found = true;
                } else if (found) {
                    input.close();
                    return line;
                }
            }
            input.close();
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * all other functions are not used on server side *
     */
    public static String getSSandDis(String pdb, String chain, String dir,
            String chainSep) {
        return getSSandDis(pdb + chainSep + chain, dir);
    }

    public static String getSSandDis(String pdbAndChain, String dir) {
        String res = "";
        StringBuilder ssb = new StringBuilder();
        StringBuilder dsb = new StringBuilder();
        BufferedReader input;

        try {
            //   String pdbId = pdbAndChain.toUpperCase();
            //   pdbId = pdbId.substring(0, 4) + ":" + pdbId.substring(4);

            input = new BufferedReader(new FileReader(dir + File.separator
                    + pdbAndChain//.toUpperCase()
            ));

            String line;
            boolean foundSS = false, foundDIS = false, h = false;

            while ((line = input.readLine()) != null) {
                h = line.startsWith(">");
                if (h) {
                    foundSS = false;
                    foundDIS = false;
                }

                if (line.indexOf(":secstr") > 0) {
                    foundSS = true;
                    foundDIS = false;
                } else if (line.indexOf(":disorder") > 0) {
                    foundDIS = true;
                    foundSS = false;
                } else if (foundSS) {
                    ssb.append(line);
                } else if (foundDIS) {
                    dsb.append(line);
                }
            }
            res = DSSPExtractor.mergeSSandDiso(ssb.toString(), dsb.toString(),
                    pdbAndChain);
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private static int overCount = 0;
    private static List<String> overlapped = new ArrayList<String>();

    private static String mergeSSandDiso(String ss, String diso, String pdb) {
        if (diso.isEmpty()) {
            return ss;
        }
        boolean overlaps = false;
        StringBuilder res = new StringBuilder(ss);
        for (int index = 0; index < diso.length(); index++) {
            if (diso.charAt(index) == 'X') {
                if (ss.charAt(index) != ' ') {
                    overlaps = true;
                }
                res.setCharAt(index, 'X');
            }
        }
        if (overlaps) {
            overCount++;
            overlapped.add(pdb + "\n" + ss + "\n" + diso);
        }
        return res.toString();
    }

    private static String pdbFromHeaderNoChain(String h) {
        int j = h.indexOf(":");
        return h.substring(1, j);
    }

    private static String pdbFromHeader(String h) {
        int j = h.lastIndexOf(":");
        return h.substring(1, j);
    }

    private static void splitFile(String file, String outDir, boolean useChain,
            String chainSep, boolean overwrite) throws IOException {
        BufferedReader input;
        FileWriter output;

        input = new BufferedReader(new FileReader(file));
        String line, pdb = "";

        StringBuilder ssb = new StringBuilder();

        int i = 0;

        File outFolder = new File(outDir);
        HashSet<File> exitingFiles = new HashSet<File>(Arrays.asList(outFolder.listFiles()));

        while ((line = input.readLine()) != null) {

            if (line.startsWith(">")) {
                i++;
                if (i % 50000 == 0) {
                    System.out.println("Pdb # " + i + "... ");
                }
                if (!pdb.isEmpty()) {
                    File ofile = new File(outDir + pdb);
                    if (overwrite) {
                        output = new FileWriter(ofile, true);
                        output.append(ssb.toString() + "\n");
                        output.close();
                    } else if (!exitingFiles.contains(ofile)) {
                        output = new FileWriter(ofile, true);
                        output.append(ssb.toString() + "\n");
                        output.close();
                    }
                    ssb = new StringBuilder();
                } else {
                    ssb = new StringBuilder();
                }
                ssb.append(line);
                ssb.append("\n");

                if (useChain) {
                    pdb = DSSPExtractor.pdbFromHeader(line);
                } else {
                    pdb = DSSPExtractor.pdbFromHeaderNoChain(line);
                }
                pdb = adjustCase(pdb, chainSep);
            } else {
                ssb.append(line);
            }
        }
        input.close();
    }

    private static String adjustCase(String pdb, String chainSep) {
        String pdbid = pdb.substring(0, 4).toLowerCase();
        String chain = "";
        if (pdb.length() > 5) {
            chain = pdb.substring(5);
        }

        if (chain.isEmpty()) {
            return pdbid;
        } else {
            return pdbid + chainSep + chain;
        }

    }

    /**
     * Parses DSSP files loaded from PDB http://www.rcsb.org/pdb/files/ss.txt.gz
     * and http://www.rcsb.org/pdb/files/ss_dis.txt.gz Merges SS and DISO
     * predictions in to a single file.
     *
     * @param base folder in which ss_dis.txt file is located
     */
    public static void prepareData(String base, String chainSeparator) {
        String mainFile = base + File.separator + "ss_dis.txt";
        String mainFile2 = base + File.separator + "ss.txt";
        String zipResult = base + File.separator + "merged.zip";
        String split = base + File.separator + "split" + File.separator;

        File fs = new File(split);
        if (fs.exists()) {
            fs.delete();
        }

        fs.mkdirs();
        String merged = base + File.separator + "merged" + File.separator;
        fs = new File(merged);
        if (fs.exists()) {
            fs.delete();
        }
        fs.mkdirs();
        try {
            // split main files into separate files containing data for specific
            // pdb and chain

            File folder = new File(split);

            System.out.println("Splitting main file...");
            DSSPExtractor
                    .splitFile(mainFile, split, true, chainSeparator, true);

            int countOne = folder.listFiles().length;
            System.out.println("round1 :" + countOne);

            System.out.println("Splitting main file 2...");
            DSSPExtractor.splitFile(mainFile2, split, true, chainSeparator,
                    false);

            int countTwo = folder.listFiles().length;
            System.out.println("round2 :" + countTwo);
            System.out.println("extra :" + (countTwo - countOne));

            DSSPExtractor.overCount = 0;
            DSSPExtractor.overlapped = new ArrayList<String>();

            // now we have bunch of files to process, will write them to zip
            System.out.println("Creating zip " + zipResult + "...");

            File[] files = folder.listFiles();

            for (int i = 0; i < files.length; i++) {
                if (i % 1000 == 0) {
                    System.out.println("Adding file: " + i + " out of "
                            + files.length);
                }

                String pdb = files[i].getName(); //
                String mergedS = ">" + pdb + "\n"
                        + DSSPExtractor.getSSandDis(pdb, split) + "\n";

                // because of the difference in versions we need to create zip
                // manually
                BufferedWriter w1 = new BufferedWriter(new FileWriter(merged
                        + pdb));// + ".txt"));
                w1.append(mergedS + "\n");
                w1.close();
            }
            /* just a count of possible overlaps */
            if (overCount > 0) {
                BufferedWriter w2 = new BufferedWriter(new FileWriter(base
                        + File.separator + "ss_diso_overlaps.txt"));

                for (String st : overlapped) {
                    w2.append(st + "\n");
                }
                w2.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        for (String s : args) {
            System.out.println(s);
        }
        if (args.length > 0) {
            DSSPExtractor.prepareData(args[0], "");
        } else {
            // FOR FSN do not use chain separator
            DSSPExtractor.prepareData("/Users/msedova/Data/dssp", "");
            // For XtalPred results use ":"
            // DSSPExtractor.prepareData("/Users/msedova/Data/dssp", ";");
            System.out
                    .println("DONE\nDon't forget to copy merged.zip into war/data/");
            // String r = DSSPExtractor.getSSandDis("9XIM", "A",
            // "/Users/msedova/Downloads/dssp/");
        }
    }

}
