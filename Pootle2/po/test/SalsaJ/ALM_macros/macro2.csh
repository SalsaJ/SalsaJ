#./macro2.csh zh zh_CN

po2prop -i ../$1/BlitterBundle_$2.po -t ../templates/BlitterBundle.properties -o ../resu/BlitterBundle_$2.properties --personality=java-utf8 

po2prop -i ../$1/ColorBundle_$2.po -t ../templates/ColorBundle.properties -o ../resu/ColorBundle_$2.properties --personality=java-utf8 

po2prop -i ../$1/EUHOUProps_$2.po -t ../templates/EUHOUProps.properties -o ../resu/EUHOUProps_$2.properties --personality=java-utf8 

po2prop -i ../$1/MenusBundle_$2.po -t ../templates/MenusBundle.properties -o ../resu/MenusBundle_$2.properties --personality=java-utf8 

po2prop -i ../$1/PluginBundle_$2.po -t ../templates/PluginBundle.properties -o ../resu/PluginBundle_$2.properties --personality=java-utf8 

po2prop -i ../$1/ToolBundle_$2.po -t ../templates/ToolBundle.properties -o ../resu/ToolBundle_$2.properties --personality=java-utf8 
