#!/bin/bash
# olifilenames.sh
# used to rename toto.po to toto_xx.po where xx is the country code (dir name)
# run as root

if [ $1 == "" ]
then
 extens="RO"
else
 extens=""$1
fi

cd $extens
pwd
for file in `ls *.po`
do
 newname=`basename $file .po`
 newname=$newname"_"$extens".po"
 echo ">>>Old name: " $file "   >>>New name: " $newname 
 mv $file $newname
 chmod 644 $newname
done
for file in `ls *.po.pending`
do
 newname=`basename $file .po.pending`
 newname=$newname"_"$extens".po.pending"
 echo ">>>Old name: " $file "   >>>New name: " $newname
 mv $file $newname
 chmod 644 $newname
done
