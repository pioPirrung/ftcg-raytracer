# ftcg-raytracer

# Hallo

Beim importieren des Projekts in Eclipse müsst ihr auf General gehen und dort "Existing Projects into Workspace" auswählen. Zumindest habe ich es so gemacht :). Wichtig ist das ihr den Git ordner auswählt also ftcg-raytracer und nicht direkt den Raytracer00 Ordner.

# Eigene Erweiterung
## Obj Parser
Unsere eigene Erweiterung ist ein .obj Parser. Hierfür wurde der Abstrakten Klasse RTFile eine abstrakte Methode hinzugefügt die sich readObjContent nennt. Die Methode wird dann von den Klassen TMesh, ISphere und RTScene
implementiert. Konkret eigentlich nur TMesh, die anderen Methodenrümpfe sind leer (ISphere und RTScene). In dieser Methode findet dann das Parsing statt und ist auch (hoffentlich gut) Dokumentiert.

Zusätzlich wurde dem RayTracerGui noch einen ExtensionFilter für .obj Dateien hinzugefügt. Damit ein Objekt aus einer .obj korrekt gerendert werden kann, ist es notwendig, dass die .obj Datei, als auch die .mtl Datei im selben Verzeichnis liegen. Denn der Parser sucht im selben Verzeichnis der .obj nach seiner zugehörigen .mtl Datei um das Material Parsen zu können. 
