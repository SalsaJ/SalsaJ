#!/bin/bash
# almprop2po.sh

if [ $1 == "" ]
then
 extens="_??"
else
 extens="_"$1
fi

for file in `ls properties/*$extens.properties`
do
newname=`basename $file .properties`
templatename=`basename $newname $extens`"_EN.properties"
echo ">>>File:" $newname "   >>>Template:" $templatename
prop2po --encoding=UTF8 -i $file -o $newname.po -t $templatename
done
