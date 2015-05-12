#!/bin/bash
# oliprop2po.sh

for file in `ls *.properties`
do
newname=`basename $file .properties`
newname=$newname".pot"
echo $newname "--> POT template"
prop2po -i $file -o $newname --pot
done
