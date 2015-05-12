#!/bin/bash
# almprop2po.sh

if [ $1 == "" ]
then
 extens="_FR"
else
 extens="_"$1
fi

for file in `ls *$extens.ini`
do
newname=`basename $file .ini`
templatename=`basename $newname $extens`"_EN.ini"
echo ">>>File:" $newname "   >>>Template:" $templatename
prop2po -i $file -o $newname.po -t $templatename
done
