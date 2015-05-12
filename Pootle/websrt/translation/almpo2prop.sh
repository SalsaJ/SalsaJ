#!/bin/bash
# almprop2po.sh
# script to create .properties files from .po files stored in directories xx
# to be run from this dir
# .po are used by Pootle, .properties are used by Java, .ini par php
# just type ./almpo2prop.sh fr   -- if you want to process fr files

 po2prop -i locale_RU.po -o locale_RU.ini -t locale_EN.po

