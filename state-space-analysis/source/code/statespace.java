import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class statespace {

    static class Trans {
        int src;
        String lbl;
        int time;
        int ctr;
        FractionNumber prb;
        Double prbFinal;
        FractionNumber actionDelayProb;
        int dst;
        Map <String, Integer> delayForAction;
        boolean isDelayTrans;
        public Trans(int src, String lbl, int tm, int dst) {
            this.src = src;
            this.lbl = lbl;
            this.time = tm;
            this.ctr = 0;
            this.prb = new FractionNumber(1, 1);
            this.prbFinal = 1.0;
            this.actionDelayProb = new FractionNumber(1, 1);
            this.dst = dst;
            this.delayForAction = new HashMap<String, Integer>();
            this.isDelayTrans = false;
        }
        public Trans(int src, String lbl, int tm, int dst, FractionNumber prb) {
            this.src = src;
            this.lbl = lbl;
            this.time = tm;
            this.ctr = 0;
            this.prb = prb;
            this.prbFinal = 1.0;
            this.actionDelayProb = new FractionNumber(1, 1);
            this.dst = dst;
            this.delayForAction = new HashMap<String, Integer>();
            this.isDelayTrans = false;
        }
        public Trans(int src, String lbl, int tm, int dst, int ctr, FractionNumber prb) {
            this.src = src;
            this.lbl = lbl;
            this.time = tm;
            this.ctr = ctr;
            this.prb = prb;
            this.prbFinal = 1.0;
            this.actionDelayProb = new FractionNumber(1, 1);
            this.dst = dst;
            this.delayForAction = new HashMap<String, Integer>();
            this.isDelayTrans = false;
        }
        public Trans(int src, String lbl, int tm, int dst, Double prbFinal) {
            this.src = src;
            this.lbl = lbl;
            this.time = tm;
            this.ctr = 0;
            this.prb = new FractionNumber(1, 1);
            this.prbFinal = prbFinal;
            this.actionDelayProb = new FractionNumber(1, 1);
            this.dst = dst;
            this.delayForAction = new HashMap<String, Integer>();
            this.isDelayTrans = false;
        }
        public void ctrUp () {
            ctr++;
        }
        public void prbComp(int ctrState) {
            prb = new FractionNumber(ctr, ctrState);
        }
        public String printTrans() {
            return "(" + src + ", " + getTimeLabel() + "; prob " + prbFinal + ", " + dst + ")";
        }
        public String asKey() {
            return "(" + src + ", " + getTimeLabel() + ", " + dst + ")";
        }
        public boolean compareTrans(Trans other) {
            if (src == other.src && lbl.equals(other.lbl) && time == other.time && ctr == other.ctr && prb == other.prb && dst == other.dst) {
                return true;
            } else {
                return false;
            }
        }
        public String getTimeLabel () {
            if (time > 0) {
                return lbl + " !" + time;
            } else {
                return lbl;
            }
        }
    }

    static class FractionNumber {
        long up;
        long down;
        public FractionNumber(long up, long down) {
            this.up = up;
            this.down = down;
        }
        public String getFractionString () {
            return up+"/"+down;
        }
        public Double getFloat() {
            return (double) up/down;
        }
        public long gcd(long a, long b) {
            return b == 0 ? a : gcd(b, a % b);
        }
        public void simplify() {
            long a  = up;
            long b = down;
            long gcd = gcd(a, b);
            up = a/gcd;
            down = b/gcd;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 4) {
            System.out.println("Parameters: <source tpts> <min,max probabilities> <max paths> <target tptps>");
            return;
        }
        Map <Integer, Set <Trans>> inTPTS = new HashMap <Integer, Set <Trans>>();
        String fileTPTS = args[0];
        Double prbMin = Double.parseDouble(args[1].split(",")[0]);
        Double prbMax = Double.parseDouble(args[1].split(",")[1]);
        int maxPaths = Integer.parseInt(args[2]);
        ArrayList <ArrayList <Trans>> paths = new ArrayList<ArrayList <Trans>>();
        ArrayList <Map <Integer, Set <Trans>>> resultTPTSsIn = new ArrayList<Map <Integer, Set <Trans>>>();
        Map <Integer, Set <Trans>> outTPTS = new HashMap <Integer, Set <Trans>>();
        ArrayList <Map <Integer, Set <Trans>>> resultTPTSsOut = new ArrayList<Map <Integer, Set <Trans>>>();
        String fileTPTSOut = args[3];
        buildTPTS(inTPTS, fileTPTS);
        extractPaths(paths, inTPTS, prbMin, prbMax, maxPaths);
        getTPTSs(resultTPTSsIn, paths, inTPTS);
        writeTPTSDots(resultTPTSsIn, fileTPTS);
        buildTPTS(outTPTS, fileTPTSOut);
        getTPTSs(resultTPTSsOut, paths, outTPTS);
        writeTPTSDots(resultTPTSsOut, fileTPTSOut);
    }

    public static String buildTPTS(Map<Integer, Set<Trans>> inTPTS, String fileLTS)
    throws FileNotFoundException, IOException {
        String propMeta = "";
        int tmpSrc;
        String tmpLbl = "";
        Double tmpLblProb;
        String tmpLblAct = "";
        int tmpLblTime = 0;
        int tmpDst;
        Set <Trans> tmpTrans;
        String[] arrLines;
        Set <Trans> tmpTransIns;
        Trans tmpNewTrans;
        try(BufferedReader br = new BufferedReader(new FileReader(fileLTS + ".aut"))) {
            String line = br.readLine();
            propMeta = line;
            while (true) {
                line = br.readLine();
                if (line != null){
                    arrLines = line.replace(" ", "").replace("(", "").replace(")", "").replace("\"", "").split(",");
                    tmpSrc = Integer.parseInt(arrLines[0]);
                    tmpLbl = arrLines[1].split(";")[0];
                    tmpLblProb = Double.parseDouble(arrLines[1].split(";")[1].replace("prob", ""));
                    tmpDst = Integer.parseInt(arrLines[2]);
                    tmpTrans = new HashSet<Trans>();  
                    if (inTPTS.containsKey(tmpSrc)) {
                        tmpTrans.addAll(inTPTS.get(tmpSrc));
                    }
                    tmpLblAct = tmpLbl.replace("!", "_").split("_")[0];
                    if (tmpLblAct.equals("Time")) {
                        if (!tmpLbl.replace("!", "_").split("_")[1].equals("oo")) {
                            tmpLblTime = Integer.parseInt(tmpLbl.replace("!", "_").split("_")[1]);
                        } else {
                            tmpLblTime = 1;
                        }
                        tmpLbl = tmpLblAct;
                    } else {
                        tmpLblTime = 0;
                    }
                    tmpNewTrans = new Trans(tmpSrc, tmpLblAct, tmpLblTime, tmpDst, tmpLblProb);
                    tmpTrans.add(tmpNewTrans);
                    inTPTS.put(tmpSrc, tmpTrans);
                    if (!inTPTS.containsKey(tmpDst)) {
                        inTPTS.put(tmpDst, new HashSet<Trans>());
                    }

                    tmpTransIns = new HashSet<Trans>();
                    tmpTransIns.add(tmpNewTrans);
                } else { break; }
            }
        }
        return propMeta;
    }

    public static void extractPaths(ArrayList <ArrayList <Trans>> paths, Map<Integer, Set<Trans>> inTPTS, Double prbMin, Double prbMax, int maxPaths) {
        extractPathsTraversal(paths, inTPTS, 0, new HashSet<Integer>(), prbMin, prbMax, new ArrayList<Trans>(), maxPaths);
    }

    public static void extractPathsTraversal(ArrayList <ArrayList <Trans>> paths, Map<Integer, Set<Trans>> inTPTS,
    int cState, Set <Integer> visited, Double prbMin, Double prbMax, ArrayList <Trans> tmpPath, int maxPaths) {
        if (tmpPath.size() > 0) {
            if (tmpPath.getLast().prbFinal >= prbMin && tmpPath.getLast().prbFinal <= prbMax) {
                ArrayList <Trans> newPath = new ArrayList<Trans>();
                for (Trans tr : tmpPath) {
                    Trans newTrans = new Trans(tr.src, tr.lbl, tr.time, tr.dst, tr.prbFinal);
                    newPath.add(newTrans);
                }
                paths.add(newPath);
                return;
            }
        }
        if (paths.size() == maxPaths) {
            return;
        }
        if (visited.contains(cState)) {
            return;
        }
        visited.add(cState);
        for (Trans tr : inTPTS.get(cState)) {
            tmpPath.add(tr);
            extractPathsTraversal(paths, inTPTS, tr.dst, visited, prbMin, prbMax, tmpPath, maxPaths);
            if (paths.size() == maxPaths) {
                return;
            }
            tmpPath.remove(tr);
        }
        visited.remove(cState);
    }

    public static void getTPTSs (ArrayList <Map <Integer, Set <Trans>>> resultTPTSs,
    ArrayList <ArrayList <Trans>> paths, Map <Integer, Set <Trans>> outTPTS) {
        Map <Integer, Set <Trans>> tmpResultTPTS;
        int cState;
        for (ArrayList<Trans> path : paths) {
            tmpResultTPTS = new HashMap<Integer, Set <Trans>>();
            cState = 0;
            for (Trans trPath : path) {
                for (Trans trTPTS : outTPTS.get(cState)) {
                    if (trPath.lbl.equals(trTPTS.lbl) && trPath.time == trTPTS.time) {
                        tmpResultTPTS.put(cState, outTPTS.get(cState));
                        cState = trTPTS.dst;
                        break;
                    }
                }
            }
            resultTPTSs.add(tmpResultTPTS);
        }
    }

    public static void writeTPTSDots(ArrayList <Map <Integer, Set <Trans>>> resultTPTSs, String fileName) throws InterruptedException {
        new File("results").mkdirs();
        int idx = 1;
        for (Map<Integer,Set<Trans>> tpts : resultTPTSs) {
            try {
                FileWriter myWriter = new FileWriter("results/" + fileName + "-" + idx + ".dot");
                myWriter.write("digraph BCG {\n" + //
                                "size = \"7, 10.5\";\n" + //
                                "center = TRUE;\n" + //
                                "node [shape = circle];\n" + //
                                "0 [peripheries = 2];\n");
                for (int st : tpts.keySet()) {
                    for (Trans tr : tpts.get(st)) {
                        if (tr.time > 0) {
                            myWriter.write(tr.src + " -> " + tr.dst + "  [label = \"" + tr.lbl + " !" + tr.time + "; prob " + tr.prbFinal + "\"];\n");
                        } else {
                            myWriter.write(tr.src + " -> " + tr.dst + "  [label = \"" + tr.lbl + "; prob " + tr.prbFinal + "\"];\n");
                        }
                    }
                }
                myWriter.write("}");
                myWriter.close();
                System.out.println("TPTS created: results/" + fileName + "-" + idx + ".dot");
                executeCommands("dot -Tpdf results/" + fileName + "-" + idx + ".dot > results/" + fileName + "-" + idx + ".pdf");
                idx++;
            } catch (IOException e) {
                System.out.println("TPTS creation error!");
                e.printStackTrace();
            }
        }
    }

    public static void printTPTS (Map <Integer, Set <Trans>> inTPTS) {
        System.out.println("\n----- Start printing TPTS -----");
        for (int st : inTPTS.keySet()) {
            for (Trans tr : inTPTS.get(st)) {
                System.out.println(tr.printTrans());
            }
        }
        System.out.println("----- End printing TPTS -----");
    }

    public static void printTPTSs (ArrayList <Map <Integer, Set <Trans>>> TPTSs) {
        System.out.println("\n----- Start printing TPTSs -----");
        int idx = 1;
        for (Map<Integer,Set<Trans>> inTPTS : TPTSs) {
            System.out.println("TPTS " + idx);
            idx++;
            for (int st : inTPTS.keySet()) {
                for (Trans tr : inTPTS.get(st)) {
                    System.out.println(tr.printTrans());
                }
            }
            System.out.println();
        }
        System.out.println("----- End printing TPTSs -----");
    }

    public static void printPaths (ArrayList <ArrayList <Trans>> paths) {
        System.out.println("\n----- Start printing paths -----");
        int idx = 1;
        for (ArrayList <Trans> path : paths) {
            System.out.println("Path " + idx);
            idx++;
            for (Trans tr : path) {
                System.out.println(tr.printTrans());
            }
            System.out.println();
        }
        System.out.println("----- End printing paths -----");
    }

    public static void executeCommands(String command) throws IOException, InterruptedException {
        File tempScript = createTempScript(command);
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } finally {
            tempScript.delete();
        }
    }

    public static File createTempScript(String command) throws IOException {
        File tempScript = File.createTempFile("script", null);
        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(tempScript));
        PrintWriter printWriter = new PrintWriter(streamWriter);
        printWriter.println("#!/bin/bash");
        printWriter.println(command);
        printWriter.close();
        return tempScript;
    }
}
