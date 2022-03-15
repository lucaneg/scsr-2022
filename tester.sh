#!/bin/bash
logdir=eval-logs
if [ -d "$logdir" ]; then
  rm -rf $logdir
fi
mkdir $logdir

for source in $(git for-each-ref --format='%(refname:lstrip=3)' refs/remotes/origin/); do
  if [ "$source" == "master" ]; then
	continue
  fi
  
  if [ "$1" == "cleanup" ]; then
    git branch -D $source
    continue 
  fi
  
  logfile=$logdir/$source.log
  
  echo "+ checking out branch: $source"
  git checkout $source >> $logfile 2>&1
  if [ $? -ne 0 ]; then
	echo "###### checkout failed on $source"
	continue
  fi
  
  echo "++ merging master into $source"
  GIT_MERGE_AUTOEDIT=no git merge master >> $logfile 2>&1
  if [ $? -ne 0 ]; then
	echo "###### merge failed on $source"
	continue
  fi
  
  echo "++ building $source"
  bash ./gradlew assemble >> $logfile 2>&1
  if [ $? -ne 0 ]; then
	echo "###### build failed on $source"
	continue
  fi
  
  echo "++ testing $source"
  bash ./gradlew test --tests $1 >> $logfile 2>&1
  if [ $? -ne 0 ]; then
	echo "###### test failed on $source"
	continue
  fi
  
  echo "++ resetting $source"
  git reset --hard >> $logfile 2>&1
  if [ $? -ne 0 ]; then
	echo "###### reset failed on $source"
	continue
  fi
  git clean -fd >> $logfile 2>&1
  if [ $? -ne 0 ]; then
	echo "###### reset failed on $source"
	continue
  fi
done

echo "+ checking out master"
git checkout master
