#!/bin/bash
# almprop2po.sh
# script to create .properties files from .po files stored in directories xx
# to be run from this dir
# .po are used by Pootle, .properties are used by Java
# just type ./olipo2prop.sh fr   -- if you want to process fr files

if [ $1 == "" ]
then
 extens="en"
else
 extens=""$1
fi

cd $extens
pwd

for file in `ls *.po`
do
 newname=`basename $file .po`
 newname=""$newname.properties
 templatename=${file%_*po}
 templatename="../templates/"${templatename%_*}".properties"
 echo 'File to be processed: ' $file
 echo 'File name after process: ' $newname
 echo 'Template name: ' $templatename
 po2prop -i $file -o $newname -t $templatename
done
