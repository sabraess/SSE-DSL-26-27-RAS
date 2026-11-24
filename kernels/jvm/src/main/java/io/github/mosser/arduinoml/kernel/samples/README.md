Depuis ```kernels/jvm``` : 


```powershell
javac -d out (Get-ChildItem -Recurse src/main/java -Filter *.java).FullName
java -cp out io.github.mosser.arduinoml.kernel.samples.Scenario4 | Out-File -Encoding ascii scenarioX\scenarioX.ino
```

Faire les bons câblages sur la carte