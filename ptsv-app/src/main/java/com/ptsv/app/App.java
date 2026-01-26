package com.ptsv.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.parser.client.SyntaxError;
import org.matheclipse.parser.client.math.MathException;

/**
 * Hello world!
 */
public class App {

    static class Trans {
        int src;
        String lbl;
        int time;
        int ctr;
        FractionNumber prb;
        FractionNumber eventDelayProb;
        int dst;
        Map <String, Integer> delayForEvent;
        boolean isDelayTrans;
        public Trans(int src, String lbl, int tm, int dst) {
            this.src = src;
            this.lbl = lbl;
            this.time = tm;
            this.ctr = 0;
            this.prb = new FractionNumber(1, 1);
            this.eventDelayProb = new FractionNumber(1, 1);
            this.dst = dst;
            this.delayForEvent = new HashMap<String, Integer>();
            this.isDelayTrans = false;
        }
        public Trans(int src, String lbl, int tm, int dst, FractionNumber prb) {
            this.src = src;
            this.lbl = lbl;
            this.time = tm;
            this.ctr = 0;
            this.prb = prb;
            this.eventDelayProb = new FractionNumber(1, 1);
            this.dst = dst;
            this.delayForEvent = new HashMap<String, Integer>();
            this.isDelayTrans = false;
        }
        public Trans(int src, String lbl, int tm, int dst, int ctr, FractionNumber prb) {
            this.src = src;
            this.lbl = lbl;
            this.time = tm;
            this.ctr = ctr;
            this.prb = prb;
            this.eventDelayProb = new FractionNumber(1, 1);
            this.dst = dst;
            this.delayForEvent = new HashMap<String, Integer>();
            this.isDelayTrans = false;
        }
        public void ctrUp () {
            ctr++;
        }
        public void prbComp(int ctrState) {
            prb = new FractionNumber(ctr, ctrState);
        }
        public String printTrans() {
            return "(" + src + ", " + getTimeLabel() + "; prob " + prb + ", " + dst + ")";
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

    static class TransPair {
        Trans trEvent;
        Trans trTime;
    }

    static class TransPossibility {
        String event;
        Map <Integer, Set <ArrayList <Trans>>> transPathsDelay;
        public TransPossibility(String event, Map <Integer, Set <ArrayList <Trans>>> transPathsDelay) {
            this.event = event;
            this.transPathsDelay = transPathsDelay;
        }
    }
    
    static class FractionNumber {
        int up;
        int down;
        // int intgr;
        public FractionNumber(int up, int down) {
            this.up = up;
            this.down = down;
            // this.intgr = -1;
        }
        // public FractionNumber(int intgr) {
        //     this.up = -1;
        //     this.down = -1;
        //     this.intgr = intgr;
        // }
        public String getFractionString () {
            return up+"/"+down;
            // if (intgr == -1) {
            //     return up+"/"+down;
            // } else {
            //     return ""+intgr;
            // }
        }
        public Double getFloat() {
            return (double) up/down;
        }

        public int gcd(int a, int b) {
            return b == 0 ? a : gcd(b, a % b);
        }

        public void simplify() {
            int a  = up;
            int b = down;
            int gcd = gcd(a, b);
            up = a/gcd;
            down = b/gcd;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // testEqs();
        if (args.length == 0) {
            System.out.println("Missing IF model name!");
            return;
        } else if (args[0].equals("mapping")) {
            if (args.length == 1) {
                System.out.println("Missing IF model name! ");
            } else {
                String ifModel = "";
                if (args[1].split("\\.").length > 0) {
                    ifModel = args[1].split("\\.")[0];
                } else {
                    ifModel = args[1];
                }

                bashCompileLTS(ifModel, "global");
                modLTS(ifModel);
                bashReduceLTS(ifModel, "global", "strong");
                ArrayList <String> taNames = new ArrayList<String>();
                bashIndividualLTSs(ifModel, taNames);
                computeMappingOfStates(ifModel + "-min", taNames);
            }
        } else if (args.length > 0) {
            String ifModel = "";
            if (args[0].split("\\.").length > 0) {
                ifModel = args[0].split("\\.")[0];
            } else {
                ifModel = args[0];
            }
            
            Map <String, String> distribution = new HashMap<String, String>();
            ArrayList <String> chain = new ArrayList<String>();
            ArrayList <Integer> minMax = new ArrayList<Integer>();
            
            System.out.println("\n!!!!---------------- Start analysis of IF model: " + ifModel + "----------------!!!!");
            String reduction = getModelInfo(distribution, chain, minMax, ifModel);
            printDistribution(distribution);
            
            System.out.println("\n<<<<<<<<<< Start generating the global TLTS");
            long startTime = System.currentTimeMillis();
            bashCompileLTS(ifModel, "global");
            modLTS(ifModel);
            bashReduceLTS(ifModel, "global", reduction);
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println("\n>>>>>>>>>> Finish generating the global TLTS (" + elapsedTime + "ms)");
            
            if (args.length == 1) {
                System.out.println("\n!!!!!!!! Computing TPTS of " + args[0] + " according to specified distributions\n");
                ArrayList <String> taNames = new ArrayList<String>();
                System.out.println("\n<<<<<<<<<< Start generating the local TLTSs");
                startTime = System.currentTimeMillis();
                bashIndividualLTSs(ifModel, taNames);
                stopTime = System.currentTimeMillis();
                elapsedTime = stopTime - startTime;
                System.out.println("\n>>>>>>>>>> Finish generating the local TLTSs (" + elapsedTime + "ms)");
                computePTSbyDistribution(ifModel + "-min", taNames, distribution);
            } else if (args.length >= 2) {
                System.out.println("\n!!!!!!!! Computing TPTS of " + args[0] + " according to traces in folder " + args[1]);
                computePTSbyTraces(ifModel + "-min", args[1]);
            }

            if (chain.size() > 0) {
                System.out.println("\n<<<<<<<<<< Start analysing reaction time probabilities");
                startTime = System.currentTimeMillis();
                System.out.print("Specified chain of actions: ");
                String delim = "";
                for (String ch : chain) { System.out.print(delim + ch); delim = ", "; }
                System.out.println("\nSpecified bounds: " + minMax.get(0) + " - " + minMax.get(1));
                if (args.length >= 2) {
                    analyzeChain(ifModel + "-min-" + args[1] + "-rem-pts", chain, minMax);
                } else {
                    analyzeChain(ifModel + "-min-pts", chain, minMax);
                }
                stopTime = System.currentTimeMillis();
                elapsedTime = stopTime - startTime;
                System.out.println("\n>>>>>>>>>> Finish analysing reaction time probabilities (" + elapsedTime + "ms)");
            }
        }
    }

    public static void testEqs() {
        // String solverEqs ="{a/b==1/2, a+b==5/8}";
        // String solverVars = "{a, b}"; 

        String solverEqs = "{" +
        "b7*c2 + b5*a9*b8 + b7*c1*b8 == 1/2," + 
        "b7*c2*a3*a5*a7 + b6*b3*a3*a5*a7 == 1/4," +
        "a9 + b1 == 1," +
        "b7*c2*a2 + b6*b3*a2 + b7*c1 == 1/4," +
        "b5 + b6 + b7 == 1," +
        "b8 == 1," +
        "a2 + a3 == 1," +
        "a7 == 1," +
        "b2 + b3 == 1," +
        "c1/c2 == 1/2," +
        "b5/b6 == 1/2," +
        "b7*c2*a3*a4 + b6*b3*a3*a4 == 1/4," +
        "b6 + b5*b1 == 1/2," +
        "a4 + a5 == 1," +
        "b5 + b6*b2 == 1/4," +
        // "b5 == 5/24," +
        "b7 == 3/8," +
        "c1 + c2 == 1}";
        String solverVars = "{c1, c2, b1, b2, a2, b3, a3, b5, a4, b6, a5, b7, a7, b8, a9}"; 

        Map <String, FractionNumber> solverResult = new HashMap<String, FractionNumber>();
        solveEqs(solverEqs, solverVars, solverResult);

        // Set <String> newEqstmp = new HashSet<String>();
        // newEqstmp.add("b5/b6 == 1/2");
        // newEqstmp.add("c1/c2 == 1/2");
        // newEqstmp.add("b5 + b6 == 5/8");
        // newEqstmp.add("b5 == 5/24");
        // newEqstmp.add("b6 == 5/12");
        // newEqstmp.addAll(equations.get(9));
        // equations.put(9, newEqstmp);
    }

    public static String getModelInfo (Map <String, String> distribution, ArrayList <String> chain,
    ArrayList <Integer> minMax, String ifModel)
    throws FileNotFoundException, IOException, InterruptedException {
        String reduc = "";
        try (BufferedReader br = new BufferedReader(new FileReader(ifModel+".if"))) {
            String line;
            String event;
            String disType;
            String chainStr[];
            String minMaxStr[];
            while ((line = br.readLine()) != null) {
                if (line.contains("[") && (line.contains("custom") || line.contains("uniform") || line.contains("binomial"))) {
                    event = StringUtils.substringBetween(line, "\"", "\"");
                    disType = StringUtils.substringBetween(line, "[", "]");
                    distribution.put(event, disType);
                } else if (line.contains("[") && line.contains("chain")) {
                    chainStr = StringUtils.substringBetween(line, ":", ";").replace(" ", "").split(",");
                    minMaxStr = StringUtils.substringBetween(line, ";", "]").replace(" ", "").split("-");
                    for (String ch : chainStr) {
                        chain.add(ch);
                    }
                    for (String mm : minMaxStr) {
                        minMax.add(Integer.parseInt(mm));
                    }
                } else if (line.contains("[") && line.contains("strong")) {
                    reduc = "strong";
                }
            }
            String com = "sed 's/\\[[^]]*\\]//g'" + " " + ifModel + ".if > " + ifModel + "-stripped.if";
            executeCommands(com);
        }
        return reduc;
    }
    
    public static void computeMappingOfStates (String ifModel, ArrayList <String> taNames) throws FileNotFoundException, IOException, InterruptedException {
        ArrayList <Map <Integer, Set <Trans>>> taLTSs = new ArrayList<Map <Integer, Set <Trans>>>();
        Map <Integer, Set <Trans>> taLTS;
        Map <Integer, Set <Trans>> statesIns;
        for (String taName : taNames) {
            taLTS = new HashMap <Integer, Set <Trans>>();
            statesIns = new HashMap <Integer, Set <Trans>>();
            buildLTS(taLTS, taName, statesIns);
            taLTSs.add(taLTS);
        }
        // printLTSs(taLTSs);
        Map <Integer, Set <Trans>> inLTS = new HashMap <Integer, Set <Trans>>();
        Map <Integer, Set <Trans>> statesInsAll = new HashMap <Integer, Set <Trans>>();
        ArrayList <ArrayList <Trans>> paths = new ArrayList<ArrayList <Trans>>();
        Map <Integer, ArrayList<Set <Integer>>> mapping = new HashMap <Integer, ArrayList<Set <Integer>>>(); 
        buildLTS(inLTS, ifModel, statesInsAll);
        getPaths(paths, inLTS);
        // printPaths (paths);
        traverseToGetMapping(mapping, paths, taLTSs, inLTS);
        // printMapping(mapping);
        writeMapping(taLTSs, mapping, ifModel);
    }

    public static void traverseToGetMapping (Map <Integer, ArrayList<Set <Integer>>> mapping,
    ArrayList <ArrayList <Trans>> paths, ArrayList <Map <Integer, Set <Trans>>> taLTSs,
    Map <Integer, Set <Trans>> inLTS) {
        ArrayList <Set <Integer>> tmpLocalStates;
        Set <Integer> tmpStates;
        // int idxPath = 1;
        int idxLTS;
        ArrayList <Integer> cStates = new ArrayList<Integer>();
        ArrayList <Integer> tmpTimes = new ArrayList<Integer>();
        int k;
        for (int st : inLTS.keySet()) {
            tmpLocalStates = new ArrayList<Set <Integer>>();
            for (int i = 0; i < taLTSs.size(); i++) {
                tmpStates = new HashSet<Integer>();
                if (st == 0) {
                    tmpStates.add(0);
                }
                tmpLocalStates.add(tmpStates);
            }
            mapping.put(st, tmpLocalStates);
        }
        for (int i = 0; i < taLTSs.size(); i++) {
            cStates.add(0);
            tmpTimes.add(0);
        }
        for (ArrayList <Trans> path : paths) {
            // System.out.println("\nPath " + idxPath);
            // idxPath++;
            for (int i = 0; i < taLTSs.size(); i++) {
                cStates.set(i, 0);
                tmpTimes.set(i, 0);
            }
            for (Trans trPath : path) {
                // System.out.println(trPath.asKey());
                idxLTS = 0;
                for (Map <Integer, Set <Trans>> taLTS : taLTSs) {
                    for (Trans trLTS : taLTS.get(cStates.get(idxLTS))) {
                        if (trPath.lbl.equals(trLTS.lbl) && !trPath.lbl.equals("Time")) {
                            cStates.set(idxLTS, trLTS.dst);
                            // System.out.println("Local LTS " + idxLTS + ": " + trLTS.asKey());
                            break;
                        } else if (trPath.lbl.equals("Time") && trLTS.lbl.equals("Time")) {
                            tmpTimes.set(idxLTS, tmpTimes.get(idxLTS) + trPath.time);
                            if (tmpTimes.get(idxLTS) == trLTS.time) {
                                tmpTimes.set(idxLTS, 0);
                                cStates.set(idxLTS, trLTS.dst);
                                // System.out.println("Local LTS " + idxLTS + ": " + trLTS.asKey());
                            }
                            break;
                        }
                    }
                    idxLTS++;
                }
                tmpLocalStates = new ArrayList<Set <Integer>>();
                tmpLocalStates.addAll(mapping.get(trPath.dst));
                k = 0;
                for (Integer st : cStates) {
                    tmpLocalStates.get(k).add(st);
                    k++;
                }
                // System.out.println("Traversing " + trPath.asKey() + ", mapping " + trPath.dst + " to:");
                // k = 0;
                // for (Set <Integer> states : tmpLocalStates) {
                //     k++;
                //     System.out.println("LTS " + k);
                //     for (Integer st : states) {
                //         System.out.println(st);
                //     }
                // }
                // System.out.println();
                mapping.put(trPath.dst, tmpLocalStates);
            }
        }
    }

    public static void computePTSbyDistribution(String ifModel,
    ArrayList <String> taNames, Map <String, String> disTypes)
    throws FileNotFoundException, IOException, InterruptedException {
        System.out.println("\n<<<<<<<<<< Start collecting equations");
        Map <Integer, Set <Trans>> inLTS = new HashMap <Integer, Set <Trans>>();
        long startTime = System.currentTimeMillis();
        ArrayList <Map <Integer, Set <Trans>>> taLTSs = new ArrayList<Map <Integer, Set <Trans>>>();
        Map <Integer, Set <Trans>> taLTS;
        Map <Integer, Set <Trans>> statesIns;
        Map <String, Set <Integer>> allEvents;
        Map <String, ArrayList<FractionNumber>> eventProbTriggerMap = new HashMap<String, ArrayList<FractionNumber>>();
        Map <String, ArrayList<FractionNumber>> eventProbTimeMap = new HashMap<String, ArrayList<FractionNumber>>();
        Map <String, FractionNumber> eventProbMap = new HashMap<String, FractionNumber>();
        for (String taName : taNames) {
            taLTS = new HashMap <Integer, Set <Trans>>();
            statesIns = new HashMap <Integer, Set <Trans>>();
            allEvents = new HashMap <String, Set <Integer>>();
            String header = buildLTS(taLTS, taName, statesIns);
            getAllEvents(allEvents, eventProbMap, taLTS, statesIns);
            // printAllEvents(allEvents, taName);
            startAssignProbs(taLTS, eventProbTriggerMap, eventProbTimeMap, allEvents, disTypes);
            writePTS(taLTS, taName, header);
            bashCreatePDF(taName + "-pts");
            taLTSs.add(taLTS);
        }
        simplifyMapFracs(eventProbTriggerMap);
        simplifyMapFracs(eventProbTimeMap);
        // printEventProbMap(eventProbTriggerMap, eventProbMap);
        // printEventProbTimeMap(eventProbTimeMap);
        Map <Integer, Set <Trans>> statesInsAll = new HashMap <Integer, Set <Trans>>();
        Map <String, Set <Integer>> eventStates = new HashMap <String, Set <Integer>>();
        Map <String, Map <Integer, Set <Integer>>> eventStatesNets = new HashMap <String, Map <Integer, Set <Integer>>>();
        Map <String, Map <Integer, Set <Set <Integer>>>> eqStartStates = new HashMap <String, Map <Integer, Set <Set <Integer>>>>();
        Map <Integer, Set <TransPossibility>> transPossibilities = new HashMap <Integer, Set <TransPossibility>>();
        Map <String, String> transVarMapping = new LinkedHashMap<String, String>();
        Map <Integer, Set <String>> equations = new HashMap<Integer, Set <String>>();
        Map <Integer, Set <String>> equationsUnmap = new HashMap<Integer, Set <String>>();
        Map <Integer, Set <String>> equationVars = new HashMap<Integer, Set <String>>();
        Map <String, FractionNumber> solverResults = new HashMap<String, FractionNumber>();
        Map <String, String> equationsPy = new HashMap <String, String>();
        String header = buildLTS(inLTS, ifModel, statesInsAll);
        annotateDelayTrans(inLTS);
        getEventStates(eventStates, inLTS);
        // printEventStates(eventStates);
        getEventStatesNets(eventStatesNets, eventStates, inLTS, statesInsAll);
        // printEventStateNets(eventStatesNets);
        getEventDelayProb(eventStatesNets, inLTS, eventProbTriggerMap);
        // printEventDelayProb(inLTS);
        getEventStateEqStart(eqStartStates, eventStatesNets, inLTS, statesInsAll);
        // printEqStarts(eqStartStates);
        getTransPossibilities(transPossibilities, eqStartStates, inLTS);
        // printTransNetPossibilities(transPossibilities);
        // printDelayTrans(inLTS);
        getTransVarMappingPy(transVarMapping, inLTS);
        // printTransVarMapping(transVarMapping);
        writeMappedLTS(inLTS, transVarMapping, ifModel, header);
        getEquations(equationsPy, equations, equationsUnmap, equationVars,
            transPossibilities, eventProbTriggerMap,
            eventProbMap, eventProbTimeMap, transVarMapping, inLTS);
        // printEquations(equations, equationVars);
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("\n>>>>>>>>>> Finish collecting equations (" + elapsedTime + "ms)");
        System.out.println("\n<<<<<<<<<< Start solving with sympy\n");
        startTime = System.currentTimeMillis();
        solveEquationsWithSympy(solverResults, ifModel, equationsPy, equationVars);
        stopTime = System.currentTimeMillis();
        elapsedTime = stopTime - startTime;
        System.out.println("\n>>>>>>>>>> Finish solving with sympy (" + elapsedTime + "ms)");
        // solveNetEquations(solverResults, equations, equationVars, transVarMapping);
        // printSolverResult(solverResults, transVarMapping);
        assignProbsToLTS(inLTS, transVarMapping, solverResults);
        writePTS(inLTS, ifModel, header);
        bashCreatePDF(ifModel +"-pts");
        writeDTMC(inLTS, ifModel +"-pts", header);
    }
    
    public static void analyzeChain(String modelName, ArrayList <String> chain,
    ArrayList <Integer> minMax) throws IOException, InterruptedException {
        Map <String, Double> mapSteady = new HashMap<String, Double>();
        Map <Integer, Double> timeProbabilities = new HashMap<Integer, Double>();
        executeCommands("($prism " + modelName + ".nm -ss) > " + modelName + "-steady.txt");
        System.out.println("Steady-state probabilities (using PRISM): " + modelName + "-steady.txt");
        String prefix = chain.get(0);
        int countPrefix  = writeIndexedPTS(modelName, prefix);
        getMapSteady(mapSteady, modelName, prefix);
        getVerdicts(modelName, chain, prefix, countPrefix, minMax.get(0), minMax.get(1));
        getTimeProbabilities(timeProbabilities, modelName, mapSteady, countPrefix);
        writeTimeProbabilities(timeProbabilities, modelName);
    }

    public static int writeIndexedPTS(String modelName, String prefix) throws IOException, InterruptedException {
        String line;
        ArrayList <String> lines = new ArrayList<String>(); 
        int idxPref = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(modelName+".aut"))) {
            while ((line = br.readLine()) != null) {
                if (line.contains(prefix)) {
                    idxPref++;
                    lines.add(line.replace(prefix, prefix + " !" + idxPref));
                } else {
                    lines.add(line);
                }
            }
        }
        try {
            FileWriter myWriter = new FileWriter(modelName + "-indexed.aut");
            for (String ln : lines) {
                myWriter.write(ln + "\n");
            }
            myWriter.close();
            executeCommands("bcg_io " + modelName + "-indexed.aut " + modelName + "-indexed.dot");
            executeCommands("dot -Tpdf " + modelName + "-indexed.dot > " + modelName + "-indexed.pdf");
            System.out.println("TPTS (indexed) created: " + modelName + "-indexed.aut");
        } catch (IOException e) {
            System.out.println("TPTS (indexed) creation error!");
            e.printStackTrace();
        }
        return idxPref;
    }

