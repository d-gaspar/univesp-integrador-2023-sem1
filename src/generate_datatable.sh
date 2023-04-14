#!/bin/bash

mkdir -p temp

output="temp/datatable.tsv"

printf "date\ttime\ttemperature_celsius\thumidity_perc\taltitude_m\tpressure_hPa\tweather\n" > $output

clear

for f in data/raw/*; do
    weather=$(echo "$f" | awk -F'.' '{print $1}' | awk -F'-' '{print $NF}')

    cat "$f" | grep -iv "connect" | awk -F'\t' -v weather=$weather '
        {
            gsub(/%/, "")
            gsub(/hPa/, "")
            gsub(/[*]C/, "")
            gsub(/m/, "")
            gsub(/[THAP]:/, "\t")
            print $0"\t"weather
        }
    ' | awk -F'\t' '
        {
            gsub(/ /, "\t", $1)
            print $0
        }
    ' >> $output
done

# remove ^M
cat $output | tr -d '\r' > $output".tmp"
mv $output".tmp" $output

#less -S $output
