#!/usr/bin/env bash




echo "Input word to search"
read WORD

hadoop fs -rm -r /outputsearch
ENC=$(echo -n "$WORD" | openssl enc -e -des-ede3-cbc -K 396d6e67363576386a66346c786e39336e6162663938316d -iv 6137366e62356839 -a)
echo $WORD && echo $ENC
$HADOOP_HOME/bin/hadoop jar search.jar Search /output /outputsearch "$ENC"

loop=true
page=1
pageLimit=25
input="N"

while [ $loop == true ];
do
input=$(hadoop fs -cat /outputsearch/part-r-00000 2>/dev/null  | head -n $(expr $page \* $pageLimit)  | tail -n $pageLimit)

java -cp decrypt.jar com.mypackage.Decrypt "$input"
echo "Next page type N ; Previous page type P ; Exit type E "
read input
if [ "$input" == "N" ];
then
    page=$(expr $page + 1)
    echo "Next page $page"
elif [ "$input" == "P" ];
 then
    if [ $page == 1 ]
    then
        echo "No previous page"
    else 
        page=$(expr $page - 1)
    fi   
elif [ "$input" == "E" ];    then

    loop=false
fi 

done 