    public static void getMapSteady(Map <String, Double> mapSteadyPrefix,
    String modelName, String prefix) throws FileNotFoundException, IOException {
        Map <Integer, Double> mapSteadyState = new HashMap<Integer, Double>();
        String line;
        int st;
        Double stProb;
        boolean startMapping = false;
        try (BufferedReader br = new BufferedReader(new FileReader(modelName + "-steady.txt"))) {
            while ((line = br.readLine()) != null) {
                if (line.length() > 0) {
                    if (line.contains("Exporting steady-state")) {
                        startMapping = true;
                    } else if (startMapping) {
                        st = Integer.parseInt(line.split(":")[0]);
                        stProb = Double.parseDouble(line.split("=")[1]);
                        mapSteadyState.put(st, stProb);
                    }
                }
            }
        }

        int stLTS;
        String indexed;
        try (BufferedReader br = new BufferedReader(new FileReader(modelName + "-indexed.aut"))) {
            while ((line = br.readLine()) != null) {
                if (line.length() > 0) {
                    if (line.contains(prefix)) {
                        stLTS = Integer.parseInt(StringUtils.substringBetween(line, "(", ","));
                        indexed = StringUtils.substringBetween(line, "\"", ";");
                        if (mapSteadyState.containsKey(stLTS)) {
                            mapSteadyPrefix.put(indexed, mapSteadyState.get(stLTS));
                        } else {
                            mapSteadyPrefix.put(indexed, 0.0);
                        }
                    }
                }
            }
        }
    }

    public static void getVerdicts(String modelName, ArrayList <String> chain,
    String prefix, int numPrefix, int min, int max) throws InterruptedException, IOException {
        boolean first;
        int chainCtr;
        int prefCtr;
        int lowBoundCtr = min;
        executeCommands("echo -n \"\" > " + modelName + "\"-verdicts.txt\"");
        executeCommands("bcg_io " + modelName + "-indexed.aut " + modelName + "-indexed.bcg ");
        try {
            while (lowBoundCtr <= max) {
                System.out.println("Model checking for t = " + lowBoundCtr);
                prefCtr = 1;
                while (prefCtr <= numPrefix) {
                    // System.out.println("Model checking for time: " + lowBoundCtr + ", prefix: " + prefix + " !" + prefCtr);
                    FileWriter myWriter = new FileWriter(modelName + "-template.mcl");
                    myWriter.write("prob\n" + //
                                        "\t(not \"" + prefix + " !" + prefCtr + "\")* . \"" + prefix + " !" + prefCtr + "\" .\n" + //
                                        "\tloop (time, chain_counter, tmp: Nat := 0) : (res: Nat) in\n");
                    chainCtr = 0;
                    first = true;
                    for (String ch1 : chain) {
                        if (!ch1.equals(prefix)) {
                            if (first) {
                                myWriter.write("\t\tif (chain_counter = " + chainCtr + ") then\n" + //
                                                    "\t\t\t  ({Time ?tx:Nat}).continue(time + tx, chain_counter, tmp)\n" + //
                                                    "\t\t\t| ({Read ?st:Nat}).continue(time, chain_counter, tmp + st)\n");
                                first = false;
                            } else {
                                myWriter.write("\t\telsif (chain_counter = " + chainCtr + ") then\n" + //
                                                    "\t\t\t  ({Time ?tx:Nat}).continue(time + tx, chain_counter, tmp)\n" + //
                                                    "\t\t\t| ({Read ?st:Nat}).continue(time, chain_counter, tmp + st)\n");
                            }
                            for (String ch2 : chain) {
                                if (!ch2.equals(prefix)) {
                                    if (ch1.equals(ch2)) {
                                        myWriter.write("\t\t\t| (\"" + ch2 + "\").continue(time, chain_counter + 1, tmp)\n");
                                    } else {
                                        myWriter.write("\t\t\t| (\"" + ch2 + "\").continue(time, chain_counter, tmp)\n");
                                    }
                                }
                            }
                            chainCtr++;
                        }
                    }
                    myWriter.write("\t\telse exit (time)\n" + //
                                        "\t\tend if\n" + //
                                        "\tend loop .\n" + //
                                        "\tif (res <> " + lowBoundCtr + ") then false end if\n" + //
                                        "is >= ? 0\n" + //
                                        "end prob");
                    myWriter.close();
                    executeCommands("echo [" + lowBoundCtr + ", " + prefix + " !" + prefCtr +"] >> " + modelName + "-verdicts.txt");
                    executeCommands("bcg_open " + modelName + "-indexed.bcg evaluator5 "
                    + modelName + "-template.mcl >> " + modelName + "-verdicts.txt");
                    executeCommands("echo \"------------------\n\" >> " + modelName + "-verdicts.txt");
                    prefCtr++;
                }
                lowBoundCtr++;
            }
        } catch (IOException e) {
            System.out.println("MCL5 template creation error!");
            e.printStackTrace();
        }
    }

