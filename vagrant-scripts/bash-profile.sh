# Create a simple .bash_profile if it does not alreay exist
if [ ! -e ~/.bash_profile ]; then
    echo "if [ -r ~/.profile ]; then" > ~/.bash_profile
    echo "    . ~/.profile;" >> ~/.bash_profile
    echo "fi" >> ~/.bash_profile
fi
