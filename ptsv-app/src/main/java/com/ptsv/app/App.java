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
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        double prb;
        int dst;
        Set <String> isAssignedBy;
        public Trans(int src, String lbl, int tm, int dst) {
            this.src = src;
            this.lbl = lbl;
            this.time = tm;
            this.ctr = 0;
            this.prb = 1.0;
            this.dst = dst;
            this.isAssignedBy = new HashSet<String>();
        }
        public Trans(int src, String lbl, int tm, int dst, double prb) {
            this.src = src;
            this.lbl = lbl;
            this.time = tm;
            this.ctr = 0;
            this.prb = prb;
            this.dst = dst;
            this.isAssignedBy = new HashSet<String>();
        }
        public Trans(int src, String lbl, int tm, int dst, int ctr, double prb) {
            this.src = src;
            this.lbl = lbl;
            this.time = tm;
            this.ctr = ctr;
            this.prb = prb;
            this.dst = dst;
            this.isAssignedBy = new HashSet<String>();
        }
        public void ctrUp () {
            ctr++;
        }
        public void prbComp(int ctrState) {
            prb = (double) ctr / ctrState;
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
        public TransPair() {
        }
    }
    static class Possibility {
        ArrayList <Set <String>> posibString;
        Double posibProb;
        Integer posibCounter;
        Set <ArrayList <Trans>> posibPaths;
        Set <ArrayList <String>> eqs;
        public Possibility(ArrayList <Set <String>> posibString, Double posibProb, Integer posibCounter,
                Set<ArrayList<Trans>> posibPaths) {
            this.posibString = posibString;
            this.posibProb = posibProb;
            this.posibCounter = posibCounter;
            this.posibPaths = posibPaths;
            eqs = new HashSet<ArrayList <String>>();
        }
        public void setEqs (Set <ArrayList <String>> inEqs) {
            eqs = new HashSet<ArrayList <String>>();
            eqs.addAll(inEqs);
        }
    }
    static class FractionNumber {
        int up;
        int down;
        public FractionNumber(int up, int down) {
            this.up = up;
            this.down = down;
        }
        public String getString () {
            return up+"/"+down;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            System.out.println("Missing IF model name!");
            return;
        } else if (args.length > 0) {
            String ifModel = "";
            if (args[0].split("\\.").length > 0) {
                ifModel = args[0].split("\\.")[0];
            } else {
                ifModel = args[0];
            }
            bashCompileLTS(ifModel, "global");
            modLTS(ifModel);
            bashReduceLTS(ifModel, "global");
            if (args.length == 1) {
                System.out.println("\n>>>>>>>>> Computing TPTS of " + args[0] + " according to uniform distribution\n");
                ArrayList <String> taNames = new ArrayList<String>();
                bashIndividualLTSs(ifModel, taNames);
                computePTSbyDistribution(ifModel + "-min", taNames);
            } else if (args.length >= 2) {
                System.out.println("\n>>>>>>>>> Computing TPTS of " + args[0] + " according to traces in folder " + args[1]);
                computePTSbyTraces(ifModel + "-min", args[1]);
            }
        }
    }

    public static void computePTSbyDistribution(String ifModel, ArrayList <String> taNames) throws FileNotFoundException, IOException, InterruptedException {
        ArrayList <Map <Integer, Set <Trans>>> taLTSs = new ArrayList<Map <Integer, Set <Trans>>>();
        ArrayList <Map <String, String>> mapEventPosList = new ArrayList<Map <String, String>>();
        Map <String, String> mapEventPos;
        Map <Integer, Set <Trans>> taLTS;
        Map <Integer, Set <Trans>> statesIns;
        Map <String, Set <Integer>> allEvents;
        for (String taName : taNames) {
            taLTS = new HashMap <Integer, Set <Trans>>();
            mapEventPos = new HashMap <String, String>();
            statesIns = new HashMap <Integer, Set <Trans>>();
            allEvents = new HashMap <String, Set <Integer>>();
            String header = buildLTS(taLTS, taName, statesIns);
            getAllEvents(allEvents, taLTS, statesIns);
            printAllEvents(allEvents, taName);
            startAssignProbs(taLTS, allEvents, mapEventPos);
            writePTS(taLTS, taName, header);
            taLTSs.add(taLTS);
            mapEventPosList.add(mapEventPos);
        }
        Map <Integer, Set <Trans>> inLTS = new HashMap <Integer, Set <Trans>>();
        Map <Integer, Set <Trans>> statesInsAll = new HashMap <Integer, Set <Trans>>();
        ArrayList <ArrayList <Trans>> paths = new ArrayList <ArrayList <Trans>>();
        Set <Possibility> posibilities = new HashSet<Possibility>();
        Set <ArrayList <String>> pathEqs = new HashSet<ArrayList <String>>();
        Set <ArrayList <String>> stateEqs = new HashSet<ArrayList <String>>();
        Map <String, String> mapEqVars = new HashMap<String, String>();
        Map <String, FractionNumber> solverResult = new HashMap<String, FractionNumber>();
        String header = buildLTS(inLTS, ifModel, statesInsAll);
        getPaths(paths, inLTS);
        getAllPosibilities(posibilities, paths, taLTSs, mapEventPosList);
        getPathEqs(posibilities, inLTS);
        printPosibilities(posibilities);
        getAllPathEqs(pathEqs, posibilities);
        printPathEqs(pathEqs);
        getStateEqs(stateEqs, inLTS);
        printStateEqs(stateEqs);
        getMapEqVars(mapEqVars, pathEqs, stateEqs);
        printEqVarsMap(mapEqVars);
        String solverVars = getSolverVars(mapEqVars);
        System.out.println("\nVariables: " + solverVars);
        String solverEqs = getSolverEqs(mapEqVars, pathEqs, stateEqs);
        System.out.println("\nEquations: " + solverEqs);
        solveEqs(solverEqs, solverVars, solverResult);
        printSolverResult(solverResult, mapEqVars);
        assignProbsToLTS(inLTS, mapEqVars, solverResult);
        writePTS(inLTS, ifModel, header);
        bashCreatePDF(ifModel + "-pts");
    }
    public static void getAllEvents (Map <String, Set <Integer>> allEvents, Map <Integer, Set <Trans>> inLTS,
    Map <Integer, Set <Trans>> statesIns) {
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (!tr.lbl.equals("Time")) {
                    allEvents.put(tr.lbl, new HashSet<Integer>());
                }
            }
        }

        Set <Integer> startingStates;
        for (String event : allEvents.keySet()) {
            startingStates = new HashSet<Integer>();
            getStartingStates(startingStates, event, inLTS, statesIns);
            allEvents.put(event, startingStates);
        }
    }
    public static void getStartingStates (Set <Integer> startingStates, String event, Map <Integer, Set <Trans>> inLTS,
    Map <Integer, Set <Trans>> statesIns) {
        int cState = 0;
        Set <Integer> visited = new HashSet<Integer>();
        traverseStartingStates(startingStates, cState, visited, event, inLTS, statesIns);
    }
    public static void traverseStartingStates (Set <Integer> startingStates, int cState, Set <Integer> visited, String event,
    Map <Integer, Set <Trans>> inLTS, Map <Integer, Set <Trans>> statesIns) {
        if (visited.contains(cState)) {
            return;
        }
        visited.add(cState);
        if (checkStartingState(cState, event, inLTS, statesIns)) {
            startingStates.add(cState);
        }
        for (Trans tr : inLTS.get(cState)) {
            traverseStartingStates(startingStates, tr.dst, visited, event, inLTS, statesIns);
        }
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
            if (trOut.time == 1) {
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
    static void startAssignProbs (Map<Integer, Set<Trans>> taLTS, Map <String, Set <Integer>> allEvents, Map <String, String> mapEventPos) {
        ArrayList <TransPair> tPs;
        int idxTp;
        for (String event : allEvents.keySet()) {
            for (int st : allEvents.get(event)) {
                tPs = new ArrayList<TransPair>();
                traverseToGetTransPairs(tPs, st, event, taLTS);
                assignProbs(tPs);
                idxTp = 1;
                for (TransPair transPair : tPs) {
                    mapEventPos.put(transPair.trEvent.asKey(), transPair.trEvent.lbl + "_E" + idxTp);
                    if (transPair.trTime != null) {
                        mapEventPos.put(transPair.trTime.asKey(), transPair.trEvent.lbl + "_D" + idxTp);
                    }
                    idxTp++;
                }
            }
        }
    }
    static void traverseToGetTransPairs(ArrayList <TransPair> tPs, int st, String event, Map<Integer, Set<Trans>> taLTS) {
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
    public static void assignProbs (ArrayList <TransPair> tPs) {
        DecimalFormat df = new DecimalFormat("#.#######");
        df.setRoundingMode(RoundingMode.HALF_UP);
        int range = tPs.size() ;
        double uniProb = (double) 1 / (range ) ;
        ArrayList <Double> distProbs = new ArrayList<Double>();
        ArrayList <Double> distProbTransList = new ArrayList<Double>();
        for (int i = 0; i < range; i++) {
            distProbs.add(uniProb);
        }
        computeDist(distProbTransList, distProbs);
        int idxTransPair = 0;
        for (Double prob : distProbTransList) {
            prob = Double.parseDouble(df.format(prob));
            tPs.get(idxTransPair).trEvent.prb = prob;
            if (tPs.get(idxTransPair).trTime != null){
                tPs.get(idxTransPair).trTime.prb = 1 - prob;
            }
            idxTransPair++;
        }
    }
    public static void computeDist (ArrayList <Double> dList, ArrayList <Double> distTrans) {
        ArrayList <Double> tmpDividers = new ArrayList<Double>();
        for (int i = 0; i < distTrans.size(); i++) {
            double divider = 1;
            for (Double t : tmpDividers) {
                divider = divider * t;
            }
            dList.add(distTrans.get(i) / divider);
            tmpDividers.add(1 - (distTrans.get(i) / divider));
        }
    }
    public static void getPaths (ArrayList <ArrayList <Trans>> paths, Map <Integer, Set<Trans>> inLTS) {
        Set <Integer> visited = new HashSet<Integer>();
        ArrayList <Trans> tmpPath = new ArrayList<Trans>();
        DFSToCollectPaths(paths, 0, visited, inLTS, tmpPath);
    }
    public static void DFSToCollectPaths (ArrayList <ArrayList <Trans>> paths, int cState, Set <Integer> visited,  Map <Integer, Set<Trans>> inLTS, ArrayList <Trans> tmpPath) {
        if (visited.contains(cState)) {
            ArrayList <Trans> newPath = new ArrayList<Trans>();
            for (Trans tr : tmpPath) {
                newPath.add(new Trans(tr.src, tr.lbl, tr.time, tr.dst, tr.ctr, tr.prb));
            }
            paths.add(newPath);
            visited.clear();
            visited.add(cState);
            return;
        } else {
            visited.add(cState);
        }
        for (Trans tr : inLTS.get(cState)) {
            tmpPath.add(tr);
            DFSToCollectPaths(paths, tr.dst, visited, inLTS, tmpPath);
            tmpPath.remove(tr);
        }
    }
    public static void getPathEqs (Set <Possibility> possibilities, Map <Integer, Set<Trans>> inLTS) {
        Set <ArrayList <String>> eqs;
        ArrayList <String> eq;
        Double tmpProb;
        for (Possibility possibility : possibilities) {
            eqs = new HashSet <ArrayList <String>>();
            tmpProb = possibility.posibProb / possibility.posibCounter;
            for (ArrayList <Trans> path : possibility.posibPaths) {
                eq = new ArrayList<String>();
                for (Trans tr : path) {
                    eq.add(tr.asKey());
                }
                eq.add(floatToFraction(tmpProb).getString());
                eqs.add(eq);
            }
            possibility.setEqs(eqs);
        }
    }
    public static void getAllPosibilities (Set <Possibility> possibilities,
    ArrayList <ArrayList <Trans>> paths, ArrayList <Map <Integer, Set <Trans>>> taLTSs,
    ArrayList <Map <String, String>> mapEventPosList) {
        ArrayList <ArrayList <Set <String>>> posibStrings = new ArrayList<ArrayList <Set <String>>>();
        ArrayList <Double> posibProbs = new ArrayList<Double>();
        ArrayList <Integer> posibCounters = new ArrayList<Integer>();
        ArrayList <Set <ArrayList <Trans>>> posibPaths = new ArrayList<Set <ArrayList <Trans>>>();
        ArrayList <Set <String>> posib;
        Set <String> posibSub;
        Set <ArrayList <Trans>> posibPathSet;
        Double probMultiplier;
        int idxLTS;
        ArrayList <Integer> cStates = new ArrayList<Integer>();
        ArrayList <Integer> tmpTimes = new ArrayList<Integer>();
        for (int i = 0; i < taLTSs.size(); i++) {
            cStates.add(0);
            tmpTimes.add(0);
        }
        for (ArrayList <Trans> path : paths) {
            posib = new ArrayList <Set <String>>();
            posibSub = new HashSet <String>();
            probMultiplier = 1.0;
            
            for (int i = 0; i < taLTSs.size(); i++) {
                cStates.set(i, 0);
                tmpTimes.set(i, 0);
            }
            for (Trans trPath : path) {
                if (posibSub.size() > 0 && trPath.lbl.equals("Time")) {
                    posib.add(posibSub);
                    posibSub = new HashSet<String>();
                }
                idxLTS = 0;
                for (Map <Integer, Set <Trans>> taLTS : taLTSs) {
                    for (Trans trLTS : taLTS.get(cStates.get(idxLTS))) {
                        if (trPath.lbl.equals(trLTS.lbl) && !trPath.lbl.equals("Time")) {
                            cStates.set(idxLTS, trLTS.dst);
                            if (mapEventPosList.get(idxLTS).containsKey(trLTS.asKey())) {
                                posibSub.add(mapEventPosList.get(idxLTS).get(trLTS.asKey()));
                                probMultiplier = probMultiplier * trLTS.prb;
                            }
                            break;
                        } else if (trPath.lbl.equals("Time") && trLTS.lbl.equals("Time")) {
                            tmpTimes.set(idxLTS, tmpTimes.get(idxLTS) + trPath.time);
                            if (tmpTimes.get(idxLTS) == trLTS.time) {
                                tmpTimes.set(idxLTS, 0);
                                cStates.set(idxLTS, trLTS.dst);
                                if (mapEventPosList.get(idxLTS).containsKey(trLTS.asKey())) {
                                    posibSub.add(mapEventPosList.get(idxLTS).get(trLTS.asKey()));
                                    probMultiplier = probMultiplier * trLTS.prb;
                                }
                            }
                            break;
                        }
                    }
                    idxLTS++;
                }
                if (posibSub.size() > 0 && trPath.lbl.equals("Time")) {
                    posib.add(posibSub);
                    posibSub = new HashSet<String>();
                }
            }
            if (!posibStrings.contains(posib)) {
                posibStrings.add(posib);
                posibProbs.add(probMultiplier);
                posibCounters.add(1);
                posibPathSet = new HashSet<ArrayList <Trans>>();
                posibPathSet.add(path);
                posibPaths.add(posibPathSet);
            } else {
                posibCounters.set(posibStrings.indexOf(posib), posibCounters.get(posibStrings.indexOf(posib)) + 1);
                posibPathSet = new HashSet<ArrayList <Trans>>();
                posibPathSet.addAll(posibPaths.get(posibStrings.indexOf(posib)));
                posibPathSet.add(path);
                posibPaths.set(posibStrings.indexOf(posib), posibPathSet);
            }
        }
        int idxPosib = 0;
        for (ArrayList <Set <String>> posibStr : posibStrings) {
            Possibility p = new Possibility(posibStr, posibProbs.get(idxPosib), posibCounters.get(idxPosib), posibPaths.get(idxPosib));
            possibilities.add(p);
            idxPosib++;
        }
    }
    public static void getAllPathEqs (Set <ArrayList <String>> pathEqs, Set <Possibility> possibilities) {
        for (Possibility possibility : possibilities) {
            pathEqs.addAll(possibility.eqs);
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
            if (result.toString().equals("{}")) {return;}
            String resultStrArr[] = result.toString().replace("{", "").replace("}", "").replace("\n", "").split(",");
            for (String string : resultStrArr) {
                String var = string.split("->")[0];
                int up = Integer.parseInt(string.split("->")[1].split("/")[0]);
                int down = 1;
                if (string.split("->")[1].split("/").length > 1) {
                    down = Integer.parseInt(string.split("->")[1].split("/")[1]);
                }
                solverResult.put(var, new FractionNumber(up, down));
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
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (mapEqVars.get(tr.asKey()) != null) {
                    if (solverResults.get(mapEqVars.get(tr.asKey())) != null){
                        FractionNumber tmpFrac = solverResults.get(mapEqVars.get(tr.asKey()));
                        tr.prb = (double) tmpFrac.up / tmpFrac.down;
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
        double tmpProb = 0.0;
        for (int state : inLTS.keySet()) {
            tmpTrCtr = 0;
            tmpProb = 0.0;
            for (Trans tr : inLTS.get(state)) {
                tmpTrCtr += tr.ctr;
                tmpProb += tr.prb;
            }
            if (tmpTrCtr == 0) {
                for (Trans tr : inLTS.get(state)) {
                    tr.prb = (double) 1 / inLTS.get(state).size();
                }
            }
            if (tmpProb < 1) {
                for (Trans tr : inLTS.get(state)) {
                    tr.prb = (double) tr.prb / tmpProb;
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
        renameStartFinishETC(inLTS);
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
    public static void renameStartFinishETC (Map<Integer, Set<Trans>> inLTS) { 
        String tmpLbl = "";
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                if (tr.lbl.contains("START") && tr.lbl.contains("FINISH")) {
                    int startIndex = ordinalIndexOf(tr.lbl, "0", 2);
                    int endIndex = ordinalIndexOf(tr.lbl, "{", 3);
                    tmpLbl = tr.lbl.substring(startIndex+1, endIndex);
                    tr.lbl = "\"" + tmpLbl + "\"";
                } else if (tr.lbl.contains("START")) {
                    int startIndex = tr.lbl.indexOf('{');
                    int endIndex = tr.lbl.indexOf('}', startIndex + 1);
                    tmpLbl = tr.lbl.substring(startIndex+1, endIndex) + "_START";
                    tr.lbl = "\"" + tmpLbl + "\"";
                } else if (tr.lbl.contains("FINISH")) {
                    int startIndex = tr.lbl.indexOf('{');
                    int endIndex = tr.lbl.indexOf('}', startIndex + 1);
                    tmpLbl = tr.lbl.substring(startIndex+1, endIndex) + "_FINISH";
                    tr.lbl = "\"" + tmpLbl + "\"";
                } else if (tr.lbl.contains("USELESS_ACT")) {
                    int startIndex = tr.lbl.indexOf('{');
                    int endIndex = tr.lbl.indexOf('}', startIndex + 1);
                    tmpLbl = tr.lbl.substring(startIndex+1, endIndex) + "_USELESS_ACT";
                    tr.lbl = "\"" + tmpLbl + "\"";
                } else if (tr.lbl.contains("USEFUL_ACT")) {
                    int startIndex = tr.lbl.indexOf('{');
                    int endIndex = tr.lbl.indexOf('}', startIndex + 1);
                    tmpLbl = tr.lbl.substring(startIndex+1, endIndex) + "_USEFUL_ACT";
                    tr.lbl = "\"" + tmpLbl + "\"";
                } else if (tr.lbl.contains("USELESS_EXEC")) {
                    int startIndex = tr.lbl.indexOf('{');
                    int endIndex = tr.lbl.indexOf('}', startIndex + 1);
                    tmpLbl = tr.lbl.substring(startIndex+1, endIndex) + "_USELESS_EXEC";
                    tr.lbl = "\"" + tmpLbl + "\"";
                } else if (tr.lbl.contains("USEFUL_EXEC")) {
                    int startIndex = tr.lbl.indexOf('{');
                    int endIndex = tr.lbl.indexOf('}', startIndex + 1);
                    tmpLbl = tr.lbl.substring(startIndex+1, endIndex) + "_USEFUL_EXEC";
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
                    tmpTrans.add(new Trans(tmpSrc, tmpLbl, tmpLblTime, tmpDst));
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
                    tmpTransIns.add(new Trans(tmpSrc, tmpLbl, tmpLblTime, tmpDst));
                    statesIns.put(tmpDst, tmpTransIns);
                } else { break; }
            }
        }
        return propMeta;
    }

    public static void bashCompileLTS(String ifModel, String name) throws IOException, InterruptedException {
        String command = "printf \"\\n" + //
                        ">>>>>>>>> Compiling " + name + " LTS\\n" + //
                        "\\n" + //
                        "\"\n" + //
                        "if2gen " + ifModel + "\".if\"\n" + //
                        "kp='./'" + ifModel + "'.x -t '" + ifModel + "'.aut'\n" + //
                        "eval $kp";
        executeCommands(command);
    }
    public static void bashReduceLTS(String ifModel, String name) throws IOException, InterruptedException {
        String command = "printf \"\\n" + //
                        ">>>>>>>>> Reducing " + name + " LTS\\n" + //
                        "\\n" + //
                        "\"\n" + //
                        "bcg_io " + ifModel + "\"-mod.aut\" \"" + ifModel + "-min.bcg\"\n" + //
                        "bcg_open \"" + ifModel + "-min.bcg\" reductor -weaktrace \"" + ifModel + "-min.bcg\"\n" + //
                        "bcg_min \"" + ifModel + "-min.bcg\"\n" + //
                        "bcg_io \"" + ifModel + "-min.bcg\" \"" + ifModel + "-min.aut\"\n" + //
                        "bcg_io \"" + ifModel + "-min.bcg\" \"" + ifModel + "-min.dot\"\n" + //
                        "dot -Tpdf -Gdpi=300 \"" + ifModel + "-min.dot\" > \"" + ifModel + "-min.pdf\"";
        executeCommands(command);
    }
    public static void bashIndividualLTSs(String ifModel, ArrayList <String> taNames) throws IOException, InterruptedException {
        try(BufferedReader br = new BufferedReader(new FileReader(ifModel + ".if"))) {
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
                        tmpSrcPath = Paths.get(ifModel + ".if");
                        tmpDstPath = Paths.get(ifModel + "-" + tmpProcName + ".if");
                        Files.copy(tmpSrcPath, tmpDstPath, StandardCopyOption.REPLACE_EXISTING);
                        try(BufferedReader brx = new BufferedReader(new FileReader(ifModel + ".if"))) {
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
                        bashCompileLTS(ifModel + "-" + tmpProcName, "process " + tmpProcName);
                        modLTS(ifModel + "-" + tmpProcName);
                        bashReduceLTS(ifModel + "-" + tmpProcName, "process " + tmpProcName);
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
    public static void printPosibilities (Set <Possibility> posibilities) {
        System.out.println("\n----- Start printing posibilities -----");
        int idxp = 0;
        String delimComma;
        for (Possibility possibility : posibilities) {
            System.out.println("> Posibility " + (idxp + 1));
            for (Set <String> evSet : possibility.posibString) {
                delimComma = "";
                System.out.print("{");
                for (String ev : evSet) {
                    System.out.print(delimComma + ev);
                    delimComma = ", ";
                }
                System.out.println("}");
            }
            System.out.println("> Probability: " + possibility.posibProb);
            System.out.println("> Counter: " + possibility.posibCounter);
            System.out.println("> Paths: ");
            for (ArrayList<Trans> path : possibility.posibPaths) {
                System.out.println("--");
                for (Trans tr : path) {
                    System.out.println(tr.asKey());
                }
            }
            System.out.println("> Path equations: ");
            printPathEqs(possibility.eqs);
            System.out.println();
            idxp++;
        }
        System.out.println("\n----- End printing posibilities -----");
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
        System.out.println("\n----- Start printing solver result -----\n");
        if (solverResult.size() != 0) {
            for (String tr : mapEqVars.keySet()) {
                System.out.println(mapEqVars.get(tr) + " " + tr + ": " + solverResult.get(mapEqVars.get(tr)).up + "/" + solverResult.get(mapEqVars.get(tr)).down);
            }
        }
        System.out.println("\n----- End printing solver result -----\n");
    }

    public static void writePTS (Map<Integer, Set<Trans>> inLTS, String fileName, String fileHeader) {
        try {
            FileWriter myWriter = new FileWriter(fileName +  "-pts.aut");
            myWriter.write(fileHeader+"\n");
            String tmpTime = "";
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(5);
            for (int st : inLTS.keySet()) {
                for (Trans itrs : inLTS.get(st)) {
                    tmpTime = "";
                    if (itrs.time > 0) {
                        tmpTime = " !" + itrs.time;
                    }
                    myWriter.write("(" + st + ", \"" + itrs.lbl + tmpTime + "; prob " + df.format(itrs.prb)
                        + "\", " + itrs.dst + ")\n");
                }
            }
            myWriter.close();
            System.out.println("PTS created: " + fileName + "-pts"+".aut");
        } catch (IOException e) {
            System.out.println("PTS creation error!");
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

    public static FractionNumber floatToFraction (double x) {
        double error = 0.000001;
        int n = (int) x;
        x -= n;
        if (x < error) {
            System.out.println("a");
            return new FractionNumber(n, 1);
        } else if (1 - error < x) {
            System.out.println("b");
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

}
