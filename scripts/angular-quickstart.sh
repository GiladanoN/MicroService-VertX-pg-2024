
# angular course: (udemy)
# https://www.udemy.com/course/the-complete-guide-to-angular-2/learn/lecture/10415888#overview

# create the app, flags are optional, and are per instruction from above course video
ng new my-first-app \
  --no-strict --standalone false --routing false

# run project:
cd my-first-app/  # cd into new project folder
ng serve   # start this new project

# troubleshooting:
npm cache clean --force  # clean npm cache before re-installing angular-cli
sudo chown -R 501:20 "~/.npm"  # remove root-owned cahced file (reccomended by cache-clean cmd)

# versions:
node --version
npm --version
ng v

# install(s):
sudo npm install -g npm  # install latest/update npm version
npm i -g @angular/cli@latest  # install latest angular-cli version
