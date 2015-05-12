#macrop2.csh  zh zh_CN

prop2po -i ../resu/BlitterBundle_$2.properties -t ../templates/BlitterBundle.properties -o ../pot/BlitterBundle_$1.pot --encoding=utf8

prop2po -i ../resu/ColorBundle_$2.properties -t ../templates/ColorBundle.properties -o ../pot/ColorBundle_$2.pot --encoding=utf8

prop2po -i ../resu/EUHOUProps_$2.properties -t ../templates/EUHOUProps.properties -o ../pot/EUHOUProps_$2.pot --encoding=utf8

prop2po -i ../resu/MenusBundle_$2.properties -t ../templates/MenusBundle.properties -o ../pot/MenusBundle_$2.pot --encoding=utf8

prop2po -i ../resu/PluginBundle_$2.properties -t ../templates/PluginBundle.properties -o ../pot/PluginBundle_$2.pot --encoding=utf8

prop2po -i ../resu/ToolBundle_$2.properties -t ../templates/ToolBundle.properties -o ../pot/ToolBundle_$2.pot --encoding=utf8

