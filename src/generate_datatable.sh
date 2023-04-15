#!/bin/bash

mkdir -p temp

output="temp/datatable.tsv"

printf "id\tdate\ttime\ttemperature_celsius\thumidity_perc\taltitude_m\tpressure_hPa\tweather\n" > $output

clear

id=1
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
    ' | awk -F'\t' -v id=$id '
        {
            gsub(/ /, "\t", $1)
            print id"\t"$0
        }
    ' >> $output

    id=$(($id+1))
done

# remove ^M
cat $output | tr -d '\r' > $output".tmp"
mv $output".tmp" $output

#less -S $output
