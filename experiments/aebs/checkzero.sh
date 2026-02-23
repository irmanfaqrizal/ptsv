model="specification/C1/aebs-min-execution-pts"
bcg_io $model".aut" $model".bcg"
bcg_open $model".bcg" exhibitor -case -dfs -all <<< "<until> [.*; prob 0]"