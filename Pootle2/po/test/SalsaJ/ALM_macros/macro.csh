#./macro.csh ar

po2prop -i ../$1/BlitterBundle_$1.po -t ../templates/BlitterBundle.properties -o ../resu/BlitterBundle_$1.properties --personality=java-utf8 

po2prop -i ../$1/ColorBundle_$1.po -t ../templates/ColorBundle.properties -o ../resu/ColorBundle_$1.properties --personality=java-utf8 

po2prop -i ../$1/EUHOUProps_$1.po -t ../templates/EUHOUProps.properties -o ../resu/EUHOUProps_$1.properties --personality=java-utf8 

po2prop -i ../$1/MenusBundle_$1.po -t ../templates/MenusBundle.properties -o ../resu/MenusBundle_$1.properties --personality=java-utf8 

po2prop -i ../$1/PluginBundle_$1.po -t ../templates/PluginBundle.properties -o ../resu/PluginBundle_$1.properties --personality=java-utf8 

po2prop -i ../$1/ToolBundle_$1.po -t ../templates/ToolBundle.properties -o ../resu/ToolBundle_$1.properties --personality=java-utf8 
