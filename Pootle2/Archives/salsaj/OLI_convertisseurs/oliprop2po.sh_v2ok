#!/bin/bash
# oliprop2po.sh

if [ $1 == "" ]
then
 extens="_fr"
else
 extens="_"$1
fi

for file in `ls *$extens.properties`
do
newname=`basename $file .properties`
templatename=`basename $newname $extens`"_en.properties"
echo ">>>File:" $newname "   >>>Template:" $templatename
prop2po -i $file -o $newname.po -t $templatename
done
