#!/bin/bash

GIT_ROOT=$(git rev-parse --show-toplevel)
function fancy_print() {
    tput bold; echo $1; tput sgr0
}

# dry run does everything except commit and push
DRY_RUN=false
while getopts n opt; do
    case $opt in
        n)
            DRY_RUN=true
            ;;
    esac
done

# ask SBT to produce scaladocs
cd $GIT_ROOT
fancy_print "Generating scaladocs..."
sbt doc
echo ""

# build gitbook sources
cd "$GIT_ROOT/macroid-docs"
fancy_print "Compiling with GitBook..."
grunt build
echo ""

# copy scaladocs
fancy_print "Copying scaladocs..."
rm -rf target/api
for module in core viewable akka; do
    mkdir -p "target/gitbook/api/$module"
    cp -r "../macroid-$module/target/scala-2.10/api/"* "target/gitbook/api/$module"
done

# push to github
if [ ! -d target/staging ]; then
    fancy_print "Cloning gh-pages repository..."
    mkdir target/staging
    git clone -b master git@github.com:macroid/macroid.github.io.git target/staging
    echo ""
fi
cd "$GIT_ROOT/macroid-docs/target/staging"
fancy_print "Updating gh-pages staging repository..."
git pull
echo ""
git rm -qrf .
cp -r ../gitbook/. .
git add -A
if ! $DRY_RUN; then
    fancy_print "Publishing to gh-pages..."
    git commit -m "Update documentation"
    git push origin master
fi
