model="specification/C1/services-min-execution-pts"
bcg_io $model".aut" $model".bcg"
bcg_open $model".bcg" exhibitor -case -dfs -all <<< "<until> [.*; prob 0]"