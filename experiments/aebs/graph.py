import matplotlib.pyplot as plt 
import numpy as np
import seaborn 
seaborn.set(font_scale=2.1) 
# plt.rcParams["font.family"] = "Times New Roman"
plt.rcParams.update({'font.size': 20})
fig = plt.figure()
tickStart=0
tickEnd=14
tmpTicks = []
for x in range(tickStart, tickEnd + 1):
    tmpTicks.append(x)

for resConf in ["C1", "C2"]:
    
    if (resConf=="C1"):
        ax = fig.add_subplot(121)
        
    else:
        ax = fig.add_subplot(122)

    for resSource in ["Specification", "Simulation", "Execution"]:
        file1 = open("verdicts" + "/" + resSource + "-" + resConf + ".txt", 'r')
        lines = file1.readlines()
        x = []
        y = []

        clr = "black"
        stl = "dashed"
        if (resSource == "Specification"):
            resSource = "Golden"
            clr = "red"
            stl = "solid"
        elif (resSource == "Simulation"):
            clr = "blue"
            stl = "dashed"
        else:
            clr = "black"
            stl = "dotted"

        for ln in lines:
            ln = ln.rstrip().replace(" ", "").split(":")
            x.append(int(ln[0]))
            y.append(float(ln[1]))
        plt.xticks(tmpTicks, fontsize=20)
        plt.yticks(fontsize=20)
        plt.ylim(0, 1.01)
        plt.xlabel('Response time', fontsize=20) 
        plt.ylabel('Probability', fontsize=20) 
        plt.title("Configuration " + resConf)
        ax.plot(x, y, linestyle=stl, linewidth=2, color=clr, label=resSource)
        ax.legend(fontsize=20)

# plt.legend(fontsize=20) 
# plt.savefig("myImagePDF.pdf", format="pdf", bbox_inches="tight")
plt.show()