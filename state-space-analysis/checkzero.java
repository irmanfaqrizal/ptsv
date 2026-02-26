import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class checkzero {

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
        public Trans(int src, String lbl, int tm, int dst, double prbFinal) {
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
        Map <Integer, Set <Trans>> inLTS = new HashMap <Integer, Set <Trans>>();
        String fileTPTS = args[0];
        buildLTS(inLTS, fileTPTS);
        printLTS(inLTS);
    }

    public static String buildLTS(Map<Integer, Set<Trans>> inLTS, String fileLTS)
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
                    if (inLTS.containsKey(tmpSrc)) {
                        tmpTrans.addAll(inLTS.get(tmpSrc));
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
                    inLTS.put(tmpSrc, tmpTrans);
                    if (!inLTS.containsKey(tmpDst)) {
                        inLTS.put(tmpDst, new HashSet<Trans>());
                    }

                    tmpTransIns = new HashSet<Trans>();
                    tmpTransIns.add(tmpNewTrans);
                } else { break; }
            }
        }
        return propMeta;
    }

    public static void printLTS (Map <Integer, Set <Trans>> inLTS) {
        System.out.println("\n----- Start printing TPTS -----");
        for (int st : inLTS.keySet()) {
            for (Trans tr : inLTS.get(st)) {
                System.out.println(tr.asKey() + ": " + tr.prbFinal);
            }
        }
        System.out.println("----- End printing TPTS -----");
    }
}
