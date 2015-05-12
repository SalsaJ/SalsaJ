#macropo.csh fr


prop2po -i ../resu/BlitterBundle_$1.properties -t ../templates/BlitterBundle.properties -o ../pot/BlitterBundle_$1.po --encoding=utf8

prop2po -i ../resu/ColorBundle_$1.properties -t ../templates/ColorBundle.properties -o ../pot/ColorBundle_$1.po --encoding=utf8

prop2po -i ../resu/EUHOUProps_$1.properties -t ../templates/EUHOUProps.properties -o ../pot/EUHOUProps_$1.po --encoding=utf8

prop2po -i ../resu/MenusBundle_$1.properties -t ../templates/MenusBundle.properties -o ../pot/MenusBundle_$1.po --encoding=utf8

prop2po -i ../resu/PluginBundle_$1.properties -t ../templates/PluginBundle.properties -o ../pot/PluginBundle_$1.po--encoding=utf8

prop2po -i ../resu/ToolBundle_$1.properties -t ../templates/ToolBundle.properties -o ../pot/ToolBundle_$1.po --encoding=utf8

