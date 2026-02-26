model=$1
prob=$2
bcg_io $model".aut" $model".bcg"
bcg_open $model".bcg" exhibitor -case -dfs -all <<< "<until> [.*; prob 0]" | cat > $model"-sequences.txt"
