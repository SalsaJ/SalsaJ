# Recompute po file into pot directory, but named .po

./macropo.csh ar
./macropo.csh bg
./macropo.csh ca
./macropo.csh cs
./macropo.csh de
./macropo.csh en
./macropo.csh es
./macropo.csh fa
./macropo.csh fr
./macropo.csh ga
./macropo.csh it
./macropo.csh ja
./macropo.csh nl
./macropo.csh pl
./macropo.csh pt
./macropo.csh ro
./macropo.csh ru
./macropo.csh si
./macropo.csh sv
./macropo.csh tr
./macropo2.csh zh zh_CN
./macropo2.csh el el_GR

# mv po files into language directory
mv ../pot/*_ar.po ../ar/
mv ../pot/*_bg.po ../bg/
mv ../pot/*_ca.po ../ca/
mv ../pot/*_cs.po ../cs/
mv ../pot/*_de.po ../de/
mv ../pot/*_en.po ../en/
mv ../pot/*_es.po ../es/
mv ../pot/*_fa.po ../fa/
mv ../pot/*_fr.po ../fr/
mv ../pot/*_ga.po ../ga/
mv ../pot/*_it.po ../it/
mv ../pot/*_ja.po ../ja/
mv ../pot/*_nl.po ../nl/
mv ../pot/*_pl.po ../pl/
mv ../pot/*_pt.po ../pt/
mv ../pot/*_ro.po ../ro/
mv ../pot/*_ru.po ../ru/
mv ../pot/*_si.po ../si/
mv ../pot/*_sv.po ../sv/
mv ../pot/*_tr.po ../tr/
mv ../pot/*_zh_CN.po ../zh/
mv ../pot/*_el_GR.po ../el/
