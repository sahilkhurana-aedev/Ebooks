#!/bin/bash
for dir in ./*; do
cd $dir
#start bash -c "echo $PWD; rm -r target;"
#start bash -c "echo $PWD; git reset --hard HEAD; git fetch; git checkout develop; git checkout release/4.1.12.1; git pull;"
echo $dir
rm -r target
git reset --hard HEAD
git fetch
#git checkout develop
git checkout release/4.1.12.2
git pull
#git reset --hard develop #Or any other command to execute in every folder
cd ..
done