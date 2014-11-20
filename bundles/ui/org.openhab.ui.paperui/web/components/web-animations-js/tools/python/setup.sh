#! /bin/bash

# Create the python environment needed by run-tests.py and closure-linter and
# other Python tools.

OLD_PWD=$PWD

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $DIR
if [ ! -f bin/activate ]; then
  echo "Setting up Python environment in $DIR"

  if [ x$(which pip) == x -o x$(which virtualenv) == x ]; then
    cat <<EOF
Can not autoinstall as pip and virtualenv are not avaliable.

To install 'pip' please do one of the following;

# sudo apt-get install python-pip python-virtualenv

or

# sudo easy_install pip
# sudo pip install virtualenv
EOF
    exit 1
  fi

  if virtualenv --system-site-packages .; then
    echo -e;
  else
   cat <<EOF
Was unable to set up the virtualenv environment. Please see output for errors.
EOF
    exit 1
  fi
fi

source bin/activate

function ensureRequirementsMet() {
  # Check if installed
  pip install --no-download $@ > /dev/null 2>&1
  if [ $? -ne 0 ]; then
    # Install dependencies
    pip install --upgrade $@
    if [ $? -ne 0 ]; then
      cat <<EOF
Unable to install dependencies. Please see error output above.
EOF
      exit 1
    fi
  fi
}

ensureRequirementsMet 'pip>=1.5'
ensureRequirementsMet -r requirements.txt

cd $OLD_PWD
