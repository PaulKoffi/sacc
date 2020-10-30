## Base de données

Lien vers la console
https://console.cloud.google.com/home/

Commande pour lancetr la db:

gcloud components install cloud-datastore-emulator

gcloud config set project project_id

gcloud beta emulators datastore start --data-dir=db
db : repertoire qui va être créee s'il nexiste pas




Pour lancer l'application:
*mvn appengine:run*
