# P2I-P4
Code de la Phase P4 du projet P2I

### Compilation et test
```shell script
# Pour tout compiler dans des jars
gradlew allShadowJars
# Pour lancer le service
gradlew :ServiceCapteur:run
# Pour lancer l'interface
gradlew :Interface:run
```

Depuis IntelliJ, il y a déjà des configurations de lancement
dans le dossier `.run` qui devraient déjà être importées.
