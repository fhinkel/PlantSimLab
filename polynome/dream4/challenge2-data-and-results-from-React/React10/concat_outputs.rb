
echo "2-5 and 14-21"  > tmp.txt
cat 2-5-13-20/output-only-good_networks-1-edges.txt >> tmp.txt
echo "2-5 and 14-23"  >> tmp.txt
cat 2-5-13-22/output-only-good_networks-1-edges.txt >> tmp.txt
echo "2-6 and 14-29"  >> tmp.txt
cat 2-6-13-28/output-only-good_networks-1-edges.txt >> tmp.txt
mv tmp.txt output-only-good-pert-1.txt

mv output-only-good-pert-1.txt output-only-good-pert-network-1.txt
mv output-only-good-pert-2.txt output-only-good-pert-network-2.txt
mv output-only-good-pert-3.txt output-only-good-pert-network-3.txt
mv output-only-good-pert-4.txt output-only-good-pert-network-4.txt
mv output-only-good-pert-5.txt output-only-good-pert-network-5.txt

echo "2-5 and 14-21"  > tmp.txt
cat 2-5-13-20/output-only-good_networks-2-edges.txt >> tmp.txt
echo "2-5 and 14-23"  >> tmp.txt
cat 2-5-13-22/output-only-good_networks-2-edges.txt >> tmp.txt
echo "2-6 and 14-29"  >> tmp.txt
cat 2-6-13-28/output-only-good_networks-2-edges.txt >> tmp.txt
mv tmp.txt output-only-good-pert-2.txt


echo "2-5 and 14-21"  > tmp.txt
cat 2-5-13-20/output-only-good_networks-3-edges.txt >> tmp.txt
echo "2-5 and 14-23"  >> tmp.txt
cat 2-5-13-22/output-only-good_networks-3-edges.txt >> tmp.txt
echo "2-6 and 14-29"  >> tmp.txt
cat 2-6-13-28/output-only-good_networks-3-edges.txt >> tmp.txt
mv tmp.txt output-only-good-pert-3.txt

echo "2-5 and 14-21"  > tmp.txt
cat 2-5-13-20/output-only-good_networks-4-edges.txt >> tmp.txt
echo "2-5 and 14-23"  >> tmp.txt
cat 2-5-13-22/output-only-good_networks-4-edges.txt >> tmp.txt
echo "2-6 and 14-29"  >> tmp.txt
cat 2-6-13-28/output-only-good_networks-4-edges.txt >> tmp.txt
mv tmp.txt output-only-good-pert-4.txt

echo "2-5 and 14-21"  > tmp.txt
cat 2-5-13-20/output-only-good_networks-5-edges.txt >> tmp.txt
echo "2-5 and 14-23"  >> tmp.txt
cat 2-5-13-22/output-only-good_networks-5-edges.txt >> tmp.txt
echo "2-6 and 14-29"  >> tmp.txt
cat 2-6-13-28/output-only-good_networks-5-edges.txt >> tmp.txt
mv tmp.txt output-only-good-pert-5.txt

cat tmp.txt
cat 2-5-13-20/output-only-good_networks-1-edges.txt; echo "Hello" 
cat 2-5-13-20/output-only-good_networks-1-edges.txt; echo "Hello"  tmp.txt
cat 2-5-13-20/output-only-good_networks-1-edges.txt && echo "Hello" >> tmp.txt
cat 2-5-13-20/output-only-good_networks-1-edges.txt | echo "Hello" 
> tmp.txt
