import itertools
import random
import simpy
import scipy
import os
import shutil
import itertools
import math
from random import choices
import numpy

SIM_NUM = 5
SIM_TIME = 100
NORMAL = True

def randomizer(nMin, nMax, dist):
    population = []
    for x in range(nMin, nMax + 1):
        population.append(x)
    resRandom = choices(population, dist)
    return resRandom[0]

class TaskNoResource:
    def __init__(self, env, execMin, execMax, dist):
        self.env = env
        self.timeMin = execMin
        self.timeMax = execMax
        self.dist = dist

    def execute(self):
        yield self.env.timeout(randomizer(self.timeMin, self.timeMax, self.dist))

def activator(env, arrTrace, task, periodicJitter, dist):
    yield env.timeout(randomizer(0, int(periodicJitter * 2), dist))
    arrTrace.append((task, env.now))

def executor(env, arrTrace, task):
    arrTrace.append(("Start", env.now))
    yield env.process(task.execute())
    arrTrace.append(("Finish", env.now))

def sensor(env, arrTrace, off, periodicTime, periodicJitter, dist):
    yield env.timeout(off)
    env.process(activator(env, arrTrace,  "Read", periodicJitter, dist))
    while True:
        yield env.timeout(periodicTime)
        env.process(activator(env, arrTrace, "Read", periodicJitter, dist))

def controller(env, arrTrace, off, periodicTime, execMin, execMax, dist):
    task = TaskNoResource(env, execMin, execMax, dist)
    yield env.timeout(off)
    env.process(executor(env, arrTrace,  task))
    while True:
        yield env.timeout(periodicTime)
        env.process(executor(env, arrTrace, task))

def actuator(env, arrTrace, off, periodicTime, periodicJitter, dist):
    yield env.timeout(off)
    env.process(activator(env, arrTrace,  "Write", periodicJitter, dist))
    while True:
        yield env.timeout(periodicTime)
        env.process(activator(env, arrTrace, "Write", periodicJitter, dist))


if __name__ == '__main__':
    components = "sca"
    orders = [''.join(p) for p in itertools.permutations(components)]

    configurations = [
        # Conf, off, period, periodjitter/execmin, nothing/execmax
        (
            "C1",
            4, 10, 1, [7/9,1/9,1/9],
            10, 5, 1, 3, [1/9,7/9,1/9],
            4, 10, 1, [7/9,1/9,1/9]
        ),
        (
            "C2",
            4, 10, 1.5, [1/7,9/14,1/14,1/7],
            10, 5, 1, 3, [1/6,2/3,1/6],
            4, 10, 0.5, [8/9,1/9]
        )
    ]

    print("----- Configurations ----- \n")
    for (conf_name,
        S_OFF, S_P, S_J, S_D,
        C_OFF, C_P, C_MIN, C_MAX, C_D,
        A_OFF, A_P, A_J, A_D
        ) in configurations:
        print(f"Conf: {conf_name},\nSensor: ({S_OFF}, {S_P}, {S_J}, {S_D}),\nController : ({C_OFF}, {C_P}, {C_MIN}, {C_MAX}, {C_D}), \nActuator: ({A_OFF}, {A_P}, {A_J}, {A_D})\n")
    
    print("----- Simulations -----")

    directory_name = "results"
    if os.path.exists(directory_name):
        shutil.rmtree(directory_name)
    os.makedirs(directory_name)


    for cft in configurations:
        try:
            os.mkdir(f"{directory_name}/{cft[0]}")
            print(f"Directory '{directory_name}'/'{cft[0]}' created successfully.")
        except FileExistsError:
            print(f"Directory '{directory_name}'/'{cft[0]}' already exists.")

    seed = 0
    for sim in range(SIM_NUM):
        for order in orders:
            seed = seed + 1
            random.seed(seed)
            for (conf_name,
                S_OFF, S_P, S_J, S_D,
                C_OFF, C_P, C_MIN, C_MAX, C_D,
                A_OFF, A_P, A_J, A_D
                ) in configurations:
                print(f"started seed:{seed}, conf:{conf_name}.txt")
                env = simpy.Environment()
                arrTrace = []
                
                for comp in order:
                    match comp:
                        case "s":
                            env.process(sensor(env, arrTrace, S_OFF, S_P, S_J, S_D))
                        case "c":
                            env.process(controller(env, arrTrace, C_OFF, C_P, C_MIN, C_MAX, C_D))
                        case "a":
                            env.process(actuator(env, arrTrace, A_OFF, A_P, A_J, A_D))
                
                env.run(until=SIM_TIME)
                
                with open(f"{directory_name}/{conf_name}/T{seed}.txt", "w") as output:
                    delim = ""
                    prevTime = 0
                    for action, t in arrTrace:
                        timedif = int(round(t, 0)) - prevTime
                        prevTime = int(round(t, 0))
                        output.write(f"{delim}{action} {timedif}")
                        delim = ","