    public static void getTimeProbabilities(Map <Integer, Double> timeProbabilities,
    String modelName, Map <String, Double> mapSteady, int numPrefix) throws FileNotFoundException, IOException {
        String line;
        String prefix;
        int time = 0;
        double prob;
        boolean readProbNext = false;
        double tmpProbSteady = 0.0;
        double tmpProbTotal = 0.0;
        double tmpProbAvg;
        int ctrPref = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(modelName + "-verdicts.txt"))) {
            while ((line = br.readLine()) != null) {
                if (line.length() > 0) {
                    if (line.contains("[")) {
                        time = Integer.parseInt(StringUtils.substringBetween(line, "[", ","));
                        prefix = StringUtils.substringBetween(line, " ", "]");
                        tmpProbSteady = mapSteady.get(prefix);
                    } else if (line.contains("running")) {
                        readProbNext = true;
                    } else if (readProbNext) {
                        readProbNext = false;
                        prob = Double.parseDouble(line);
                        tmpProbTotal += prob * tmpProbSteady;
                        ctrPref++;
                        if (ctrPref == numPrefix) {
                            tmpProbAvg = tmpProbTotal / numPrefix;
                            timeProbabilities.put(time, tmpProbAvg);
                            ctrPref = 0;
                            tmpProbTotal = 0.0;
                        }
                    }
                }
            }
        }
        Double tmpTotalTimeProbs = 0.0;
        Double scaledProb;
        for (int key : timeProbabilities.keySet()) {
            tmpTotalTimeProbs += timeProbabilities.get(key);
        }
        for (int key : timeProbabilities.keySet()) {
            scaledProb = (timeProbabilities.get(key) / tmpTotalTimeProbs) * 1;
            timeProbabilities.put(key, scaledProb);
        }
    }

    public static void writeTimeProbabilities (Map <Integer, Double> timeProbabilities, String modelName) {
        try {
            FileWriter myWriter = new FileWriter(modelName + "-verdicts-final.txt");
            for (int time : timeProbabilities.keySet()) {
                myWriter.write(time + ": " + timeProbabilities.get(time) + "\n");
            }
            myWriter.close();
            System.out.println("Analysis results created: " + modelName + "-verdicts-final.txt");
        } catch (IOException e) {
            System.out.println("Analysis results creation error!");
            e.printStackTrace();
        }
    }

    public static void getAllEvents (Map <String, Set <Integer>> allEvents, Map <String, FractionNumber> eventProbMap,
    Map <Integer, Set <Trans>> inLTS, Map <Integer, Set <Trans>> statesIns) {
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (!tr.lbl.equals("Time") && !allEvents.containsKey(tr.lbl)) {
                    allEvents.put(tr.lbl, new HashSet<Integer>());
                    eventProbMap.put(tr.lbl, new FractionNumber(1, 1));
                }
            }
        }
        Set <Integer> startingStates;
        for (String event : allEvents.keySet()) {
            startingStates = new HashSet<Integer>();
            getStartingStates(startingStates, event, inLTS, statesIns);
            if (startingStates.size() == 0) {
                for (Integer st : inLTS.keySet()) {
                    for (Trans tr : inLTS.get(st)) {
                        if (tr.lbl.equals(event)) {
                            startingStates.add(st);
                        }
                    }
                }
            }
            allEvents.put(event, startingStates);
        }
    }
    
    public static void getStartingStates (Set <Integer> startingStates, String event, Map <Integer, Set <Trans>> inLTS,
    Map <Integer, Set <Trans>> statesIns) {
        int cState = 0;
        Set <Integer> visited = new HashSet<Integer>();
        traverseStartingStates(startingStates, cState, visited, event, inLTS, statesIns);
    }
    
    public static void getEventStates (Map <String, Set <Integer>> allEvents, Map <Integer, Set <Trans>> inLTS) {
        Queue <Integer> toVisit = new LinkedList<Integer>();
        Set <Integer> visited = new HashSet<Integer>();
        int visiting;
        Set <Integer> tmpStarts;

        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (!tr.lbl.equals("Time") && !allEvents.containsKey(tr.lbl)) {
                    allEvents.put(tr.lbl, new HashSet<Integer>());
                }
            }
        }

        for (String event : allEvents.keySet()) {
            toVisit.add(0);
            while (!toVisit.isEmpty()) {
                visiting = toVisit.poll();
                for (Trans tr : inLTS.get(visiting)) {
                    if (!visited.contains(tr.dst)) {
                        visited.add(tr.dst);
                        toVisit.add(tr.dst);
                    }
                    if (tr.lbl.equals(event)) {
                        tmpStarts = new HashSet<Integer>();
                        tmpStarts.addAll(allEvents.get(event));
                        tmpStarts.add(visiting);
                        allEvents.put(event, tmpStarts);
                    }
                }
            }
            visited.clear();
        }
    }

    public static void getEventStatesNets(Map <String, Map <Integer, Set <Integer>>> eventStatesNets,
    Map <String, Set <Integer>> eventStates, Map <Integer, Set <Trans>> inLTS,
    Map <Integer, Set <Trans>> statesInsAll) {
        // boolean firstState;
        // Set <Set <Integer>> stateNets;
        // Set <Integer> stateNet;
        // Set <Integer> stateNetNew;
        // boolean addedToNet;
        // for (String event : eventStates.keySet()) {
        //     firstState = true;
        //     stateNets = new HashSet<Set <Integer>>();
        //     for (int st1 : eventStates.get(event)) {
        //         if (firstState) {
        //             stateNet = new HashSet<Integer>();
        //             stateNet.add(st1);
        //             stateNets.add(stateNet);
        //             firstState = false;
        //         } else {
        //             addedToNet = false;
        //             loopnet:
        //             for (Set <Integer> net : stateNets) {
        //                 for (int st2 : net) {
        //                     if (checkStateNetwork(st1, st2, inLTS, statesInsAll, event)) {
        //                         net.add(st1);
        //                         addedToNet = true;
        //                         break loopnet;
        //                     }
        //                 }
        //             }
        //             if (!addedToNet) {
        //                 stateNetNew = new HashSet<Integer>();
        //                 stateNetNew.add(st1);
        //                 stateNets.add(stateNetNew);
        //             }
        //         }
        //     }
        //     eventStatesNets.put(event, stateNets);
        // }

        Set <Integer> netRoots;
        Map <Integer, Set <Integer>> MapStateNet;
        Set <Integer> stateNet;
        for (String event : eventStates.keySet()) {
            netRoots = new HashSet<Integer>();
            for (int st : eventStates.get(event)) {
                if (checkNetRoot(st, statesInsAll, event)) {
                    netRoots.add(st);
                }
            }
            MapStateNet = new HashMap <Integer, Set <Integer>>();
            for (Integer stRoot : netRoots) {
                stateNet = new HashSet<Integer>();
                for (Integer stOther : eventStates.get(event)) {
                    if (checkStateNetwork(stRoot, stOther, inLTS, event)) {
                        stateNet.add(stRoot);
                        stateNet.add(stOther);
                    }
                }
                MapStateNet.put(stRoot, stateNet);
            }
            eventStatesNets.put(event, MapStateNet);
        }
    }

    public static void getEventDelayProb(Map <String, Map <Integer, Set <Integer>>> eventStatesNets,
    Map <Integer, Set <Trans>> inLTS, Map <String, ArrayList<FractionNumber>> eventProbTriggerMap) {
        for (String ev : eventStatesNets.keySet()) {
            // System.out.println("\n> Event: " + ev);
            for (int start : eventStatesNets.get(ev).keySet()) {
                // System.out.println(">>> Start: " + start);
                traverseToAssignEventDelayProb(start, 0, ev, new HashSet<Integer>(), inLTS, eventProbTriggerMap);
            }
        }
    }

    public static void traverseToAssignEventDelayProb(int cState, int cTime, String cEvent, Set <Integer> visited,
    Map <Integer, Set <Trans>> inLTS, Map <String, ArrayList<FractionNumber>> eventProbTriggerMap) {
        if (visited.contains(cState)) {
            return;
        }
        visited.add(cState);
        for (Trans tr : inLTS.get(cState)) {
            // System.out.println("Traversing " + tr.asKey() + ", cTime: " + cTime);
            if (tr.lbl.equals(cEvent)) {
                // System.out.println("Assigning to " + tr.asKey());
                tr.eventDelayProb = eventProbTriggerMap.get(tr.lbl).get(cTime);
            } else if (tr.delayForEvent.containsKey(cEvent)) {
                cTime = cTime + 1;
                traverseToAssignEventDelayProb(tr.dst, cTime, cEvent, visited, inLTS, eventProbTriggerMap);
                cTime = cTime - 1;
            } else {
                traverseToAssignEventDelayProb(tr.dst, cTime, cEvent, visited, inLTS, eventProbTriggerMap);
            }
        }
        visited.remove(cState);
    }

    public static boolean checkNetRoot(int st, Map <Integer, Set <Trans>> statesInsAll, String event) {
        for (Trans tr : statesInsAll.get(st)) {
            if ((tr.delayForEvent.containsKey(event) || !tr.lbl.equals("Time")) && !tr.lbl.equals(event)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean checkStateNetwork (int st1, int st2, Map <Integer, Set <Trans>> inLTS,
    String event) {
        Set <Integer> visited = new HashSet<Integer>();
        Set <Boolean> isNetwork = new HashSet<Boolean>();
        traverseToCheckStateNetwork2(isNetwork, st1, st2, inLTS, event, visited);
        if (isNetwork.size() > 0) {
            return true;
        }
        // isNetwork.clear();
        // visited.clear();
        // traverseToCheckStateNetwork2(isNetwork, st2, st1, inLTS, event, visited);
        // if (isNetwork.size() > 0) {
        //     return true;
        // }
        return false;
    }

    public static void traverseToCheckStateNetwork2 (Set <Boolean> isNetwork, int cState, int tState,
    Map <Integer, Set <Trans>> inLTS, String event, Set <Integer> visited) {
        if (visited.contains(cState)) {
            return;
        }
        visited.add(cState);

        if (cState == tState) {
            isNetwork.add(true);
            return;
        }

        for (Trans tr : inLTS.get(cState)) {
            if ((tr.delayForEvent.containsKey(event) || !tr.lbl.equals("Time")) && !tr.lbl.equals(event)) {
                traverseToCheckStateNetwork2(isNetwork, tr.dst, tState, inLTS, event, visited);
            }
        }

        // for (Trans tr : statesInsAll.get(cState)) {
        //     if ((tr.delayForEvent.containsKey(event) || !tr.lbl.equals("Time")) && !tr.lbl.equals(event)) {
        //         traverseToCheckStateNetwork2(isNetwork, tr.src, tState, inLTS, statesInsAll, event, visited);
        //     }
        // }
    }
    
    public static void getEventStateEqStart (
    Map <String, Map <Integer,Set <Set <Integer>>>> eqStartStates,
    Map <String, Map <Integer, Set <Integer>>> eventStatesNets,
    Map <Integer, Set <Trans>> inLTS, Map <Integer, Set <Trans>> statesInsAll) {
        int closest;
        Map <Integer, Set <Set <Integer>>> tmpStarts;
        Set <Set <Integer>> tmpEnds;
        Set <Integer> net;
        for (String event : eventStatesNets.keySet()) {
            // System.out.println("\nEvent " + event);
            for (int root : eventStatesNets.get(event).keySet()) {
                // System.out.print("\nNet {");
                net = eventStatesNets.get(event).get(root);
                // for (Integer st: net) {
                //     System.out.print(st + " ");
                // }
                // System.out.println("}");
                closest = getClosestToNet(root, net, inLTS);
                tmpStarts = new HashMap<Integer, Set <Set <Integer>>>();
                tmpEnds = new HashSet<Set <Integer>>();
                if (eqStartStates.containsKey(event)) {
                    tmpStarts.putAll(eqStartStates.get(event));
                    if (eqStartStates.get(event).containsKey(closest)) {
                        tmpEnds.addAll(eqStartStates.get(event).get(closest));
                    }
                }
                tmpEnds.add(net);
                tmpStarts.put(closest, tmpEnds);
                eqStartStates.put(event, tmpStarts);
            }
        }
    }

    public static int getClosestToNet(int root, Set <Integer> net, Map <Integer, Set <Trans>> inLTS) {
        List <Integer> pathIntersection = new ArrayList <Integer>();
        int cState = 0;
        ArrayList <Boolean> visited = new ArrayList<Boolean>();
        int stIdx = 0;
        while (stIdx < inLTS.size()) {
            visited.add(false);
            stIdx++;
        }
        String path = "";
        traverseInitToNet(pathIntersection, cState, root, visited, net, inLTS, path);
        return pathIntersection.getLast();
    }

    public static void traverseInitToNet(List <Integer> pathIntersection, int cState, int rState,
    ArrayList <Boolean> visited, Set <Integer> net, Map <Integer, Set <Trans>> inLTS,
    String path) {
        path += cState + ",";
        // System.out.println("Current path: " + path);
        if (net.contains(cState)) {
            List <Integer> tmpPath = Arrays.asList(path.split("\\s*,\\s*"))
            .stream().map(s -> Integer.parseInt(s.trim())).collect(Collectors.toList());;
            // if (tmpPath.contains(rState)) {
                if (pathIntersection.size() == 0) {
                    pathIntersection.addAll(tmpPath);
                    // System.out.print("New intersection ");
                    // for (Integer integer : pathIntersection) {
                    //     System.out.print(integer + " ");
                    // }
                    // System.out.println("");
                } else {
                    pathIntersection.retainAll(tmpPath);
                    // System.out.print("New intersection ");
                    // for (Integer integer : pathIntersection) {
                    //     System.out.print(integer + " ");
                    // }
                    // System.out.println("");
                }
            // }
            return;
        }
        if (visited.get(cState)) {
            return;
        }
        visited.set(cState, true);
        for (Trans tr : inLTS.get(cState)) {
            traverseInitToNet(pathIntersection, tr.dst, rState, visited, net, inLTS, path);
        }
        visited.set(cState, false);
    }

    public static void getTransPossibilities (Map <Integer, Set <TransPossibility>> transNetPossibilities,
    Map <String, Map <Integer, Set <Set <Integer>>>> eqStartStates,
    Map <Integer, Set <Trans>> inLTS) {
        Map <Integer, Set <ArrayList <Trans>>> transPaths;
        ArrayList <Trans> tmpPath;
        Set <TransPossibility> tmpTransPossibilities;
        TransPossibility transPos;
        Set <Integer> visited;
        for (String event : eqStartStates.keySet()) {
            // System.out.println("\n> Event: " + event);
            for (int start : eqStartStates.get(event).keySet()) {
                // System.out.println(">>> Start state: " + start);
                for (Set <Integer> endStates : eqStartStates.get(event).get(start)) {
                    transPaths = new HashMap <Integer, Set <ArrayList <Trans>>>();
                    tmpPath = new ArrayList<Trans>();
                    // System.out.println(">>>>> End states: ");
                    // for (int st : endStates) {
                    //     System.out.print(st + " ");
                    // }
                    // System.out.println();
                    visited = new HashSet<Integer>();
                    traverseStartToEvent(transPaths, tmpPath, start, endStates,
                        visited, event, inLTS, 0);
                    transPos = new TransPossibility(event, transPaths);
                    tmpTransPossibilities = new HashSet<TransPossibility>();
                    if (transNetPossibilities.containsKey(start)) {
                        tmpTransPossibilities.addAll(transNetPossibilities.get(start));
                    }
                    tmpTransPossibilities.add(transPos);
                    transNetPossibilities.put(start, tmpTransPossibilities);
                }
            }
        }
    }

    public static void traverseStartToEvent(Map <Integer, Set <ArrayList <Trans>>> transPaths,
    ArrayList <Trans> tmpPath, int cState, Set <Integer> endStates, Set <Integer> visited,
    String event, Map <Integer, Set <Trans>> inLTS, int delay) {
        // for (Trans tr : inLTS.get(cState)) {
        //     tmpPath.add(tr);
        //     if (tr.lbl.equals(event) && endStates.contains(tr.src)) {
        //         ArrayList <Trans> newPath = new ArrayList<Trans>();
        //         newPath.addAll(tmpPath);
        //         Set <ArrayList <Trans>> tmpPaths = new HashSet<ArrayList <Trans>>();
        //         if (transPaths.containsKey(delay)) {
        //             tmpPaths.addAll(transPaths.get(delay));
        //         }    
        //         tmpPaths.add(newPath);        
        //         transPaths.put(delay, tmpPaths);
        //     } else {
        //         if (tr.delayForEvent.containsKey(event)) {
        //             tr.delayForEvent.put(event, delay);
        //             delay++;
        //             traverseStartToEvent(transPaths, tmpPath, tr.dst, endStates, event, inLTS, delay);
        //             delay--;
        //         } else if (tr.lbl.equals(event)) {
        //             int tmpDelay = delay;
        //             delay = 0;
        //             traverseStartToEvent(transPaths, tmpPath, tr.dst, endStates, event, inLTS, delay);
        //             delay = tmpDelay;
        //         } else {
        //             traverseStartToEvent(transPaths, tmpPath, tr.dst, endStates, event, inLTS, delay);
        //         }
        //     }
        //     tmpPath.remove(tr);
        // }
        // System.out.println("Now at " + cState);
        if (visited.contains(cState)) {
            return;
        }
        visited.add(cState);
        for (Trans tr : inLTS.get(cState)) {
            if (tr.lbl.equals(event) && endStates.contains(cState)) {
                tmpPath.add(tr);
                ArrayList <Trans> newPath = new ArrayList<Trans>();
                for (Trans trPath : tmpPath) {
                    newPath.add(trPath);
                }
                tmpPath.remove(tr);
                Set <ArrayList <Trans>> tmpPaths = new HashSet<ArrayList <Trans>>();
                if (transPaths.containsKey(delay)) {
                    tmpPaths.addAll(transPaths.get(delay));
                }
                tmpPaths.add(newPath);
                transPaths.put(delay, tmpPaths);
                // visitedEndStates.add(cState);
                // if (!visitedEndStates.containsAll(endStates)) {
                //     System.out.println("Moving forward on " + tr.asKey());
                //     tmpPath.add(tr);
                //     delay = 0;
                //     traverseStartToEvent(transPaths, tmpPath, tr.dst, endStates, visitedEndStates, event, inLTS, delay);
                //     tmpPath.remove(tr);
                // }
                // System.out.println("Returning at " + tr.asKey());
            } else {
                // System.out.println("Traversing " + tr.asKey());
                if (tr.delayForEvent.containsKey(event)) {
                    tr.delayForEvent.put(event, delay);
                    delay++;
                    tmpPath.add(tr);
                    traverseStartToEvent(transPaths, tmpPath, tr.dst, endStates, visited, event, inLTS, delay);
                    tmpPath.remove(tr);
                    delay--;
                } else if (tr.lbl.equals(event)) {
                    tmpPath.add(tr);
                    delay = 0;
                    traverseStartToEvent(transPaths, tmpPath, tr.dst, endStates, visited, event, inLTS, delay);
                    tmpPath.remove(tr);
                } else {
                    tmpPath.add(tr);
                    traverseStartToEvent(transPaths, tmpPath, tr.dst, endStates, visited, event, inLTS, delay);
                    tmpPath.remove(tr);
                }
            }
        }
        visited.remove(cState);
    }

    public static void getTransVarMapping(Map <String, String> transVarMapping, Map <Integer, Set <Trans>> inLTS) {
        char varChar = 'a';
        int varNum = 1;
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                StringBuilder sb = new StringBuilder();
                sb.append(varChar);
                sb.append(varNum);
                transVarMapping.put(tr.asKey(), sb.toString());
                if (varNum == 9) {
                    varNum = 1;
                    varChar+=1;
                } else {
                    varNum++;
                }
            }
        }
    }

    public static void getTransVarMappingPy(Map <String, String> transVarMapping, Map <Integer, Set <Trans>> inLTS) {
        char varChar = 'v';
        int varNum = 1;
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                StringBuilder sb = new StringBuilder();
                sb.append(varChar);
                sb.append(varNum);
                transVarMapping.put(tr.asKey(), sb.toString());
                varNum++;
            }
        }
    }

    public static void getEquations(Map <String, String> equationsPy,
    Map <Integer, Set <String>> equations, Map <Integer, Set <String>> equationsUnmap,
    Map <Integer, Set <String>> equationVars, Map <Integer, Set <TransPossibility>> transPossibilities,
    Map <String, ArrayList<FractionNumber>> eventProbTriggerMap, Map <String, FractionNumber> eventProbMap,
    Map <String, ArrayList<FractionNumber>> eventProbTimeMap,
    Map <String, String> transVarMapping, Map <Integer, Set <Trans>> inLTS) {
        Map <Integer, Set <Integer>> statesInNets = new HashMap<Integer, Set <Integer>>();
        Set <String> tmpEqs;
        Set <String> tmpEqsUnmap;
        Set <String> tmpVars;
        Set <String> combinedEqs;
        Set <String> combinedEqsUnmap;
        String tmpEq;
        String tmpEqUnmap;
        String multi;
        String plus;
        Set <Integer> netStates;
        int checkMinEvents;
        FractionNumber multiForDelayProb;
        String firstTransEvent;
        Trans firstTrans = new Trans(0, "", 0, 0);
        for (int start : transPossibilities.keySet()) {
            tmpEqs = new HashSet<String>();
            tmpEqsUnmap = new HashSet<String>();
            tmpVars = new HashSet<String>();
            netStates = new HashSet<Integer>();
            for (TransPossibility tp : transPossibilities.get(start)) {
                for (int delay : tp.transPathsDelay.keySet()) {
                    plus = "";
                    tmpEq = "";
                    tmpEqUnmap = "";
                    for (List <Trans> path : tp.transPathsDelay.get(delay)) {
                        tmpEq += plus;
                        tmpEqUnmap += plus;
                        multi = "";
                        for (Trans tr : path) {
                            tmpEq += multi + transVarMapping.get(tr.asKey());
                            tmpEqUnmap += multi + tr.asKey();
                            multi = "*";
                            tmpVars.add(transVarMapping.get(tr.asKey()));
                            netStates.add(tr.src);
                        }
                        plus = " + ";
                    }
                    // System.out.println("Building for state start " + start + ", event: " + tp.event + ", delay: " + delay);
                    if (eventProbTriggerMap.get(tp.event) != null) {
                        // System.out.println("Adding to equations");
                        equationsPy.put(tmpEq, eventProbTriggerMap.get(tp.event).get(delay).getFractionString());
                        tmpEq += " == " + eventProbTriggerMap.get(tp.event).get(delay).getFractionString();
                        tmpEqUnmap += " == " + eventProbTriggerMap.get(tp.event).get(delay).getFractionString();
                        tmpEqs.add(tmpEq);
                        tmpEqsUnmap.add(tmpEqUnmap);
                    }
                }
            }
            equations.put(start, tmpEqs);
            equationsUnmap.put(start, tmpEqsUnmap);
            equationVars.put(start, tmpVars);
            statesInNets.put(start, netStates);
        }
        for (Integer start : statesInNets.keySet()) {
            tmpEqs = new HashSet<String>();
            tmpEqsUnmap = new HashSet<String>();
            for (Integer state : statesInNets.get(start)) {
                plus = "";
                tmpEq = "";
                tmpEqUnmap = "";
                checkMinEvents = 0;
                firstTransEvent = "";
                for (Trans tr : inLTS.get(state)) {
                    tmpEq += plus + transVarMapping.get(tr.asKey());
                    tmpEqUnmap += plus + tr.asKey();
                    plus = " + ";
                    if (!tr.lbl.equals("Time")) {
                        checkMinEvents++;
                        if (firstTransEvent.equals("")) {
                            firstTransEvent = tr.asKey();
                            firstTrans = tr;
                        }
                    }
                }
                equationsPy.put(tmpEq, "1");
                tmpEq += " == 1";
                tmpEqs.add(tmpEq);
                tmpEqUnmap += " == 1";
                tmpEqsUnmap.add(tmpEqUnmap);
                if (checkMinEvents > 1) {
                    for (Trans tr : inLTS.get(state)) {
                        // if (!tr.asKey().equals(firstTransEvent) && !tr.lbl.equals("Time")) {
                        //     tmpEqs.add(transVarMapping.get(firstTransEvent) + " == " + transVarMapping.get(tr.asKey()));
                        //     tmpEqsUnmap.add(firstTransEvent + " == " + tr.asKey());
                        // }
                        if (!tr.asKey().equals(firstTransEvent) && !tr.lbl.equals("Time")) {
                            equationsPy.put(transVarMapping.get(firstTransEvent) + "/" + transVarMapping.get(tr.asKey()), fracDiv(firstTrans.eventDelayProb, tr.eventDelayProb).getFractionString());
                            tmpEqs.add(transVarMapping.get(firstTransEvent) + "/" + transVarMapping.get(tr.asKey())
                            + " == " + fracDiv(firstTrans.eventDelayProb, tr.eventDelayProb).getFractionString());
                            tmpEqsUnmap.add(firstTransEvent + " == " + tr.asKey());
                        }
                        if (tr.isDelayTrans) {
                            multiForDelayProb = new FractionNumber(1, 1);
                            for (String event : tr.delayForEvent.keySet()) {
                                multiForDelayProb = fracMultiply(multiForDelayProb, eventProbTimeMap.get(event).get(tr.delayForEvent.get(event)));
                            }
                            equationsPy.put(transVarMapping.get(tr.asKey()), multiForDelayProb.getFractionString());
                            tmpEqs.add(transVarMapping.get(tr.asKey()) + " == " + multiForDelayProb.getFractionString());
                            tmpEqsUnmap.add(tr.asKey() + " == " + multiForDelayProb.getFractionString());
                        }
                    }
                }
            }
            combinedEqs = new HashSet<String>();
            combinedEqs.addAll(equations.get(start));
            combinedEqs.addAll(tmpEqs);
            equations.put(start, combinedEqs);
            combinedEqsUnmap = new HashSet<String>();
            combinedEqsUnmap.addAll(equationsUnmap.get(start));
            combinedEqsUnmap.addAll(tmpEqsUnmap);
            equationsUnmap.put(start, combinedEqsUnmap);
        }
    }

    public static void solveNetEquations (Map <String, FractionNumber> solverRes,
    Map <Integer, Set <String>> equations,
    Map <Integer, Set <String>> equationVars, Map <String, String> transVarMapping) {
        // String eqString;
        // String varsString;
        // String delim;
        // for (int state : equations.keySet()) {
        //     System.out.println("Solving statenet " + state);
        //     eqString = "{";
        //     delim = "";
        //     for (String eq : equations.get(state)) {
        //         eqString += delim + eq;
        //         delim = ", ";
        //     }
        //     eqString += "}";
        //     varsString = "{";
        //     delim = "";
        //     for (String var : equationVars.get(state)) {
        //         varsString += delim + var;
        //         delim = ", ";
        //     }
        //     varsString += "}";
        //     solveEqs(eqString, varsString, solverRes);
        //     System.out.println();
        // }

        String eqString = "{";
        String varsString = "{";
        String delimEq = "";
        String delimVar = "";
        for (int state : equations.keySet()) {
            for (String eq : equations.get(state)) {
                eqString += delimEq + eq;
                delimEq = ", ";
            }
            for (String var : equationVars.get(state)) {
                // eqString += delimEq + var + " >= 0";
                varsString += delimVar + var;
                delimVar = ", ";
            }
        }
        eqString += "}";
        varsString += "}";
        System.out.println("All equations: " + eqString);
        System.out.println("All variables: " + varsString);
        solveEqs(eqString, varsString, solverRes);
    }

    public static void getGlobalStartingStates (Set <Integer> startingStates, String event, Map <Integer, Set <Trans>> inLTS,
    Map <Integer, Set <Trans>> statesIns) {
        boolean isEvent;
        // boolean isTime;
        boolean isInEvent;
        boolean isInTime;
        boolean isIn;
        for (int st : inLTS.keySet()) {
            isEvent = false;
            // isTime = false;
            for (Trans tr : inLTS.get(st)) {
                if (tr.lbl.equals(event)) {
                    isEvent = true;
                }
                // else if (tr.lbl.equals("Time") && tr.time == 1) {
                //     isTime = true;
                // }
            }
            if (isEvent) {
                isIn = false;
                for (Trans tr : statesIns.get(st)) {
                    isInEvent = false;
                    isInTime = false;
                    if (tr.time == 1 && tr.lbl.equals("Time")) {
                        for (Trans tr2 : inLTS.get(tr.src)) {
                            if (tr2.lbl.equals(event)) {
                                isInEvent = true;
                            } else if (tr2.time == 1 && tr2.lbl.equals("Time")) {
                                isInTime = true;
                            }
                        }
                        if (isInEvent && isInTime) {
                            isIn = true;
                            break;
                        }
                    }
                }
                if (!isIn){
                    startingStates.add(st);
                }
            }
        }
    }
    
    public static void checkStartingStatesAncestors (Set <Integer> startingStatesChecked, Set <Integer> startingStates,
    String event, Map <Integer, Set <Trans>> inLTS, Map <Integer, Set <Trans>> statesIns) {
        for (Integer stNow : startingStates) {
            for (Integer stOther : startingStates) {
                if (stNow != stOther) {

                }
            }
        }
    }

    public static void traverseStartingStates (Set <Integer> startingStates, int cState, Set <Integer> visited, String event,
    Map <Integer, Set <Trans>> inLTS, Map <Integer, Set <Trans>> statesIns) {
        if (visited.contains(cState)) {
            return;
        }
        visited.add(cState);
        if (checkStartingState(cState, event, inLTS, statesIns)) {
            // System.out.println(cState + " YES");
            startingStates.add(cState);
        } else {
            // System.out.println(cState + " NO");
        }
        for (Trans tr : inLTS.get(cState)) {
            traverseStartingStates(startingStates, tr.dst, visited, event, inLTS, statesIns);
        }
        visited.remove(cState);
    }
    
    public static boolean checkStartingState (int state, String event,
    Map <Integer, Set <Trans>> inLTS, Map <Integer, Set <Trans>> statesIns) {
        boolean inCheckTime = true;
        boolean inCheckEvent = true;
        boolean outCheckTime = false;
        boolean outCheckEvent = false;
        for (Trans trIn : statesIns.get(state)) {
            for (Trans trInOut : inLTS.get(trIn.src)) {
                if (trInOut.lbl.equals(event)) {
                    inCheckEvent = false;
                } else if (trInOut.time == 1) {
                    inCheckTime = false;
                }
            }
        }
        for (Trans trOut : inLTS.get(state)) {
            if (trOut.src == trOut.dst && trOut.lbl.equals(event)) {
                return true;
            } else if (trOut.time == 1) {
                outCheckTime = true;
            } else if (trOut.lbl.equals(event)) {
                outCheckEvent = true;
            }
        }
        if ((inCheckTime || inCheckEvent) && outCheckTime && outCheckEvent) {
            return true;
        }
        return false;
    }
    
    public static void startAssignProbs (Map<Integer, Set<Trans>> taLTS, Map <String, ArrayList<FractionNumber>> eventProbSet,
    Map <String, ArrayList<FractionNumber>> eventProbTimeMap, Map <String, Set <Integer>> allEvents, Map <String, String> disTypes) {
        ArrayList <TransPair> tPs;
        ArrayList <FractionNumber> delayProbs;
        for (String event : allEvents.keySet()) {
            for (int st : allEvents.get(event)) {
                tPs = new ArrayList<TransPair>();
                delayProbs = new ArrayList<FractionNumber>();
                traverseToGetTransPairs(tPs, st, event, taLTS);
                assignProbs(eventProbTimeMap, tPs, delayProbs, disTypes, event);
                eventProbSet.put(event, delayProbs);
            }
        }
    }
    
    public static void traverseToGetTransPairs(ArrayList <TransPair> tPs, int st, String event,
    Map<Integer, Set<Trans>> taLTS) {
        boolean cnt[] = {false, false};
        TransPair tP = new TransPair();
        for (Trans tr : taLTS.get(st)) {
            if (tr.lbl.equals("Time") && tr.time == 1) {
                tP.trTime = tr;
                cnt[0] = true;
            } else if (tr.lbl.equals(event)) {
                tP.trEvent = tr;
                cnt[1] = true;
            }
        }

        if (cnt[0] == true && cnt[1] == true) {
            tPs.add(tP);
        } else if (tPs.size() > 0 && cnt[1] == true) {
            tPs.add(tP);
            return;
        } else {
            return;
        }

        for (Trans tr : taLTS.get(st)) {
            if (tr.lbl.equals("Time") && tr.time == 1) {
                traverseToGetTransPairs(tPs, tr.dst, event, taLTS);
            }
        }
    }
    
    public static void assignProbs (Map <String, ArrayList<FractionNumber>> eventProbTimeMap,
    ArrayList <TransPair> tPs, ArrayList <FractionNumber> delayProbs, Map <String, String> disTypes,
    String event) {
        DecimalFormat df = new DecimalFormat("#.#######");
        df.setRoundingMode(RoundingMode.HALF_UP);
        int range = tPs.size();
        ArrayList <FractionNumber> distProbTransList = new ArrayList<FractionNumber>();
        ArrayList <FractionNumber> newTimeProbList;
        if (!disTypes.containsKey(event)) {
            distProbTransList.add(new FractionNumber(1, 1));
            delayProbs.add(new FractionNumber(1, 1));
        }
        else if (disTypes.get(event).equals("binomial")) {
            int n = range;
            double[][] binomial = new double[n+1][];
            binomial[1] = new double[1 + 2];
            binomial[1][1] = 1.0;
            for (int i = 2; i <= n; i++) {
                binomial[i] = new double[i+2];
                for (int k = 1; k < binomial[i].length - 1; k++)
                    binomial[i][k] = 0.5 * (binomial[i-1][k-1] + binomial[i-1][k]);
            }
            for (int k = 1; k < binomial[n].length - 1; k++) {
                delayProbs.add(floatToFraction(binomial[n][k]));
            }
            computeDist(distProbTransList, delayProbs);
        } else if (disTypes.get(event).equals("uniform")) {
            for (int i = 0; i < range; i++) {
                delayProbs.add(new FractionNumber(1 , range));
            }
            computeDist(distProbTransList, delayProbs);
        } else if (disTypes.get(event).contains("custom")) {
            String [] arrCustom = disTypes.get(event).split(":")[1].replace(" ", "").split(",");
            for (int i = 0; i < range; i++) {
                delayProbs.add(new FractionNumber(Integer.parseInt(arrCustom[i].split("/")[0]), Integer.parseInt(arrCustom[i].split("/")[1])));
            }
            computeDist(distProbTransList, delayProbs);
        } else {
            System.out.println("Unknown distribution!");
            System.out.println("uniform/binomial/custom");
        }

        if (disTypes.containsKey(event)) {
            int idxTransPair = 0;
            for (FractionNumber prob : distProbTransList) {
                tPs.get(idxTransPair).trEvent.prb = prob;
                if (tPs.get(idxTransPair).trTime != null){
                    tPs.get(idxTransPair).trTime.prb = fracMin(new FractionNumber(1, 1), prob);
                    newTimeProbList = new ArrayList<FractionNumber>();
                    if (eventProbTimeMap.containsKey(tPs.get(idxTransPair).trEvent.lbl)) {
                        newTimeProbList.addAll(eventProbTimeMap.get(tPs.get(idxTransPair).trEvent.lbl));
                    }
                    newTimeProbList.add(tPs.get(idxTransPair).trTime.prb);
                    eventProbTimeMap.put(tPs.get(idxTransPair).trEvent.lbl, newTimeProbList);
                }
                idxTransPair++;
            }
        }
    }
    
    // public static void computeDist (ArrayList <Double> dList, ArrayList <Double> distTrans) {
    //     ArrayList <Double> tmpDividers = new ArrayList<Double>();
    //     for (int i = 0; i < distTrans.size(); i++) {
    //         double divider = 1;
    //         for (Double t : tmpDividers) {
    //             divider = divider * t;
    //         }
    //         dList.add(distTrans.get(i) / divider);
    //         tmpDividers.add(1 - (distTrans.get(i) / divider));
    //     }
    // }

    public static void computeDist (ArrayList <FractionNumber> dList, ArrayList <FractionNumber> distTrans) {
        ArrayList <FractionNumber> tmpDividers = new ArrayList<FractionNumber>();
        FractionNumber div;
        FractionNumber tmpFrac;
        for (FractionNumber dt : distTrans) {
            div = new FractionNumber(1, 1);
            for (FractionNumber t : tmpDividers) {
                div = fracMultiply(div, t);
            }
            tmpFrac = fracMultiply(dt, fracFlip(div));
            dList.add(tmpFrac);
            tmpDividers.add(fracMin(new FractionNumber(1, 1), tmpFrac));
        }
    }
    
    public static void getPaths (ArrayList <ArrayList <Trans>> paths, Map <Integer, Set<Trans>> inLTS) {
        ArrayList <Boolean> visited = new ArrayList<Boolean>();
        ArrayList <Trans> tmpPath = new ArrayList<Trans>();
        int k = 0;
        while (k < inLTS.size()) {
            visited.add(false);
            k++;
        }
        DFSToCollectPaths(paths, tmpPath, inLTS, 0, 0, visited);
    }
    
    public static void DFSToCollectPaths (ArrayList <ArrayList <Trans>> paths, ArrayList <Trans> tmpPath,
    Map <Integer, Set<Trans>> inLTS, int cState, int tState, ArrayList <Boolean> visited) {
        if (visited.get(cState)) {
            ArrayList <Trans> newPath = new ArrayList<Trans>();
            for (Trans tr : tmpPath) {
                newPath.add(tr);
            }
            paths.add(newPath);
            return;
        }
        visited.set(cState, true);
        for (Trans tr : inLTS.get(cState)) {
            tmpPath.add(tr);
            DFSToCollectPaths(paths, tmpPath, inLTS, tr.dst, tState, visited);
            tmpPath.remove(tr);
        }
        visited.set(cState, false);
    }

    public static boolean checkPathExist (ArrayList <Trans> path, Set <Set <Integer>> nodesInPaths) {
        for (Set <Integer> nodes : nodesInPaths) {
            Set <Integer> tmpNodes = new HashSet<Integer>();
            getAllNodesInPath(tmpNodes, path);
            if (nodes.containsAll(tmpNodes)){
                return true;
            }
        }
        return false;
    }

    public static void getAllNodesInPath (Set <Integer> nodes, ArrayList <Trans> path) {
        for (Trans trans : path) {
            nodes.add(trans.src);
            nodes.add(trans.dst);
        }
    }
    
    public static void getStateEqs (Set <ArrayList <String>> eqs, Map <Integer, Set<Trans>> lts) {
        ArrayList <String> eq;
        for (int st : lts.keySet()) {
            eq = new ArrayList<String>();
            for (Trans tr : lts.get(st)) {
                eq.add(tr.asKey());
            }
            eqs.add(eq);
        }
    }
    
    public static void getMapEqVars (Map <String, String> mapEqVars, Set <ArrayList <String>> pathEqs, Set <ArrayList <String>> stateEqs) {
        char alphabet = 'a';
        int idxVar = 1;
        int idx;
        for (ArrayList<String> eq : pathEqs) {
            idx = 1;
            for (String var : eq) {
                if (!mapEqVars.containsKey(var) && idx != eq.size()) {
                    mapEqVars.put(var, String.valueOf(alphabet)+idxVar);
                    idxVar++;
                    if (idxVar == 10) {
                        idxVar = 1;
                        alphabet++;
                    }
                }
                idx++;
            }
        }
        for (ArrayList<String> eq : stateEqs) {
            for (String var : eq) {
                if (!mapEqVars.containsKey(var)) {
                    mapEqVars.put(var, String.valueOf(alphabet)+idxVar);
                    idxVar++;
                    if (idxVar == 10) {
                        idxVar = 1;
                        alphabet++;
                    }
                }
            }
        }
    }
    
    public static String getSolverVars (Map <String, String> mapEqVars) {
        String solverVars = "{";
        String delim = "";
        for (String var : mapEqVars.keySet()) {
            solverVars += delim + mapEqVars.get(var);
            delim = ", ";
        }
        return solverVars + "}";
    }
    
    public static String getSolverEqs (Map <String, String> mapEqVars, Set <ArrayList <String>> pathEqs, Set <ArrayList <String>> stateEqs) {
        String eqs = "{";

        String delimMulti;
        int idx;
        String delimComma = "";
        String delimAdd = "";
        for (ArrayList<String> path : pathEqs) {
            eqs += delimComma;
            delimMulti = "";
            idx = 1;
            for (String var : path) {
                if (idx == path.size()) {
                    eqs += "==" + var;
                } else {
                    eqs += delimMulti + mapEqVars.get(var);
                    delimMulti = "*";
                }
                idx++;
            }
            delimComma = ", ";
        }

        for (ArrayList<String> state : stateEqs) {
            delimAdd = "";
            eqs += delimComma;
            for (String str : state) {
                eqs += delimAdd + mapEqVars.get(str);
                delimAdd = "+";
            }
            eqs += "==1";
        }

        return eqs + "}";
    }
    
    public static void solveEqs (String solverEqs, String solverVars, Map <String, FractionNumber> solverResult) {
        try {
            ExprEvaluator util = new ExprEvaluator();
            IExpr result = util.eval("Solve(" + solverEqs + ", " + solverVars + ")");
            System.out.println(result.toString());
            if (result.toString().equals("{{}}") || result.toString().equals("{}")) {
                System.out.println("No solution");
                return;
            }
            String resultStrArr[] = result.toString().replace("{", "").replace("}", "").replace("\n", "").split(",");
            for (String string : resultStrArr) {
                String var = string.split("->")[0];
                if (string.split("->")[1].split("/").length > 1) {
                    int up = Integer.parseInt(string.split("->")[1].split("/")[0]);
                    int down = 1;
                    if (string.split("->")[1].split("/").length > 1) {
                        down = Integer.parseInt(string.split("->")[1].split("/")[1]);
                    }
                    solverResult.put(var, new FractionNumber(up, down));
                } else {
                    int num = Integer.parseInt(string.split("->")[1]);
                    solverResult.put(var, new FractionNumber(num, 1));
                }
            }
        } catch (SyntaxError e) {
            System.out.println(e.getMessage());
        } catch (MathException me) {
            System.out.println(me.getMessage());
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
        } catch (final StackOverflowError soe) {
            System.out.println(soe.getMessage());
        } catch (final OutOfMemoryError oome) {
            System.out.println(oome.getMessage());
        }
    }
    
    public static void assignProbsToLTS (Map <Integer, Set <Trans>> inLTS, Map <String, String> mapEqVars, Map <String, FractionNumber> solverResults) {
        int up;
        int down;
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (mapEqVars.get(tr.asKey()) != null) {
                    if (solverResults.get(mapEqVars.get(tr.asKey())) != null){
                        up = solverResults.get(mapEqVars.get(tr.asKey())).up;
                        down = solverResults.get(mapEqVars.get(tr.asKey())).down;
                        tr.prb = new FractionNumber(up, down);
                        // if (tmpFrac.intgr == -1) {
                        //     tr.prb = new FractionNumber(tmpFrac.up, tmpFrac.down);
                        // } else {
                        //     tr.prb = new FractionNumber(1, 1);
                        // }
                    }
                }
            }
        }
    }
    
    public static void computePTSbyTraces (String ifModel, String tracesName) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        String propRemMeta = "";
        int numTrace = countTraces(tracesName);
        
        Map <Integer, Set <Trans>> inLTS = new HashMap <Integer, Set <Trans>>();
        Map <Integer, Set <Trans>> statesInsAll = new HashMap <Integer, Set <Trans>>();
        Map <Integer, Set <Trans>> cutLTS = new HashMap <Integer, Set <Trans>>();
        Map <Integer, Set <Trans>> cutLTSRenum = new HashMap <Integer, Set <Trans>>();
        Map <Integer, Integer> mapCtr = new HashMap <Integer, Integer>();
        Map <Integer, Integer> mapCtrSteady = new HashMap <Integer, Integer>();
        Map <Integer, Double> mapSteadyOri = new HashMap <Integer, Double>();
        Map <Integer, Double> mapSteadyRem = new HashMap <Integer, Double>();

        String propMeta = buildLTS(inLTS, ifModel, statesInsAll);
        computePTSfromAllTraces(inLTS, numTrace, tracesName, mapCtr);
        computeSteadyStates(mapCtr, mapSteadyOri);
        removeTrans(inLTS, cutLTS);
        renumStates(cutLTS, cutLTSRenum, mapCtr, mapCtrSteady);
        checkDead(cutLTSRenum);
        computeSteadyStates(mapCtrSteady, mapSteadyRem);
        propRemMeta = computeMeta(cutLTSRenum);
        writePTS(inLTS, ifModel + "-" + tracesName, propMeta);
        bashCreatePDF(ifModel + "-" + tracesName + "-pts");
        writePTS(cutLTSRenum, ifModel + "-" + tracesName + "-rem", propRemMeta);
        bashCreatePDF(ifModel + "-" + tracesName + "-rem" + "-pts");
        writeSteady(mapSteadyOri, ifModel + "-ori");
        writeSteady(mapSteadyRem, ifModel + "-rem");
        writeDTMC(cutLTSRenum, ifModel + "-" + tracesName + "-rem-pts", propRemMeta);

        double roundOff = (double) cutLTS.size() / (double) inLTS.size();
        DecimalFormat df = new DecimalFormat("#.000");
        System.out.println("State coverage: " + (df.format(roundOff)));

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("PTS computation time: " + elapsedTime + "ms");

    }
    
    public static int countTraces (String dirTrace) throws IOException {
        int num = 0;
        try (Stream<Path> files = Files.list(Paths.get(dirTrace))) {
            num = (int) files.count();
        }
        return num;
    }
    
    public static void computePTSfromAllTraces (Map<Integer, Set<Trans>> inLTS, int numTrace, String dirTrace, Map <Integer, Integer> mapCtr) throws IOException {
        for (int stmap : inLTS.keySet()) {
            mapCtr.put(stmap, 0);
        }
        mapCtr.put(0, 0);
        for (int t = 1; t <= numTrace; t++) {
            String traceNow = dirTrace + "/T" + t + ".txt";
            BufferedReader brTest = new BufferedReader(new FileReader(traceNow));
            List<String> lines = new ArrayList<String>();
            String line = null;
            while ((line = brTest.readLine()) != null) {
                lines.add(line);
            }
            brTest.close();
            PrintWriter writer = new PrintWriter(traceNow, "UTF-8");
            for (String ln : lines) {
                writer.print(ln);
            }
            writer.close();
            System.out.println("computing: " + dirTrace + "/T" + t + ".txt");
            computePTS(inLTS, mapCtr, traceNow);
        }

        for (int st : inLTS.keySet()) {
            for (Trans trs : inLTS.get(st)) {
                trs.prbComp(mapCtr.get(st));
            }
        }

        int tmpTrCtr = 0;
        FractionNumber tmpProb;
        for (int state : inLTS.keySet()) {
            tmpTrCtr = 0;
            tmpProb = new FractionNumber(0, 1);
            for (Trans tr : inLTS.get(state)) {
                tmpTrCtr += tr.ctr;
                if (tr.prb.down == 0) {
                    tr.prb.down = 1;
                }
                tmpProb = fracPlus(tmpProb, tr.prb);
            }
            if (tmpTrCtr == 0) {
                for (Trans tr : inLTS.get(state)) {
                    tr.prb = new FractionNumber(1, inLTS.get(state).size()) ;
                }
            }
            if (fracLess(tmpProb, new FractionNumber(1, 1))) {
                for (Trans tr : inLTS.get(state)) {
                    tr.prb = fracMultiply(tr.prb, fracFlip(tmpProb));
                }
            }
        }
    }
    
    public static void computePTS (Map<Integer, Set<Trans>> inLTS, Map<Integer, Integer> mapCtr, String fileTrace) throws IOException {
        BufferedReader brTest = new BufferedReader(new FileReader(fileTrace));
        String [] trace = brTest.readLine().replace("'", "").split(",");
        brTest.close();
        int cState = 0;
        int initStateCtr = mapCtr.get(cState) + 1;
        mapCtr.put(cState, initStateCtr);
        boolean isFound = false;
        int tmpCState = cState;
        boolean skipNext = false;
        int actNum = 0;
        ArrayList <String> traversedActs = new ArrayList<String>();
        for (String actx : trace) {
            traversedActs.add(actx);
            actNum++;
            if (skipNext) {
                skipNext = false;
            } else {
                String act = actx.split(" ")[0];
                int timeInt = 0;
                if (actx.split(" ").length > 1) {
                    String time = actx.split(" ")[1];
                    timeInt = Integer.parseInt(time);
                }
                if (timeInt == 0) {
                    tmpCState = cState;
                    isFound = false;
                    inner:
                    for (Trans trs : inLTS.get(cState)) {
                        if (compareLabels(trs.lbl, act)) {
                            isFound = true;
                            trs.ctrUp();
                            cState = trs.dst;
                            mapCtr.put(cState, mapCtr.get(cState) + 1);
                            break inner;
                        } else {
                            if (trs.lbl.split("->").length > 1) {
                                if (compareLabels(trs.lbl.split("->")[0], act)) {
                                    isFound = true;
                                    trs.ctrUp();
                                    cState = trs.dst;
                                    mapCtr.put(cState, mapCtr.get(cState) + 1);
                                    skipNext = true;
                                    break inner;
                                }
                            }
                        }
                    }
                    if (isFound == false) {
                        System.out.println("No trans in state " + tmpCState + " is labelled with " + act + " (" + actNum + ")");
                        break;
                    }
                } else {
                    ArrayList <Trans> timeTrace = new ArrayList<>();
                    int tmpCstate = cState;
                    findTimeTrace(inLTS, tmpCstate, act, timeInt, timeTrace);
                    if (timeTrace.size() == 0) {
                        System.out.println("Time does not match on the transitions from state " + cState + ", act: " + act + " (" + actNum + ")");
                        break;
                    } else {
                        for (Trans timeTrans : timeTrace) {
                            inner:
                            for (Trans trs : inLTS.get(cState)) {
                                if ((trs.lbl.equals("Time") && trs.time == timeTrans.time) || (compareLabels(trs.lbl, timeTrans.lbl))) {
                                    trs.ctrUp();
                                    cState = trs.dst;
                                    mapCtr.put(cState, mapCtr.get(cState) + 1);
                                    if (trs.lbl.split("->").length > 1) {
                                        skipNext = true;
                                    }
                                    break inner;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static boolean compareLabels (String s1, String s2) {
        if (s1.equals(s2)) {
            return true;
        } else {
            List<String> arrS1 = new ArrayList<String>(Arrays.asList(s1.split("_")));
            if (arrS1.size() > 2) {
                if ((arrS1.get(arrS1.size() - 2).equals("USELESS") || arrS1.get(arrS1.size() - 2).equals("USEFUL")) &&
                (arrS1.get(arrS1.size() - 1).equals("EXEC") || arrS1.get(arrS1.size() - 1).equals("ACT"))) {
                    arrS1.remove(arrS1.size() - 1);
                    arrS1.remove(arrS1.size() - 1);
                    arrS1.add("START");
                    String joinedS1 = String.join("_", arrS1);
                    if (joinedS1.equals(s2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static void findTimeTrace(Map<Integer, Set<Trans>> inLTS, int cState, String action, int timeGoal, ArrayList <Trans> timeTrace) {
        if (timeTrace.size() > 0) {
            if (compareLabels(timeTrace.get(timeTrace.size() - 1).lbl, action)
            && getTransTimeTotal(timeTrace) == timeGoal) {
                // System.out.println("xa returning state: " + cState + ", with lbl: " + timeTrace.get(timeTrace.size() - 1).lbl + ", T: " + timeTrace.get(timeTrace.size() - 1).time);
                return;
            } else if (timeTrace.get(timeTrace.size() - 1).lbl.split("->").length > 1) {
                if (timeTrace.get(timeTrace.size() - 1).lbl.split("->")[0].equals(action)
                && getTransTimeTotal(timeTrace) == timeGoal) {
                    // System.out.println("xb returning state: " + cState + ", with lbl: " + timeTrace.get(timeTrace.size() - 1).lbl + ", T: " + timeTrace.get(timeTrace.size() - 1).time);
                    return;
                } else if (timeTrace.get(timeTrace.size() - 1).lbl.split("->")[0].equals(action)
                && getTransTimeTotal(timeTrace) != timeGoal) {
                    // System.out.println("xc returning state: " + cState + ", with lbl: " + timeTrace.get(timeTrace.size() - 1).lbl + ", T: " + timeTrace.get(timeTrace.size() - 1).time);
                    return;
                } else if (!compareLabels(timeTrace.get(timeTrace.size() - 1).lbl.split("->")[0], action)) {
                    // System.out.println("xd returning state: " + cState + ", with lbl: " + timeTrace.get(timeTrace.size() - 1).lbl + ", T: " + timeTrace.get(timeTrace.size() - 1).time);
                    return;
                }
            } else if (getTransTimeTotal(timeTrace) > timeGoal
            || ((!timeTrace.get(timeTrace.size() - 1).lbl.equals("Time")) && (!compareLabels(timeTrace.get(timeTrace.size() - 1).lbl, action)))) {
                // System.out.println("xe returning state: " + cState + ", with lbl: " + timeTrace.get(timeTrace.size() - 1).lbl + ", T: " + timeTrace.get(timeTrace.size() - 1).time);
                return;
            } else if (getTransTimeTotal(timeTrace) < timeGoal && compareLabels(timeTrace.get(timeTrace.size() - 1).lbl, action)) {
                // System.out.println("xf returning state: " + cState + ", with lbl: " + timeTrace.get(timeTrace.size() - 1).lbl + ", T: " + timeTrace.get(timeTrace.size() - 1).time);
                return;
            } else if (getTransTimeTotal(timeTrace) < timeGoal && timeTrace.get(timeTrace.size() - 1).lbl.split("->").length > 1) {
                if (timeTrace.get(timeTrace.size() - 1).lbl.split("->")[0].equals(action)) {
                    // System.out.println("xg returning state: " + cState + ", with lbl: " + timeTrace.get(timeTrace.size() - 1).lbl + ", T: " + timeTrace.get(timeTrace.size() - 1).time);
                    return;
                }
            }
        }
        for (Trans tr : inLTS.get(cState)) {
            timeTrace.add(new Trans(tr.src, tr.lbl, tr.time, tr.dst));
            cState = tr.dst;
            // System.out.println("traversing: (" + tr.src + ", " + tr.lbl + ", " + tr.time + ", " + tr.dst + ")");
            findTimeTrace(inLTS, cState, action, timeGoal, timeTrace);
            if (!compareLabels(timeTrace.get(timeTrace.size() - 1).lbl, action)) {
                if (timeTrace.get(timeTrace.size() - 1).lbl.split("->").length > 1) {
                    if ((!timeTrace.get(timeTrace.size() - 1).lbl.split("->")[0].equals(action))
                    || (timeTrace.get(timeTrace.size() - 1).lbl.split("->")[0].equals(action) && getTransTimeTotal(timeTrace) < timeGoal)) {
                        // System.out.println("i removing: " + timeTrace.get(timeTrace.size() - 1).lbl + ", T: " + timeTrace.get(timeTrace.size() - 1).time);
                        timeTrace.remove(timeTrace.size() - 1);
                    } else {
                        // System.out.println("returning aaaaa");
                        return;
                    }
                } else {
                    // System.out.println("j removing: " + timeTrace.get(timeTrace.size() - 1).lbl + ", T: " + timeTrace.get(timeTrace.size() - 1).time);
                    timeTrace.remove(timeTrace.size() - 1);
                }
            } else if (compareLabels(timeTrace.get(timeTrace.size() - 1).lbl, action) && getTransTimeTotal(timeTrace) != timeGoal) {
                timeTrace.remove(timeTrace.size() - 1);
            } else if (getTransTimeTotal(timeTrace) == timeGoal) {
                // System.out.println("returning bbbbb");
                return;
            }
        }
    }
    
    public static int getTransTimeTotal (ArrayList <Trans> transList) {
        int res = 0;
        for (Trans trs : transList) {
            res += trs.time;
        }
        return res;
    }
    
    public static void computeSteadyStates (Map <Integer, Integer> mapCtr, Map <Integer, Double> mapSteady) {
        int totalStatesVisited = 0;
        for (int st : mapCtr.keySet()) {
            totalStatesVisited += mapCtr.get(st);
        }
        for (int st : mapCtr.keySet()) {
            mapSteady.put(st, (double) mapCtr.get(st) / totalStatesVisited);
        }
    }
    
    public static void removeTrans (Map<Integer, Set<Trans>> inLTS, Map<Integer, Set<Trans>> cutLTS) {
        Set <Trans> tmpTrans;
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (tr.ctr > 0) {
                    tmpTrans = new HashSet<Trans>();  
                    if (cutLTS.containsKey(st)) {
                        tmpTrans.addAll(cutLTS.get(st));
                    }
                    tmpTrans.add(new Trans(tr.src, tr.lbl, tr.time, tr.dst, tr.ctr, tr.prb));
                    cutLTS.put(st, tmpTrans);

                    if(!cutLTS.containsKey(tr.dst)) {
                        cutLTS.put(tr.dst, new HashSet<Trans>());
                    }
                }
            }
        }
    }
    
    public static void renumStates (Map<Integer, Set<Trans>> cutLTS, Map<Integer, Set<Trans>> renumLTS, Map<Integer, Integer> mapCtr, Map<Integer, Integer> mapCtrSteady) {
        int numStates = 0;
        Map <Integer, Integer> mapRenum = new HashMap <Integer, Integer>();
        for (int st : cutLTS.keySet()) {
            mapRenum.put(st, numStates);
            mapCtrSteady.put(numStates, mapCtr.get(st));
            numStates++;
        }

        Set <Trans> tmpTrans;
        for (int st : cutLTS.keySet()) {
            for (Trans tr : cutLTS.get(st)) {
                int newSrc = mapRenum.get(st);
                int newDst = mapRenum.get(tr.dst);
                tmpTrans = new HashSet<Trans>();  
                if (renumLTS.containsKey(newSrc)) {
                    tmpTrans.addAll(renumLTS.get(newSrc));
                }
                tmpTrans.add(new Trans(newSrc, tr.lbl, tr.time, newDst, tr.ctr, tr.prb));
                renumLTS.put(newSrc, tmpTrans);

                if(!renumLTS.containsKey(newDst)) {
                    renumLTS.put(newDst, new HashSet<Trans>());
                }
            }
        }
    }
    
    public static void checkDead (Map <Integer, Set <Trans>> cutLTSRenum) {
        for (int st : cutLTSRenum.keySet()) {
            if (cutLTSRenum.get(st).size() == 0) {
                System.out.println("Deadlock state (unfinished loop traversal/not enough trace) : " + st);
            }
        }
    }
    
    public static String computeMeta (Map<Integer, Set<Trans>> cutLTS) {
        String meta = "";
        int source = 0;
        int numTrans = 0;
        int numStates = 0;
        for (int st : cutLTS.keySet()) {
            numStates++;
            numTrans+=cutLTS.get(st).size();
        }
        meta = "des (" + source + ", " + numTrans + ", " + numStates + ")";
        return meta;
    }
    
    public static void modLTS(String ifModel) throws FileNotFoundException, IOException {
        String fileLTS = ifModel;
        String propMeta = "";
        Map <Integer, Set <Trans>> inLTS = new HashMap <Integer, Set <Trans>>();
        Map <Integer, Set <Trans>> statesInsAll = new HashMap <Integer, Set <Trans>>();
        propMeta = buildLTS(inLTS, fileLTS, statesInsAll);
        renameTime(inLTS);
        renameLTSLabels(inLTS);
        hideInit(inLTS);
        hideData(inLTS);
        writeLTSMod(inLTS, fileLTS, propMeta);
    }
    
    public static void renameTime(Map<Integer, Set<Trans>> inLTS) {
        String tmpLbl = "";
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (tr.lbl.contains("time")) {
                    int startIndex = tr.lbl.indexOf('/');
                    int endIndex = tr.lbl.indexOf('>', startIndex + 1);
                    tmpLbl = "\"Time !" + tr.lbl.substring(startIndex+1, endIndex)+"\"";
                    tr.lbl = tmpLbl;
                }
            }
        }
    }
    
    public static void renameLTSLabels (Map<Integer, Set<Trans>> inLTS) { 
        String tmpLbl = "";
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (StringUtils.countMatches(tr.lbl, "<<") == 2) {
                    int startIndex = ordinalIndexOf(tr.lbl, "0", 2);
                    int endIndex = ordinalIndexOf(tr.lbl, "{", 3);
                    tmpLbl = tr.lbl.substring(startIndex+1, endIndex);
                    tr.lbl = "\"" + tmpLbl + "\"";
                } else if (!tr.lbl.contains("i>>") && !tr.lbl.contains("t/") && !tr.lbl.contains("Time")) {
                    int startIndex = ordinalIndexOf(tr.lbl, "0", 1);
                    int endIndex = ordinalIndexOf(tr.lbl, "{", 2);
                    tmpLbl = tr.lbl.substring(startIndex+1, endIndex);
                    tr.lbl = "\"" + tmpLbl + "\"";
                }
            }
        }
    }
    
    public static int ordinalIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1)
            pos = str.indexOf(substr, pos + 1);
        return pos;
    }
    
    public static void hideInit(Map<Integer, Set<Trans>> inLTS) {
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (tr.lbl.contains("}0i>>")) {
                    tr.lbl = "i";
                }
            }
        }
    }
    
    public static void hideData(Map<Integer, Set<Trans>> inLTS) {
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (tr.lbl.contains("}0?")) {
                    tr.lbl = "i";
                }
            }
        }
    }

    public static String buildLTS(Map<Integer, Set<Trans>> inLTS, String fileLTS, Map<Integer, Set<Trans>> statesIns)
    throws FileNotFoundException, IOException {
        String propMeta = "";
        int tmpSrc;
        String tmpLbl = "";
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
                    tmpLbl = arrLines[1];
                    tmpDst = Integer.parseInt(arrLines[2]);
                    tmpTrans = new HashSet<Trans>();  
                    if (inLTS.containsKey(tmpSrc)) {
                        tmpTrans.addAll(inLTS.get(tmpSrc));
                    }
                    tmpLblAct = tmpLbl.replace("!", "_").split("_")[0];
                    if (tmpLblAct.equals("Time")) {
                        tmpLblTime = Integer.parseInt(tmpLbl.replace("!", "_").split("_")[1]);
                        tmpLbl = tmpLblAct;
                    } else {
                        tmpLblTime = 0;
                    }
                    tmpNewTrans = new Trans(tmpSrc, tmpLbl, tmpLblTime, tmpDst);
                    tmpTrans.add(tmpNewTrans);
                    inLTS.put(tmpSrc, tmpTrans);
                    if (!inLTS.containsKey(tmpDst)) {
                        inLTS.put(tmpDst, new HashSet<Trans>());
                    }

                    if (!statesIns.containsKey(tmpSrc)) {
                        statesIns.put(tmpSrc, new HashSet<Trans>());
                    }
                    tmpTransIns = new HashSet<Trans>();
                    if (statesIns.containsKey(tmpDst)) {
                        tmpTransIns.addAll(statesIns.get(tmpDst));
                    }
                    tmpTransIns.add(tmpNewTrans);
                    statesIns.put(tmpDst, tmpTransIns);
                } else { break; }
            }
        }
        return propMeta;
    }

    public static void annotateDelayTrans(Map<Integer, Set<Trans>> inLTS) {
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (tr.lbl.equals("Time") && tr.time == 1) {
                    for (Trans tr2 : inLTS.get(st)) {
                        if (!tr2.lbl.equals("Time")) {
                            tr.isDelayTrans = true;
                            tr.delayForEvent.put(tr2.lbl, 0);
                        }
                    }
                }
            }
        }
    }

    public static void bashCompileLTS(String ifModel, String name) throws IOException, InterruptedException {
        String command = "printf \"\\n" + //
                        "!!!!!!!! Compiling " + name + " TLTS\\n" + //
                        "\\n" + //
                        "\"\n" + //
                        "if2gen " + ifModel + "\"-stripped.if\"\n" + //
                        "kp='./'" + ifModel + "'-stripped.x -t '" + ifModel + "'.aut'\n" + //
                        "eval $kp";
        executeCommands(command);
    }

    public static void bashCompileIndvLTS(String ifModel, String name) throws IOException, InterruptedException {
        String command = "printf \"\\n" + //
                        "!!!!!!!! Compiling " + name + " TLTS\\n" + //
                        "\\n" + //
                        "\"\n" + //
                        "if2gen " + ifModel + "\".if\"\n" + //
                        "kp='./'" + ifModel + "'.x -t '" + ifModel + "'.aut'\n" + //
                        "eval $kp";
        executeCommands(command);
    }
    
    public static void bashReduceLTS(String ifModel, String name, String reduction) throws IOException, InterruptedException {
        String command = "printf \"\\n" + //
                        "!!!!!!!! Reducing " + name + " TLTS\\n" + //
                        "\\n" + //
                        "\"\n" + //
                        "bcg_io " + ifModel + "\"-mod.aut\" \"" + ifModel + "-min.bcg\"\n" + //
                        "bcg_open \"" + ifModel + "-min.bcg\" reductor -weaktrace \"" + ifModel + "-min.bcg\"\n" + //
                        "bcg_io \"" + ifModel + "-min.bcg\" \"" + ifModel + "-min.aut\"\n" + //
                        "bcg_io \"" + ifModel + "-min.bcg\" \"" + ifModel + "-min.dot\"\n" + //
                        // "graphviz2drawio \"" + ifModel + "-min.dot\"\n" + //
                        "dot -Tpdf -Gdpi=300 \"" + ifModel + "-min.dot\" > \"" + ifModel + "-min.pdf\"";
        if (reduction.equals("strong")) {
            command = "printf \"\\n" + //
                        "!!!!!!!! Reducing " + name + " TLTS\\n" + //
                        "\\n" + //
                        "\"\n" + //
                        "bcg_io " + ifModel + "\"-mod.aut\" \"" + ifModel + "-min.bcg\"\n" + //
                        "bcg_open \"" + ifModel + "-min.bcg\" reductor -weaktrace \"" + ifModel + "-min.bcg\"\n" + //
                        "bcg_min \"" + ifModel + "-min.bcg\"\n" + //
                        "bcg_io \"" + ifModel + "-min.bcg\" \"" + ifModel + "-min.aut\"\n" + //
                        "bcg_io \"" + ifModel + "-min.bcg\" \"" + ifModel + "-min.dot\"\n" + //
                        // "graphviz2drawio \"" + ifModel + "-min.dot\"\n" + //
                        "dot -Tpdf -Gdpi=300 \"" + ifModel + "-min.dot\" > \"" + ifModel + "-min.pdf\"";
        }
        
        executeCommands(command);
    }

    public static void bashReduceIndvLTS(String ifModel, String name) throws IOException, InterruptedException {
        String command = "printf \"\\n" + //
                        "!!!!!!!! Reducing " + name + " TLTS\\n" + //
                        "\\n" + //
                        "\"\n" + //
                        "bcg_io " + ifModel + "\"-mod.aut\" \"" + ifModel + "-min.bcg\"\n" + //
                        "bcg_open \"" + ifModel + "-min.bcg\" reductor -weaktrace \"" + ifModel + "-min.bcg\"\n" + //
                        "bcg_min \"" + ifModel + "-min.bcg\"\n" + //
                        "bcg_io \"" + ifModel + "-min.bcg\" \"" + ifModel + "-min.aut\"\n" + //
                        "bcg_io \"" + ifModel + "-min.bcg\" \"" + ifModel + "-min.dot\"\n" + //
                        // "graphviz2drawio \"" + ifModel + "-min.dot\"\n" + //
                        "dot -Tpdf -Gdpi=300 \"" + ifModel + "-min.dot\" > \"" + ifModel + "-min.pdf\"";
        executeCommands(command);
    }
    
    public static void bashIndividualLTSs(String ifModel, ArrayList <String> taNames) throws IOException, InterruptedException {
        try(BufferedReader br = new BufferedReader(new FileReader(ifModel + "-stripped.if"))) {
            String line;
            String tmpProcName;
            Path tmpSrcPath;
            Path tmpDstPath;
            String currentLine;
            boolean deleting;
            while (true) {
                line = br.readLine();
                if (line != null) {
                    if (line.contains("process") && !line.contains("endprocess")) {
                        tmpProcName = StringUtils.substringBetween(line, " ", "(");
                        tmpSrcPath = Paths.get(ifModel + "-stripped.if");
                        tmpDstPath = Paths.get(ifModel + "-" + tmpProcName + ".if");
                        Files.copy(tmpSrcPath, tmpDstPath, StandardCopyOption.REPLACE_EXISTING);
                        try(BufferedReader brx = new BufferedReader(new FileReader(ifModel + "-stripped.if"))) {
                            BufferedWriter writer = new BufferedWriter(new FileWriter(ifModel + "-" + tmpProcName + ".if"));
                            deleting = false;
                            while((currentLine = brx.readLine()) != null) {
                                if (currentLine.contains("process") && !currentLine.contains("process " + tmpProcName) && !currentLine.contains("endprocess") && !deleting) {
                                    deleting = true;
                                    continue;
                                } else if (currentLine.contains("endprocess") && deleting) {
                                    deleting = false;
                                    continue;
                                } else if (deleting) {
                                    continue;
                                }
                                writer.write(currentLine + System.getProperty("line.separator"));
                            }
                            writer.close();
                        }
                        bashCompileIndvLTS(ifModel + "-" + tmpProcName, "process " + tmpProcName);
                        modLTS(ifModel + "-" + tmpProcName);
                        bashReduceIndvLTS(ifModel + "-" + tmpProcName, "process " + tmpProcName);
                        taNames.add(ifModel + "-" + tmpProcName + "-min");
                    }
                } else {
                    break;
                }
            } 
        }
    }
    
    public static void bashCreatePDF(String ifModel) throws IOException, InterruptedException {
        String command = "bcg_io \"" + ifModel + ".aut\" \"" + ifModel + ".dot\"\n" + //
                        // "graphviz2drawio \"" + ifModel + ".dot\"\n" + //
                        "dot -Tpdf -Gdpi=300 \"" + ifModel + ".dot\" > \"" + ifModel + ".pdf\"";
        executeCommands(command);
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

    public static void printAllEvents (Map <String, Set <Integer>> allEvents, String name) {
        System.out.println("\n----- Start printing all events in LTS " + name + " -----");
        String delim;
        for (String event : allEvents.keySet()) {
            System.out.print(event);
            System.out.print(", starting states: {");
            delim = "";
            for (int st : allEvents.get(event)) {
                System.out.print(delim + st);
                delim = ", ";
            }
            System.out.println("}");
        }
        System.out.println("----- End printing all events in LTS " + name + " -----");
    }
    
    public static void printEventProbMap (Map <String, ArrayList<FractionNumber>> eventProbSet,
    Map <String, FractionNumber> eventProb) {
        System.out.println("\n----- Start printing event probabilities -----");
        int idxp = 0;
        String delimComma;
        for (String event : eventProbSet.keySet()) {
            System.out.println(event + ": " + eventProb.get(event).up + "/" + eventProb.get(event).down);
            idxp = 0;
            delimComma = "";
            for (FractionNumber prob : eventProbSet.get(event)) {
                System.out.print(delimComma + idxp + ": " + prob.up + "/" + prob.down);
                delimComma = ", ";
                idxp++;
            }
            System.out.println();
        }
        System.out.println("----- End printing event probabilities -----");
    }

    public static void printEventProbTimeMap (Map <String, ArrayList<FractionNumber>> eventProbTimeMap) {
        System.out.println("\n----- Start printing transition delay probabilities -----");
        int idxp = 0;
        String delimComma;
        for (String event : eventProbTimeMap.keySet()) {
            System.out.println(event + ": ");
            idxp = 0;
            delimComma = "";
            for (FractionNumber prob : eventProbTimeMap.get(event)) {
                System.out.print(delimComma + idxp + ": " + prob.up + "/" + prob.down);
                delimComma = ", ";
                idxp++;
            }
            System.out.println();
        }
        System.out.println("----- End printing transition delay probabilities -----");
    }
    
    public static void printPathEqs (Set <ArrayList <String>> eqs) {
        String multi;
        int idx;
        for (ArrayList <String> eq : eqs) {
            multi = "";
            idx = 1;
            for (String str : eq) {
                if (idx == eq.size()) {
                    System.out.print(" == " + str);
                } else {
                    System.out.print(multi + str);
                    multi = " * ";
                }
                idx++;
            }
            System.out.println();
        }
    }
    
    public static void printStateEqs (Set <ArrayList <String>> eqs) {
        System.out.println("\n----- Start printing state equations -----");
        String add;
        for (ArrayList <String> eq : eqs) {
            add = "";
            for (String str : eq) {
                System.out.print(add + str);
                add = " + ";
            }
            System.out.println(" == 1.0");
        }
        System.out.println("----- End printing state equations -----");
    }
    
    public static void printEqVarsMap (Map <String, String> map) {
        System.out.println("\n----- Start printing map of vars -----");
        for (String k : map.keySet()) {
            System.out.println(k + ", " + map.get(k));
        }
        System.out.println("----- End printing map of vars -----");
    }
    
    public static void printSolverResult (Map <String, FractionNumber> solverResult, Map <String, String> mapEqVars) {
        System.out.println("\n----- Start printing solver result -----");
        if (solverResult.size() != 0) {
            for (String tr : mapEqVars.keySet()) {
                if (solverResult.get(mapEqVars.get(tr)) != null) {
                    System.out.println(mapEqVars.get(tr) + ": " + solverResult.get(mapEqVars.get(tr)).getFractionString());
                }
            }
        }
        System.out.println("----- End printing solver result -----\n");
    }

    public static void printDelayTrans (Map <Integer, Set <Trans>> inLTS) {
        System.out.println("\n----- Start printing delay transitions -----");
        String delim;
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (tr.isDelayTrans) {
                    System.out.print(tr.asKey() + ", delay for: " );
                    delim = "";
                    for (String event : tr.delayForEvent.keySet()) {
                        System.out.print(delim + event + " (" + tr.delayForEvent.get(event) + ")");
                        delim = ", ";
                    }
                    System.out.println();
                }
            }
        }
        System.out.println("----- End printing delay transitions -----");
    }

    public static void printEventStates (Map <String, Set <Integer>> allEvents) {
        System.out.println("\n----- Start printing event states -----");
        String delim;
        for (String event : allEvents.keySet()) {
            System.out.print(event);
            System.out.print(", source states: {");
            delim = "";
            for (int st : allEvents.get(event)) {
                System.out.print(delim + st);
                delim = ", ";
            }
            System.out.println("}");
        }
        System.out.println("----- End printing event states -----");
    }

    public static void printEventStateNets (Map <String, Map <Integer, Set <Integer>>> eventStateNets) {
        System.out.println("\n----- Start printing event state networks -----");
        String delim;
        String delim2;
        for (String event : eventStateNets.keySet()) {
            System.out.print(event);
            System.out.print(", state networks: {");
            delim = "";
            for (int root : eventStateNets.get(event).keySet()) {
                System.out.print(delim);
                System.out.print("[" + root + "] ");
                System.out.print("{");
                delim2 = "";
                for (Integer st : eventStateNets.get(event).get(root)) {
                    System.out.print(delim2 + st);
                    delim2 = ", ";
                }
                delim = ", ";
                System.out.print("}");
            }
            System.out.println("}");
        }
        System.out.println("----- End printing event state networks -----");
    }

    public static void printEventDelayProb(Map <Integer, Set <Trans>> inLTS) {
        System.out.println("\n----- Start printing transition event delay probabilities -----");
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (!tr.lbl.equals("Time")) {
                    System.out.println(tr.asKey() + ": " + tr.eventDelayProb.getFractionString());
                }
            }
        }
        System.out.println("----- End printing transition event delay probabilities -----");
    }

    public static void printEqStarts (Map <String, Map <Integer, Set <Set <Integer>>>> allEvents) {
        System.out.println("\n----- Start printing equation starting states -----");
        String delim;
        int endingIdx;
        for (String event : allEvents.keySet()) {
            System.out.println("> Event: " + event);
            for (int st1 : allEvents.get(event).keySet()) {
                System.out.println("Starting: " + st1);
                endingIdx = 1;
                for (Set <Integer> states : allEvents.get(event).get(st1)) {
                    delim = "";
                    System.out.print("Ending " + endingIdx + ": ");
                    endingIdx++;
                    for (Integer st2 : states) {
                        System.out.print(delim + st2);
                        delim = ", ";
                    }
                    System.out.println();
                }
            }
        }
        System.out.println("----- End printing equation starting states -----");
    }

    public static void printTransNetPossibilities (Map <Integer, Set <TransPossibility>> transNetPossibilities) {
        System.out.println("\n----- Start printing equation paths -----");
        for (int start : transNetPossibilities.keySet()) {
            System.out.println("> Start state: " + start);
            for (TransPossibility tp : transNetPossibilities.get(start)) {
                System.out.println(">> Event: " + tp.event);
                for (int delay : tp.transPathsDelay.keySet()) {
                    System.out.println(">>> Delay: " + delay);
                    for (List <Trans> paths : tp.transPathsDelay.get(delay)) {
                        for (Trans trans : paths) {
                            System.out.println(trans.asKey());
                        }
                        System.out.println();
                    }
                }
            }
        }
        System.out.println("----- End printing equation paths -----");
    }

    public static void printTransVarMapping (Map <String, String> transVarMapping) {
        System.out.println("\n----- Start printing equation variables -----");
        for (String trans : transVarMapping.keySet()) {
            System.out.println(transVarMapping.get(trans) + ": " + trans);
        }
        System.out.println("----- End printing equation variables -----");
    }
    
    public static void printEquations (Map <Integer, Set <String>> equations,
    Map <Integer, Set <String>> equationVars) {
        System.out.println("\n----- Start printing equations -----");
        String varDelim;
        for (int start : equations.keySet()) {
            System.out.println("Statenet starts at: " + start);
            System.out.print("Vars: ");
            varDelim = "";
            for (String var : equationVars.get(start)) {
                System.out.print(varDelim + var);
                varDelim = ", ";
            }
            System.out.println();
            for (String eq : equations.get(start)) {
                System.out.println(eq);
            }
            System.out.println();
        }
        System.out.println("----- End printing equations -----");
    }

    public static void solveEquationsWithSympy (Map <String, FractionNumber> solverResult, String fileName,
    Map <String, String> equationsPy,
    Map <Integer, Set <String>> eqVars) throws InterruptedException {
        try {
            FileWriter myWriter = new FileWriter(fileName +  "-solver.py");
            myWriter.write("import sympy as sp\n");
            myWriter.write("import sys\n");
            String delimVar = "";
            for (int st : eqVars.keySet()) {
                for (String var : eqVars.get(st)) {
                    myWriter.write(delimVar + var);
                    delimVar = ", ";
                }
            }
            myWriter.write(" = sp.symbols(\'");
            delimVar = "";
            for (int st : eqVars.keySet()) {
                for (String var : eqVars.get(st)) {
                    myWriter.write(delimVar + var);
                    delimVar = " ";
                }
            }
            myWriter.write("\', real=True)\n");
            myWriter.write("vars_all = [");
            delimVar = "";
            for (int st : eqVars.keySet()) {
                for (String var : eqVars.get(st)) {
                    myWriter.write(delimVar + var);
                    delimVar = ", ";
                }
            }
            myWriter.write("]\n");
            myWriter.write("eqs = [\n");
            String delimEq = "";
            for (String left : equationsPy.keySet()) {
                if (equationsPy.get(left).split("/").length > 1) {
                    String up = equationsPy.get(left).split("/")[0];
                    String down = equationsPy.get(left).split("/")[1];
                    myWriter.write(delimEq + "\tsp.Eq(" + left + ", sp.Rational(" + up + ", " + down + "))");
                } else {
                    myWriter.write(delimEq + "\tsp.Eq(" + left + ", " + equationsPy.get(left) + ")");
                }
                delimEq = ",\n";
            }
            myWriter.write("\n]\nsol = sp.solve(eqs, vars_all, dict=True)\n");
            myWriter.write("print(\"Solution:\")\n" + //
                                "for s in sol:\n" + //
                                "\tprint(s)\n");
            myWriter.write("text_file = open(sys.argv[1] + \"-sympy.txt\", \"w\")\n" + //
                                "for s in sol:\n" + //
                                "    for key, value in s.items():\n" + //
                                "        text_file.write(str(key) + \":\" + str(value) + \"\\n" + //
                                "\")\n" + //
                                "text_file.close()");
            myWriter.close();
            executeCommands("python3 " + fileName + "-solver.py " + fileName);

            String string;
            try(BufferedReader br = new BufferedReader(new FileReader(fileName + "-sympy.txt"))) {
                while ((string = br.readLine()) != null) {
                    String var = string.split(":")[0];
                    if (string.split(":")[1].split("/").length > 1) {
                        int up = Integer.parseInt(string.split(":")[1].split("/")[0]);
                        int down = 1;
                        if (string.split(":")[1].split("/").length > 1) {
                            down = Integer.parseInt(string.split(":")[1].split("/")[1]);
                        }
                        solverResult.put(var, new FractionNumber(up, down));
                    } else {
                        int num = Integer.parseInt(string.split(":")[1]);
                        solverResult.put(var, new FractionNumber(num, 1));
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Python code creation error!");
            e.printStackTrace();
        }
    }

    public static void printEquationsUnmap (Map <Integer, Set <String>> equationsUnmap) {
        System.out.println("\n----- Start printing equations (unmap) -----");
        for (int start : equationsUnmap.keySet()) {
            System.out.println("Statenet starts at: " + start);
            for (String eq : equationsUnmap.get(start)) {
                System.out.println(eq);
            }
            System.out.println();
        }
        System.out.println("----- End printing equations (unmap) -----");
    }

    public static void printLTSs (ArrayList <Map <Integer, Set <Trans>>> LTSs) {
        System.out.println("\n----- Start printing local TLTSs -----");
        int idxLTS = 1;
        for (Map <Integer, Set <Trans>> lts : LTSs) {
            System.out.println("LTS " + idxLTS);
            idxLTS++;
            for (int st : lts.keySet()) {
                for (Trans tr : lts.get(st)) {
                    System.out.println(tr.asKey());
                }
            }
        }
        System.out.println("----- End printing local TLTSs -----");
    }

    public static void printMapping (Map <Integer, ArrayList <Set <Integer>>> mapping) {
        System.out.println("\n----- Start printing mapping between states -----");
        String delim;
        int idxLTS;
        for (int gState : mapping.keySet()) {
            idxLTS = 1;
            System.out.println("Global state: " + gState);
            for (Set <Integer> lStates : mapping.get(gState)) {
                System.out.print("LTS " + idxLTS + ": ");
                System.out.print("{");
                delim = "";
                for (int lstate : lStates) {
                    System.out.print(delim + lstate);
                    delim = ", ";
                }
                System.out.println("}");
                idxLTS++;
            }
        }
        System.out.println("\n----- End printing mapping between states -----");
    }

    public static void printPaths (ArrayList <ArrayList <Trans>> paths) {
        System.out.println("\n----- Start printing paths -----");
        int idx = 1;
        for (ArrayList <Trans> path : paths) {
            System.out.println("Path " + idx);
            idx++;
            for (Trans tr : path) {
                System.out.println(tr.asKey());
            }
            System.out.println();
        }
        System.out.println("----- End printing paths -----");
    }

    public static void printDistribution (Map <String, String> distribution) {
        if (distribution.size() > 0) {
            System.out.println("Detected probabilistic distribution: ");
            for (String event : distribution.keySet()) {
                System.out.println(event + ": " + distribution.get(event));
            }
        }
    }

    public static void writePTS (Map<Integer, Set<Trans>> inLTS, String fileName, String fileHeader) {
        try {
            FileWriter myWriter = new FileWriter(fileName +  "-pts.aut");
            myWriter.write(fileHeader+"\n");
            String tmpTime = "";
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(12);
            for (int st : inLTS.keySet()) {
                for (Trans itrs : inLTS.get(st)) {
                    tmpTime = "";
                    if (itrs.time > 0) {
                        tmpTime = " !" + itrs.time;
                    }
                    myWriter.write("(" + st + ", \"" + itrs.lbl + tmpTime + "; prob " + df.format(itrs.prb.getFloat())
                        + "\", " + itrs.dst + ")\n");
                }
            }
            myWriter.close();
            System.out.println("TPTS created: " + fileName + "-pts"+".aut");
        } catch (IOException e) {
            System.out.println("TPTS creation error!");
            e.printStackTrace();
        }
    }
    
    public static void writeDTMC (Map<Integer, Set<Trans>> inLTS, String fileName, String fileHeader) {
        try {
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(12);
            int numState = Integer.parseInt(fileHeader.replace("(", "").replace(")", "").replace(" ", "").split(",")[2]);
            String delimPlus = "";
            FileWriter myWriter = new FileWriter(fileName +".nm");
            myWriter.write("dtmc\n\n");
            myWriter.write("module translated\n\n");
            myWriter.write("\ts : [0.." + (numState-1) +"] init 0 ;\n\n");
            for (int st : inLTS.keySet()) {
                myWriter.write("\t[] s=" + st + " -> ");
                delimPlus = "";
                for (Trans itrs : inLTS.get(st)) {
                    myWriter.write(delimPlus + df.format(itrs.prb.getFloat()) + " : (s'=" + itrs.dst + ") ");
                    delimPlus = "+ ";
                }
                myWriter.write(";\n");
            }
            myWriter.write("\nendmodule\n");
            myWriter.close();
            System.out.println("DTMC created: " + fileName +".nm");
        } catch (IOException e) {
            System.out.println("DTMC translation error!");
            e.printStackTrace();
        }
    }

    public static void writeMappedLTS (Map<Integer, Set<Trans>> inLTS, Map <String, String> transVarMapping, String fileName, String fileHeader) throws InterruptedException {
        try {
            FileWriter myWriter = new FileWriter(fileName +  "-mapped.aut");
            myWriter.write(fileHeader+"\n");
            String tmpTime = "";
            for (int st : inLTS.keySet()) {
                for (Trans itrs : inLTS.get(st)) {
                    // myWriter.write(itrs.asKey() + " (" + transVarMapping.get(itrs.asKey()) + ")" + "\n");
                    tmpTime = "";
                    if (itrs.time > 0) {
                        tmpTime = " !" + itrs.time;
                    }
                    myWriter.write("(" + st + ", \"" + itrs.lbl + tmpTime + " (" + transVarMapping.get(itrs.asKey()) + ")\", " + itrs.dst + ")\n");
                }
            }
            myWriter.close();
            bashCreatePDF(fileName + "-mapped");
            // System.out.println("Mapped LTS created: " + fileName + "-mapped"+".aut");
        } catch (IOException e) {
            System.out.println("Mapped LTS creation error!");
            e.printStackTrace();
        }
    }

    public static void writeLTSMod (Map<Integer, Set<Trans>> inLTS, String fileName, String fileHeader) {
        try {
            FileWriter myWriter = new FileWriter(fileName + "-mod"+".aut");
            myWriter.write(fileHeader+"\n");
            for (int st : inLTS.keySet()) {
                for (Trans itrs : inLTS.get(st)) {
                    myWriter.write("(" + st + ", " + itrs.lbl + ", " + itrs.dst + ")\n");
                }
            }
            myWriter.close();
            System.out.println("LTS created: " + fileName + "-mod"+".aut");
        } catch (IOException e) {
            System.out.println("LTS computation error!");
            e.printStackTrace();
        }
    }
    
    public static void writeSteady (Map<Integer, Double> mapSteady, String fileName) {
        try {
            FileWriter myWriter = new FileWriter(fileName + "-steady.txt");
            String delim = "";
            for (int st : mapSteady.keySet()) {
                myWriter.write(delim + st + " : " + mapSteady.get(st));
                delim = "\n";
            }
            myWriter.close();
            System.out.println("Steady-state probabilities: " + fileName + "-steady.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeMapping (ArrayList <Map <Integer, Set <Trans>>> LTSs,
    Map<Integer, ArrayList <Set <Integer>>> mapping, String modelName) throws IOException {
        FileWriter myWriter = new FileWriter(modelName + "-mapping.txt");
        myWriter.write("Local LTSs:\n");
        int idxLTS = 1;
        for (Map <Integer, Set <Trans>> lts : LTSs) {
            myWriter.write("LTS " + idxLTS + "\n");
            idxLTS++;
            for (int st : lts.keySet()) {
                for (Trans tr : lts.get(st)) {
                    myWriter.write(tr.asKey() + "\n");
                }
            }
        }
        myWriter.write("\nMapping between states:\n");
        String delim;
        for (int gState : mapping.keySet()) {
            idxLTS = 1;
            myWriter.write("Global state: " + gState + "\n");
            for (Set <Integer> lStates : mapping.get(gState)) {
                myWriter.write("LTS " + idxLTS + ": ");
                myWriter.write("{");
                delim = "";
                for (int lstate : lStates) {
                    myWriter.write(delim + lstate);
                    delim = ", ";
                }
                myWriter.write("}\n");
                idxLTS++;
            }
        }
        myWriter.close();
        System.out.println("Mapping between states: " + modelName + "-mapping.txt");
    }

    public static FractionNumber floatToFraction (double x) {
        double error = 0.000001;
        int n = (int) x;
        x -= n;
        if (x < error) {
            return new FractionNumber(n, 1);
        } else if (1 - error < x) {
            return new FractionNumber(n + 1, 1);
        }
        int lowN = 0;
        int lowD = 1;
        int upN = 1;
        int upD = 1;
        while (true) {
            int midN = lowN + upN;
            int midD = lowD + upD;
            if (midD * (x + error) < midN) {
                upN = midN;
                upD = midD;
            } else if (midN < (x - error) * midD) {
                lowN = midN;
                lowD = midD;
            } else {
                return new FractionNumber(n * midD + midN, midD);
            }
        }
    }

    public static FractionNumber fracMultiply (FractionNumber a, FractionNumber b) {
        FractionNumber simple = new FractionNumber(a.up * b.up, a.down * b.down);
        simple.simplify();
        return simple;
    }

    public static FractionNumber fracDiv (FractionNumber a, FractionNumber b) {
        FractionNumber simple = new FractionNumber(a.up * b.down, a.down * b.up);
        simple.simplify();
        return simple;
    }

    public static FractionNumber fracPlus (FractionNumber a, FractionNumber b) {
        int newDown = a.down * b.down;
        int newUpA = newDown / a.down * a.up;
        int newUpB = newDown / b.down * b.up;
        FractionNumber simple = new FractionNumber(newUpA + newUpB, newDown);
        simple.simplify();
        return simple;
    }

    public static FractionNumber fracMin (FractionNumber a, FractionNumber b) {
        int newDown = a.down * b.down;
        int newUpA = newDown / a.down * a.up;
        int newUpB = newDown / b.down * b.up;
        FractionNumber simple = new FractionNumber(newUpA - newUpB, newDown);
        simple.simplify();
        return simple;
    }

    public static FractionNumber fracFlip (FractionNumber a) {
        return new FractionNumber(a.down, a.up);
    }

    public static boolean fracLess (FractionNumber a, FractionNumber b) {
        int newDown = a.down * b.down;
        int newUpA = newDown / a.down * a.up;
        int newUpB = newDown / b.down * b.up;
        if (newUpA < newUpB) {
            return true;
        } else {
            return false;
        }
    }

    public static void simplifyMapFracs (Map <String, ArrayList<FractionNumber>> mapFracs) {
        for (String s : mapFracs.keySet()) {
            for (FractionNumber frac : mapFracs.get(s)) {
                frac.simplify();
            }
        }
    }

    // public static void normalize (Map<Integer, Set<Trans>> inLTS) {
    //     double tmpTotal = 0.0;
    //     for (int st : inLTS.keySet()) {
    //         tmpTotal = 0.0;
    //         for (Trans tr : inLTS.get(st)) {
    //             tmpTotal += tr.prb;
    //         }
    //         for (Trans tr : inLTS.get(st)) {
    //             tr.prb = tr.prb / tmpTotal;
    //         }
    //     }
    // }
}
