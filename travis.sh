#!/bin/bash

ideaVersion="2016.1"
if [ "$PHPSTORM_ENV" == "2016.1" ]; then
    ideaVersion="2016.1.4"
elif [ "$PHPSTORM_ENV" == "2016.1.2" ]; then
    ideaVersion="2016.1.4"
elif [ "$PHPSTORM_ENV" == "2016.2" ]; then
    ideaVersion="2016.2"
elif [ "$PHPSTORM_ENV" == "2016.2.1" ]; then
    ideaVersion="2016.2.4"
elif [ "$PHPSTORM_ENV" == "eap" ]; then
    ideaVersion="163.5644.15"
fi

travisCache=".cache"

if [ ! -d ${travisCache} ]; then
    echo "Create cache" ${travisCache} 
    mkdir ${travisCache}
fi

function download {

  url=$1
  basename=${url##*[/|\\]}
  cachefile=${travisCache}/${basename}
  
  if [ ! -f ${cachefile} ]; then
      wget $url -P ${travisCache};
    else
      echo "Cached file `ls -sh $cachefile` - `date -r $cachefile +'%Y-%m-%d %H:%M:%S'`"
  fi  

  if [ ! -f ${cachefile} ]; then
    echo "Failed to download: $url"
    exit 1
  fi  
}

# Unzip IDEA

if [ -d ./idea  ]; then
  rm -rf idea
  mkdir idea
  echo "created idea dir"  
fi

# Download main idea folder
download "http://download.jetbrains.com/idea/ideaIU-${ideaVersion}.tar.gz"
tar zxf ${travisCache}/ideaIU-${ideaVersion}.tar.gz -C .

# Move the versioned IDEA folder to a known location
ideaPath=$(find . -name 'idea-IU*' | head -n 1)
mv ${ideaPath} ./idea
  
if [ -d ./plugins ]; then
  rm -rf plugins
  mkdir plugins
  echo "created plugin dir"  
fi

if [ "$PHPSTORM_ENV" == "2016.1" ]; then

    #php
    download "https://plugins.jetbrains.com/files/6610/24752/php-145.258.2.zip"
    unzip -qo $travisCache/php-145.258.2.zip -d ./plugins

    #twig
    download "https://plugins.jetbrains.com/files/7303/24757/twig-145.258.2.zip"
    unzip -qo $travisCache/twig-145.258.2.zip -d ./plugins

elif [ "$PHPSTORM_ENV" == "2016.1.2" ]; then

    #php
    download "https://plugins.jetbrains.com/files/6610/25793/php-145.970.40.zip"
    unzip -qo $travisCache/php-145.970.40.zip -d ./plugins

    #twig
    download "https://plugins.jetbrains.com/files/7303/24757/twig-145.258.2.zip"
    unzip -qo $travisCache/twig-145.258.2.zip -d ./plugins

elif [ "$PHPSTORM_ENV" == "2016.2" ]; then

    #php
    download "https://plugins.jetbrains.com/files/6610/27035/php-162.1121.34.zip"
    unzip -qo $travisCache/php-162.1121.34.zip -d ./plugins

    #twig
    download "https://plugins.jetbrains.com/files/7303/27028/twig-162.1121.34.zip"
    unzip -qo $travisCache/twig-162.1121.34.zip -d ./plugins

elif [ "$PHPSTORM_ENV" == "2016.2.1" ]; then

    #php
    download "https://plugins.jetbrains.com/files/6610/27859/php-162.1628.23.zip"
    unzip -qo $travisCache/php-162.1628.23.zip -d ./plugins

    #twig
    download "https://plugins.jetbrains.com/files/7303/27028/twig-162.1121.34.zip"
    unzip -qo $travisCache/twig-162.1121.34.zip -d ./plugins

elif [ "$PHPSTORM_ENV" == "eap" ]; then

    #php
    download "https://plugins.jetbrains.com/files/6610/28510/php-163.4830.18.zip"
    unzip -qo $travisCache/php-163.4830.18.zip -d ./plugins

    #twig
    download "https://plugins.jetbrains.com/files/7303/28516/twig-163.4830.18.zip"
    unzip -qo $travisCache/twig-163.4830.18.zip -d ./plugins

    # TODO: extract latest builds for plugins from eap site they are not public
    # https://confluence.jetbrains.com/display/PhpStorm/PhpStorm+Early+Access+Program
    # echo "No configuration for PhpStorm: $PHPSTORM_ENV"
    # exit 1

else
    echo "Unknown PHPSTORM_ENV value: $PHPSTORM_ENV"
    exit 1
fi

rm -f $travisCache/php-toolbox.jar
download "https://plugins.jetbrains.com/files/8133/27005/php-toolbox.jar"
cp $travisCache/php-toolbox.jar ./plugins

rm -f $travisCache/symfony2-plugin.jar
download "http://plugins.jetbrains.com/files/7219/26459/symfony2-plugin.jar"
cp $travisCache/symfony2-plugin.jar ./plugins

# Run the tests
if [ "$1" = "-d" ]; then
    ant -d -f build-test.xml -DIDEA_HOME=./idea
else
    ant -f build-test.xml -DIDEA_HOME=./idea
fi

# Was our build successful?
stat=$?

if [ "${TRAVIS}" != true ]; then
    ant -f build-test.xml -q clean

    if [ "$1" = "-r" ]; then
        rm -rf idea
        rm -rf plugins
    fi
fi

# Return the build status
exit ${stat}